package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Devolucao;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.ItemDevolucao;
import com.cecilia.gigi.dominio.ItemVenda;
import com.cecilia.gigi.dominio.Produto;
import com.cecilia.gigi.dominio.Venda;

public class DevolucaoDAO implements IDAO {

    private final Connection connection;
    private final EstoqueDAO estoqueDAO;

    public DevolucaoDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.estoqueDAO = new EstoqueDAO();
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
        Devolucao dev = (Devolucao) entidadeDominio;
        String sqlDev = "INSERT INTO dev_devolucao (dev_venda_id, dev_status, dev_data_solicitacao) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO dev_item_devolucao (dit_devolucao_id, dit_item_venda_id, dit_quantidade) VALUES (?, ?, ?)";

        PreparedStatement psDev = null;
        PreparedStatement psItem = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);

            psDev = connection.prepareStatement(sqlDev, Statement.RETURN_GENERATED_KEYS);
            psDev.setLong(1, dev.getVenda().getId());
            psDev.setString(2, dev.getStatus());
            psDev.setDate(3, Date.valueOf(dev.getDataSolicitacao() != null ? dev.getDataSolicitacao() : LocalDate.now()));
            psDev.executeUpdate();

            rs = psDev.getGeneratedKeys();
            if (rs.next()) dev.setId(rs.getLong(1));

            psItem = connection.prepareStatement(sqlItem);
            for (ItemDevolucao item : dev.getItens()) {
                psItem.setLong(1, dev.getId());
                psItem.setLong(2, item.getItemVenda().getId());
                psItem.setInt(3, item.getQuantidade());
                psItem.addBatch();
            }
            psItem.executeBatch();

            connection.commit();
            return dev;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(psItem, null);
            closeResources(psDev, rs);
        }
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        String sql = "SELECT * FROM dev_devolucao WHERE dev_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                Devolucao dev = mapResultSetToDevolucao(rs);
                dev.setItens(getItensByDevolucaoId(dev.getId()));
                return dev;
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT * FROM dev_devolucao ORDER BY dev_id DESC LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                Devolucao dev = mapResultSetToDevolucao(rs);
                dev.setItens(getItensByDevolucaoId(dev.getId()));
                lista.add(dev);
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

    public List<Devolucao> getByVendaId(Long vendaId) throws SQLException {
        String sql = "SELECT * FROM dev_devolucao WHERE dev_venda_id = ? ORDER BY dev_id DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Devolucao> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, vendaId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Devolucao dev = mapResultSetToDevolucao(rs);
                dev.setItens(getItensByDevolucaoId(dev.getId()));
                lista.add(dev);
            }
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    public void updateStatus(Long id, String novoStatus) throws SQLException {
        String sql = "UPDATE dev_devolucao SET dev_status = ? WHERE dev_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, novoStatus);
            ps.setLong(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("{\"error\": \"Devolução não encontrada\", \"status\": 404}");
        } finally {
            closeResources(ps, null);
        }
    }

    public void reentradaItens(Long devolucaoId) throws SQLException {
        List<ItemDevolucao> itens = getItensByDevolucaoId(devolucaoId);
        for (ItemDevolucao item : itens) {
            estoqueDAO.reentrada(item.getItemVenda().getProduto().getId(), item.getQuantidade());
        }
    }

    @Override
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        return null;
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        return null;
    }

    private List<ItemDevolucao> getItensByDevolucaoId(Long devolucaoId) throws SQLException {
        String sql = "SELECT d.*, iv.pro_id, p.pro_nome, p.pro_foto " +
                     "FROM dev_item_devolucao d " +
                     "JOIN ven_item_venda iv ON d.dit_item_venda_id = iv.ven_item_id " +
                     "JOIN pro_produto p ON iv.pro_id = p.pro_id " +
                     "WHERE d.dit_devolucao_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ItemDevolucao> itens = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, devolucaoId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemDevolucao item = new ItemDevolucao();
                item.setId(rs.getLong("dit_id"));
                item.setQuantidade(rs.getInt("dit_quantidade"));

                ItemVenda iv = new ItemVenda();
                iv.setId(rs.getLong("dit_item_venda_id"));
                Produto produto = new Produto();
                produto.setId(rs.getLong("pro_id"));
                produto.setNome(rs.getString("pro_nome"));
                produto.setFoto(rs.getString("pro_foto"));
                iv.setProduto(produto);
                item.setItemVenda(iv);

                itens.add(item);
            }
            return itens;
        } finally {
            closeResources(ps, rs);
        }
    }

    public Map<String, Object> getClienteEValorByDevolucao(Long devolucaoId) throws SQLException {
        String sql = "SELECT v.cli_id, " +
                     "COALESCE(SUM(d.dit_quantidade * iv.ven_item_valor_unitario), 0) AS valor_total " +
                     "FROM dev_devolucao dd " +
                     "JOIN dev_item_devolucao d ON d.dit_devolucao_id = dd.dev_id " +
                     "JOIN ven_item_venda iv ON iv.ven_item_id = d.dit_item_venda_id " +
                     "JOIN ven_venda v ON v.ven_id = dd.dev_venda_id " +
                     "WHERE dd.dev_id = ? " +
                     "GROUP BY v.cli_id";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, devolucaoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("clienteId", rs.getLong("cli_id"));
                result.put("valorTotal", rs.getDouble("valor_total"));
                return result;
            }
            throw new SQLException("{\"error\": \"Devolução não encontrada\", \"status\": 404}");
        } finally {
            closeResources(ps, rs);
        }
    }

    private Devolucao mapResultSetToDevolucao(ResultSet rs) throws SQLException {
        Devolucao dev = new Devolucao();
        dev.setId(rs.getLong("dev_id"));
        dev.setStatus(rs.getString("dev_status"));
        dev.setDataSolicitacao(rs.getDate("dev_data_solicitacao").toLocalDate());
        Venda venda = new Venda();
        venda.setId(rs.getLong("dev_venda_id"));
        dev.setVenda(venda);
        return dev;
    }
}
