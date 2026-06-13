package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Cartao;
import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.ItemVenda;
import com.cecilia.gigi.dominio.PagamentoVenda;
import com.cecilia.gigi.dominio.Produto;
import com.cecilia.gigi.dominio.Venda;

public class VendaDAO implements IDAO {

    private final Connection connection;
    private final EstoqueDAO estoqueDAO;
    private final EnderecoDAO enderecoDAO;

    public VendaDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.estoqueDAO = new EstoqueDAO();
        this.enderecoDAO = new EnderecoDAO();
    }

    private void closeResources(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Venda venda = (Venda) entidadeDominio;

        String sqlVenda = "INSERT INTO ven_venda (cli_id, ven_data, ven_status, ven_frete, ven_valor_total) VALUES (?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO ven_item_venda (ven_id, pro_id, ven_item_quantidade, ven_item_valor_unitario, ven_item_valor_total) VALUES (?, ?, ?, ?, ?)";
        String sqlPagamento = "INSERT INTO ven_pagamento_venda (ven_id, car_id, pag_valor) VALUES (?, ?, ?)";
        String sqlCupom = "INSERT INTO ven_cupom_venda (vcv_venda_id, vcv_cupom_id, vcv_valor_desconto) VALUES (?, ?, ?)";

        PreparedStatement psVenda = null;
        PreparedStatement psItem = null;
        PreparedStatement psPagamento = null;
        PreparedStatement psCupom = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);

            psVenda = connection.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS);
            psVenda.setLong(1, venda.getCliente().getId());
            psVenda.setDate(2, Date.valueOf(venda.getDataVenda()));
            psVenda.setString(3, venda.getStatus());
            psVenda.setDouble(4, venda.getFrete() != null ? venda.getFrete() : 0.0);
            psVenda.setDouble(5, venda.getValorTotal());
            psVenda.executeUpdate();

            rs = psVenda.getGeneratedKeys();
            if (rs.next()) {
                venda.setId(rs.getLong(1));
            }

            psItem = connection.prepareStatement(sqlItem);
            for (ItemVenda item : venda.getItens()) {
                psItem.setLong(1, venda.getId());
                psItem.setLong(2, item.getProduto().getId());
                psItem.setInt(3, item.getQuantidade());
                psItem.setDouble(4, item.getValorUnitario());
                psItem.setDouble(5, item.getValorTotal());
                psItem.addBatch();

                estoqueDAO.darBaixa(item.getProduto().getId(), item.getQuantidade());
            }
            psItem.executeBatch();

            psPagamento = connection.prepareStatement(sqlPagamento);
            for (PagamentoVenda pagamento : venda.getPagamentos()) {
                psPagamento.setLong(1, venda.getId());
                psPagamento.setLong(2, pagamento.getCartao().getId());
                psPagamento.setDouble(3, pagamento.getValor());
                psPagamento.addBatch();
            }
            psPagamento.executeBatch();

            // Salva cupons aplicados
            if (venda.getCupons() != null && !venda.getCupons().isEmpty()) {
                psCupom = connection.prepareStatement(sqlCupom);
                double subtotal = venda.getItens().stream().mapToDouble(ItemVenda::getValorTotal).sum();
                for (Cupom cupom : venda.getCupons()) {
                    double valorDesconto = "PERCENT".equals(cupom.getTipo())
                            ? subtotal * (cupom.getValor() / 100.0)
                            : cupom.getValor();
                    psCupom.setLong(1, venda.getId());
                    psCupom.setLong(2, cupom.getId());
                    psCupom.setDouble(3, valorDesconto);
                    psCupom.addBatch();
                }
                psCupom.executeBatch();
            }

            connection.commit();
            return venda;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(psCupom, null);
            closeResources(psPagamento, null);
            closeResources(psItem, null);
            closeResources(psVenda, rs);
        }
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        String sql = "SELECT v.*, c.cli_nome FROM ven_venda v " +
                     "JOIN cli_cliente c ON v.cli_id = c.cli_id WHERE v.ven_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                Venda venda = mapResultSetToVenda(rs);
                venda.setItens(getItensByVendaId(venda.getId()));
                return venda;
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT v.*, c.cli_nome FROM ven_venda v " +
                     "JOIN cli_cliente c ON v.cli_id = c.cli_id ORDER BY v.ven_id DESC LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                Venda venda = mapResultSetToVenda(rs);
                venda.setItens(getItensByVendaId(venda.getId()));
                lista.add(venda);
            }
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return new ArrayList<>();
    }

    public List<EntidadeDominio> getByClienteId(Long clienteId) throws SQLException {
        String sql = "SELECT v.*, c.cli_nome FROM ven_venda v " +
                     "JOIN cli_cliente c ON v.cli_id = c.cli_id WHERE v.cli_id = ? ORDER BY v.ven_id DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Venda venda = mapResultSetToVenda(rs);
                venda.setItens(getItensByVendaId(venda.getId()));
                lista.add(venda);
            }
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    public void updateStatus(Long id, String novoStatus) throws SQLException {
        String sql = "UPDATE ven_venda SET ven_status = ? WHERE ven_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, novoStatus);
            ps.setLong(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("{\"error\": \"Venda não encontrada\", \"status\": 404}");
            }
        } finally {
            closeResources(ps, null);
        }
    }

    @Override
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        return null;
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        String sql = "DELETE FROM ven_venda WHERE ven_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
            return null;
        } finally {
            closeResources(ps, null);
        }
    }

    public void reentradaItensVenda(Long vendaId) throws SQLException {
        List<ItemVenda> itens = getItensByVendaId(vendaId);
        for (ItemVenda item : itens) {
            estoqueDAO.reentrada(item.getProduto().getId(), item.getQuantidade());
        }
    }

    private List<ItemVenda> getItensByVendaId(Long vendaId) throws SQLException {
        String sql = "SELECT i.*, p.pro_nome, p.pro_foto FROM ven_item_venda i " +
                     "JOIN pro_produto p ON i.pro_id = p.pro_id WHERE i.ven_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ItemVenda> itens = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, vendaId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemVenda item = new ItemVenda();
                item.setId(rs.getLong("ven_item_id"));
                item.setQuantidade(rs.getInt("ven_item_quantidade"));
                item.setValorUnitario(rs.getDouble("ven_item_valor_unitario"));
                item.setValorTotal(rs.getDouble("ven_item_valor_total"));

                Produto produto = new Produto();
                produto.setId(rs.getLong("pro_id"));
                produto.setNome(rs.getString("pro_nome"));
                produto.setFoto(rs.getString("pro_foto"));
                item.setProduto(produto);

                itens.add(item);
            }
            return itens;
        } finally {
            closeResources(ps, rs);
        }
    }

    private List<PagamentoVenda> getPagamentosByVendaId(Long vendaId) throws SQLException {
        String sql = "SELECT pag_id, car_id, pag_valor FROM ven_pagamento_venda WHERE ven_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<PagamentoVenda> pagamentos = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, vendaId);
            rs = ps.executeQuery();
            while (rs.next()) {
                PagamentoVenda pagamento = new PagamentoVenda();
                pagamento.setId(rs.getLong("pag_id"));
                pagamento.setValor(rs.getDouble("pag_valor"));

                Cartao cartao = new Cartao();
                cartao.setId(rs.getLong("car_id"));
                pagamento.setCartao(cartao);

                pagamentos.add(pagamento);
            }
            return pagamentos;
        } finally {
            closeResources(ps, rs);
        }
    }

    private Venda mapResultSetToVenda(ResultSet rs) throws SQLException {
        Venda venda = new Venda();
        venda.setId(rs.getLong("ven_id"));
        venda.setDataVenda(rs.getDate("ven_data").toLocalDate());
        venda.setStatus(rs.getString("ven_status"));
        venda.setFrete(rs.getDouble("ven_frete"));
        venda.setValorTotal(rs.getDouble("ven_valor_total"));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cli_id"));
        cliente.setNomeCompleto(rs.getString("cli_nome"));
        cliente.setEnderecos(enderecoDAO.getEnderecosByClienteId(cliente.getId()));

        venda.setCliente(cliente);
        venda.setPagamentos(getPagamentosByVendaId(venda.getId()));

        return venda;
    }
}