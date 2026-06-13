package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cecilia.gigi.database.DatabaseConnection;

public class ChatContextDAO {

    private final Connection connection;

    public ChatContextDAO() throws SQLException {
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

    public String getCatalogoProdutos() throws SQLException {
        String sql = "SELECT pro_id, pro_nome, pro_descricao, pro_aroma, pro_ingredientes, pro_indicacao " +
                     "FROM pro_produto ORDER BY pro_nome LIMIT 30";
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sb = new StringBuilder();
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                sb.append("- ").append(rs.getString("pro_nome"));
                String aroma = rs.getString("pro_aroma");
                String indicacao = rs.getString("pro_indicacao");
                String descricao = rs.getString("pro_descricao");
                String ingredientes = rs.getString("pro_ingredientes");
                if (aroma != null && !aroma.isBlank()) sb.append(" (aroma: ").append(aroma).append(")");
                if (indicacao != null && !indicacao.isBlank()) sb.append(" — indicado para: ").append(indicacao);
                if (descricao != null && !descricao.isBlank()) sb.append(" — ").append(descricao);
                if (ingredientes != null && !ingredientes.isBlank()) sb.append(" | ingredientes: ").append(ingredientes);
                sb.append("\n");
            }
            return sb.toString();
        } finally {
            closeResources(ps, rs);
        }
    }

    public String getProdutosMaisVendidos(int limit) throws SQLException {
        String sql = "SELECT p.pro_id, p.pro_nome, p.pro_aroma, p.pro_indicacao, " +
                     "SUM(i.ven_item_quantidade) AS total_vendido, " +
                     "ROUND(AVG(a.ava_nota), 1) AS nota_media " +
                     "FROM pro_produto p " +
                     "JOIN ven_item_venda i ON p.pro_id = i.pro_id " +
                     "JOIN ven_venda v ON i.ven_id = v.ven_id " +
                     "LEFT JOIN ava_avaliacao a ON p.pro_id = a.pro_id " +
                     "WHERE v.ven_status IN ('ENTREGUE','APROVADA','EM_TRANSPORTE','TROCADO') " +
                     "GROUP BY p.pro_id, p.pro_nome, p.pro_aroma, p.pro_indicacao " +
                     "ORDER BY total_vendido DESC LIMIT ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sb = new StringBuilder();
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                sb.append("- ").append(rs.getString("pro_nome"));
                String aroma = rs.getString("pro_aroma");
                String indicacao = rs.getString("pro_indicacao");
                if (aroma != null && !aroma.isBlank()) sb.append(" (aroma: ").append(aroma).append(")");
                if (indicacao != null && !indicacao.isBlank()) sb.append(", indicação: ").append(indicacao);
                sb.append(" — ").append(rs.getInt("total_vendido")).append(" vendidos");
                double nota = rs.getDouble("nota_media");
                if (!rs.wasNull()) sb.append(", nota ").append(nota);
                sb.append("\n");
            }
            return sb.toString();
        } finally {
            closeResources(ps, rs);
        }
    }

    public String getEstoquePorProduto() throws SQLException {
        String sql = "SELECT p.pro_nome, COALESCE(SUM(e.est_quantidade), 0) AS estoque_total " +
                     "FROM pro_produto p " +
                     "LEFT JOIN est_estoque e ON e.pro_id = p.pro_id " +
                     "WHERE p.pro_ativo = TRUE " +
                     "GROUP BY p.pro_id, p.pro_nome " +
                     "ORDER BY p.pro_nome";
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sb = new StringBuilder();
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int qtd = rs.getInt("estoque_total");
                sb.append("- ").append(rs.getString("pro_nome"))
                  .append(": ").append(qtd).append(qtd == 1 ? " unidade" : " unidades")
                  .append(qtd == 0 ? " (INDISPONÍVEL)" : "").append("\n");
            }
            return sb.toString();
        } finally {
            closeResources(ps, rs);
        }
    }

    public String getProdutosCompradosByCliente(Long clienteId) throws SQLException {
        String sql = "SELECT p.pro_id, p.pro_nome, p.pro_aroma, p.pro_indicacao, " +
                     "a.ava_nota, a.ava_descricao, MAX(v.ven_id) AS ultima_venda " +
                     "FROM ven_venda v " +
                     "JOIN ven_item_venda i ON v.ven_id = i.ven_id " +
                     "JOIN pro_produto p ON i.pro_id = p.pro_id " +
                     "LEFT JOIN ava_avaliacao a ON p.pro_id = a.pro_id AND a.cli_id = v.cli_id " +
                     "WHERE v.cli_id = ? " +
                     "GROUP BY p.pro_id, p.pro_nome, p.pro_aroma, p.pro_indicacao, a.ava_nota, a.ava_descricao " +
                     "ORDER BY ultima_venda DESC LIMIT 15";
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sb = new StringBuilder();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            rs = ps.executeQuery();
            while (rs.next()) {
                sb.append("- ").append(rs.getString("pro_nome"));
                String aroma = rs.getString("pro_aroma");
                String indicacao = rs.getString("pro_indicacao");
                if (aroma != null && !aroma.isBlank()) sb.append(" (aroma: ").append(aroma).append(")");
                if (indicacao != null && !indicacao.isBlank()) sb.append(", indicação: ").append(indicacao);
                double nota = rs.getDouble("ava_nota");
                if (!rs.wasNull()) {
                    sb.append(" — avaliado com nota ").append((int) nota);
                    String desc = rs.getString("ava_descricao");
                    if (desc != null && !desc.isBlank()) sb.append(": \"").append(desc).append("\"");
                }
                sb.append("\n");
            }
            return sb.toString();
        } finally {
            closeResources(ps, rs);
        }
    }
}
