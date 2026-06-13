package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.cecilia.gigi.dao.CupomDAO;
import com.cecilia.gigi.dao.DevolucaoDAO;
import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.Devolucao;
import com.cecilia.gigi.dominio.EntidadeDominio;

public class DevolucaoFacade implements IFacade {

    private final DevolucaoDAO devolucaoDAO;
    private final CupomDAO cupomDAO;

    public DevolucaoFacade() throws SQLException {
        this.devolucaoDAO = new DevolucaoDAO();
        this.cupomDAO = new CupomDAO();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return devolucaoDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return devolucaoDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return devolucaoDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        Devolucao dev = (Devolucao) entidadeDominio;

        if (dev.getVenda() == null || dev.getVenda().getId() == null) {
            throw new SQLException("{\"error\": \"Venda é obrigatória\", \"status\": 400}");
        }
        if (dev.getItens() == null || dev.getItens().isEmpty()) {
            throw new SQLException("{\"error\": \"Informe pelo menos um item para devolução\", \"status\": 400}");
        }
        for (var item : dev.getItens()) {
            if (item.getItemVenda() == null || item.getItemVenda().getId() == null) {
                throw new SQLException("{\"error\": \"ID do item da venda é obrigatório\", \"status\": 400}");
            }
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new SQLException("{\"error\": \"Quantidade do item deve ser maior que zero\", \"status\": 400}");
            }
        }

        dev.setStatus("SOLICITADA");
        dev.setDataSolicitacao(LocalDate.now());
        devolucaoDAO.save(dev);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        return false;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        return false;
    }

    public List<Devolucao> getByVendaId(Long vendaId) throws SQLException {
        return devolucaoDAO.getByVendaId(vendaId);
    }

    public void autorizar(Long id) throws SQLException {
        validarTransicao(id, "SOLICITADA", "AUTORIZADA");
        devolucaoDAO.updateStatus(id, "AUTORIZADA");

        Map<String, Object> info = devolucaoDAO.getClienteEValorByDevolucao(id);
        Long clienteId = (Long) info.get("clienteId");
        Double valor = (Double) info.get("valorTotal");

        Cupom cupom = new Cupom();
        cupom.setCodigo("DEV-" + id + "-" + gerarSufixo());
        cupom.setValor(25.0);
        cupom.setTipo("FIXED");
        cupom.setEhTroca(true);
        cupom.setAtivo(true);
        cupom.setClienteId(clienteId);
        cupomDAO.save(cupom);
    }

    private String gerarSufixo() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    public void concluir(Long id) throws SQLException {
        validarTransicao(id, "AUTORIZADA", "CONCLUIDA");
        devolucaoDAO.updateStatus(id, "CONCLUIDA");
        devolucaoDAO.reentradaItens(id);
    }

    private void validarTransicao(Long id, String statusEsperado, String novoStatus) throws SQLException {
        Devolucao dev = (Devolucao) devolucaoDAO.get(id);
        if (dev == null) {
            throw new SQLException("{\"error\": \"Devolução não encontrada\", \"status\": 404}");
        }
        if (!statusEsperado.equals(dev.getStatus())) {
            throw new SQLException("{\"error\": \"Transição inválida. Status atual: " + dev.getStatus() +
                    ". Esperado: " + statusEsperado + " para mudar para " + novoStatus + "\", \"status\": 422}");
        }
    }
}
