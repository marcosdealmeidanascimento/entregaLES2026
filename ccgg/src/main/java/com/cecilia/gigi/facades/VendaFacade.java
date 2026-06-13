package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.cecilia.gigi.dao.CupomDAO;
import com.cecilia.gigi.dao.EstoqueDAO;
import com.cecilia.gigi.dao.VendaDAO;
import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.ItemVenda;
import com.cecilia.gigi.dominio.Venda;
import com.cecilia.gigi.strategies.ValidarVenda;

public class VendaFacade implements IFacade {

    private final VendaDAO vendaDAO;
    private final ValidarVenda validarVenda;
    private final EstoqueDAO estoqueDAO;
    private final CupomDAO cupomDAO;

    public VendaFacade() throws SQLException {
        this.vendaDAO = new VendaDAO();
        this.validarVenda = new ValidarVenda();
        this.estoqueDAO = new EstoqueDAO();
        this.cupomDAO = new CupomDAO();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return vendaDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return vendaDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return vendaDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        Venda venda = (Venda) entidadeDominio;

        validarVenda.processar(venda);

        if (venda.getDataVenda() == null) {
            venda.setDataVenda(LocalDate.now());
        }
        venda.setStatus("EM_PROCESSAMENTO");

        // Buscar valorVenda do estoque e calcular totais
        double subtotal = 0.0;
        for (ItemVenda item : venda.getItens()) {
            Double valorVenda = getValorVendaProduto(item.getProduto().getId());
            item.setValorUnitario(valorVenda);
            item.setValorTotal(valorVenda * item.getQuantidade());
            subtotal += item.getValorTotal();
        }

        // Aplicar descontos dos cupons
        double desconto = 0.0;
        if (venda.getCupons() != null) {
            for (com.cecilia.gigi.dominio.Cupom cupom : venda.getCupons()) {
                if ("PERCENT".equals(cupom.getTipo())) {
                    desconto += subtotal * (cupom.getValor() / 100.0);
                } else {
                    desconto += cupom.getValor();
                }
            }
        }

        double valorTotal = Math.max(0, subtotal - desconto);
        if (venda.getFrete() != null) {
            valorTotal += venda.getFrete();
        }
        venda.setValorTotal(valorTotal);

        vendaDAO.save(venda);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        return false;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        vendaDAO.delete(id);
        return true;
    }

    public List<EntidadeDominio> getByClienteId(Long clienteId) throws SQLException {
        return vendaDAO.getByClienteId(clienteId);
    }

    // Transições de status

    public void aprovar(Long id) throws SQLException {
        validarTransicao(id, "EM_PROCESSAMENTO", "APROVADA");
        vendaDAO.updateStatus(id, "APROVADA");
    }

    public void reprovar(Long id) throws SQLException {
        validarTransicao(id, "EM_PROCESSAMENTO", "REPROVADA");
        vendaDAO.updateStatus(id, "REPROVADA");
        vendaDAO.reentradaItensVenda(id);
    }

    public void despachar(Long id) throws SQLException {
        validarTransicao(id, "APROVADA", "EM_TRANSPORTE");
        vendaDAO.updateStatus(id, "EM_TRANSPORTE");
    }

    public void entregar(Long id) throws SQLException {
        validarTransicao(id, "EM_TRANSPORTE", "ENTREGUE");
        vendaDAO.updateStatus(id, "ENTREGUE");
    }

    public void solicitarTroca(Long id) throws SQLException {
        validarTransicao(id, "ENTREGUE", "EM_TROCA");
        vendaDAO.updateStatus(id, "EM_TROCA");
    }

    public void autorizarTroca(Long id) throws SQLException {
        validarTransicao(id, "EM_TROCA", "TROCA_AUTORIZADA");
        vendaDAO.updateStatus(id, "TROCA_AUTORIZADA");
    }

    public void confirmarTroca(Long id) throws SQLException {
        validarTransicao(id, "TROCA_AUTORIZADA", "TROCADO");
        vendaDAO.updateStatus(id, "TROCADO");
        vendaDAO.reentradaItensVenda(id);

        Venda venda = (Venda) vendaDAO.get(id);
        Cupom cupom = new Cupom();
        cupom.setCodigo("TROCA-" + id + "-" + gerarSufixo());
        cupom.setValor(25.0);
        cupom.setTipo("FIXED");
        cupom.setEhTroca(true);
        cupom.setAtivo(true);
        cupom.setClienteId(venda.getCliente().getId());
        cupomDAO.save(cupom);
    }

    private String gerarSufixo() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private void validarTransicao(Long id, String statusEsperado, String novoStatus) throws SQLException {
        Venda venda = (Venda) vendaDAO.get(id);
        if (venda == null) {
            throw new SQLException("{\"error\": \"Venda não encontrada\", \"status\": 404}");
        }
        if (!statusEsperado.equals(venda.getStatus())) {
            throw new SQLException("{\"error\": \"Transição inválida. Status atual: " + venda.getStatus() +
                    ". Esperado: " + statusEsperado + " para mudar para " + novoStatus + "\", \"status\": 422}");
        }
    }

    private Double getValorVendaProduto(Long produtoId) throws SQLException {
        String sql = "SELECT est_valor_venda FROM est_estoque WHERE pro_id = ? AND est_quantidade > 0 ORDER BY est_data_entrada ASC LIMIT 1";
        java.sql.Connection connection = com.cecilia.gigi.database.DatabaseConnection.getConnection();
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("est_valor_venda");
            }
            throw new SQLException("{\"error\": \"Nenhum estoque disponível para o produto ID " + produtoId + "\", \"status\": 422}");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
