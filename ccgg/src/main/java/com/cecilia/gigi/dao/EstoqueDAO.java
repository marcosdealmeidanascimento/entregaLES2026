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
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.Estoque;
import com.cecilia.gigi.dominio.Produto;

public class EstoqueDAO implements IDAO {

    private final Connection connection;

    public EstoqueDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
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
    public EntidadeDominio get(Long id) throws SQLException {
        String sql = "SELECT e.*, p.pro_nome, p.pro_descricao, p.pro_foto FROM est_estoque e " +
                     "JOIN pro_produto p ON e.pro_id = p.pro_id WHERE e.est_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToEstoque(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT e.*, p.pro_nome, p.pro_descricao, p.pro_foto FROM est_estoque e " +
                     "JOIN pro_produto p ON e.pro_id = p.pro_id LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToEstoque(rs));
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

    @Override
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Estoque estoque = (Estoque) entidadeDominio;

        // RF0052 / RN005x: usar maior valorCusto do produto para calcular valorVenda
        Double maiorCusto = getMaxValorCustoByProduto(estoque.getProduto().getId());
        if (maiorCusto == null || estoque.getValorCusto() > maiorCusto) {
            maiorCusto = estoque.getValorCusto();
            Double novoValorVenda = calcularValorVenda(maiorCusto, estoque.getProduto().getId());
            atualizarValorVendaByProduto(estoque.getProduto().getId(), novoValorVenda);
            estoque.setValorVenda(novoValorVenda);
        } else {
            estoque.setValorVenda(calcularValorVenda(maiorCusto, estoque.getProduto().getId()));
        }

        String sql = "INSERT INTO est_estoque (pro_id, est_quantidade, est_valor_custo, est_valor_venda, est_fornecedor, est_data_entrada) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, estoque.getProduto().getId());
            ps.setInt(2, estoque.getQuantidade());
            ps.setDouble(3, estoque.getValorCusto());
            ps.setDouble(4, estoque.getValorVenda());
            ps.setString(5, estoque.getFornecedor());
            ps.setDate(6, Date.valueOf(estoque.getDataEntrada()));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                estoque.setId(rs.getLong(1));
            }
            connection.commit();
            return estoque;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(ps, rs);
        }
    }

    @Override
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Estoque estoque = (Estoque) entidadeDominio;
        String sql = "UPDATE est_estoque SET pro_id=?, est_quantidade=?, est_valor_custo=?, est_valor_venda=?, est_fornecedor=?, est_data_entrada=? WHERE est_id=?";
        PreparedStatement ps = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            ps.setLong(1, estoque.getProduto().getId());
            ps.setInt(2, estoque.getQuantidade());
            ps.setDouble(3, estoque.getValorCusto());
            ps.setDouble(4, estoque.getValorVenda());
            ps.setString(5, estoque.getFornecedor());
            ps.setDate(6, Date.valueOf(estoque.getDataEntrada()));
            ps.setLong(7, id);
            ps.executeUpdate();
            estoque.setId(id);
            connection.commit();
            return estoque;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(ps, null);
        }
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        String sql = "DELETE FROM est_estoque WHERE est_id=?";
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

    // RF0053: Dar baixa em estoque (FIFO)
    public void darBaixa(Long produtoId, Integer quantidade) throws SQLException {
        String selectSql = "SELECT est_id, est_quantidade FROM est_estoque WHERE pro_id = ? AND est_quantidade > 0 ORDER BY est_data_entrada ASC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(selectSql);
            ps.setLong(1, produtoId);
            rs = ps.executeQuery();

            int restante = quantidade;
            while (rs.next() && restante > 0) {
                long estoqueId = rs.getLong("est_id");
                int qtdDisponivel = rs.getInt("est_quantidade");
                int qtdConsumida = Math.min(qtdDisponivel, restante);
                atualizarQuantidade(estoqueId, qtdDisponivel - qtdConsumida);
                restante -= qtdConsumida;
            }

            if (restante > 0) {
                connection.rollback();
                throw new SQLException("{\"error\": \"Estoque insuficiente\", \"status\": 422}");
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(ps, rs);
        }
    }

    // RF0054: Reentrada em estoque
    public void reentrada(Long produtoId, Integer quantidade) throws SQLException {
        String findSql = "SELECT est_id, est_quantidade FROM est_estoque WHERE pro_id = ? ORDER BY est_data_entrada DESC LIMIT 1";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(findSql);
            ps.setLong(1, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                long estoqueId = rs.getLong("est_id");
                int qtdAtual = rs.getInt("est_quantidade");
                atualizarQuantidade(estoqueId, qtdAtual + quantidade);
            } else {
                throw new SQLException("{\"error\": \"Nenhum registro de estoque encontrado para o produto\", \"status\": 404}");
            }
        } finally {
            closeResources(ps, rs);
        }
    }

    public Integer getQuantidadeDisponivelByProduto(Long produtoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(est_quantidade), 0) FROM est_estoque WHERE pro_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(ps, rs);
        }
    }

    // RF0052: valorVenda = valorCusto * (1 + percentualMargem / 100)
    private Double calcularValorVenda(Double valorCusto, Long produtoId) throws SQLException {
        String sql = "SELECT g.grp_percentual_margem FROM grp_grupo_precificacao g " +
                     "JOIN pro_produto p ON p.grp_id = g.grp_id WHERE p.pro_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                double percentual = rs.getDouble(1);
                return valorCusto * (1 + percentual / 100.0);
            }
            return valorCusto;
        } finally {
            closeResources(ps, rs);
        }
    }

    private Double getMaxValorCustoByProduto(Long produtoId) throws SQLException {
        String sql = "SELECT MAX(est_valor_custo) FROM est_estoque WHERE pro_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                double val = rs.getDouble(1);
                return rs.wasNull() ? null : val;
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    private void atualizarValorVendaByProduto(Long produtoId, Double novoValorVenda) throws SQLException {
        String sql = "UPDATE est_estoque SET est_valor_venda = ? WHERE pro_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setDouble(1, novoValorVenda);
            ps.setLong(2, produtoId);
            ps.executeUpdate();
        } finally {
            closeResources(ps, null);
        }
    }

    private void atualizarQuantidade(Long estoqueId, int novaQuantidade) throws SQLException {
        String sql = "UPDATE est_estoque SET est_quantidade = ? WHERE est_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, novaQuantidade);
            ps.setLong(2, estoqueId);
            ps.executeUpdate();
        } finally {
            closeResources(ps, null);
        }
    }

    private Estoque mapResultSetToEstoque(ResultSet rs) throws SQLException {
        Estoque estoque = new Estoque();
        estoque.setId(rs.getLong("est_id"));
        estoque.setQuantidade(rs.getInt("est_quantidade"));
        estoque.setValorCusto(rs.getDouble("est_valor_custo"));
        estoque.setValorVenda(rs.getDouble("est_valor_venda"));
        estoque.setFornecedor(rs.getString("est_fornecedor"));
        estoque.setDataEntrada(rs.getDate("est_data_entrada").toLocalDate());

        Produto produto = new Produto();
        produto.setId(rs.getLong("pro_id"));
        produto.setNome(rs.getString("pro_nome"));
        produto.setDescricao(rs.getString("pro_descricao"));
        produto.setFoto(rs.getString("pro_foto"));
        estoque.setProduto(produto);

        return estoque;
    }
}
