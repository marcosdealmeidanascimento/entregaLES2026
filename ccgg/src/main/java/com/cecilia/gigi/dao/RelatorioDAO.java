package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cecilia.gigi.database.DatabaseConnection;

public class RelatorioDAO {

    private final Connection connection;

    public RelatorioDAO() throws SQLException {
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

    public Map<String, Object> getDashboardStats() throws SQLException {
        String sql = "SELECT " +
            "COALESCE(SUM(CASE WHEN ven_data >= DATE_FORMAT(NOW(),'%Y-%m-01') " +
            "  THEN ven_valor_total END), 0) AS receita_atual, " +
            "COALESCE(SUM(CASE WHEN ven_data >= DATE_FORMAT(NOW() - INTERVAL 1 MONTH,'%Y-%m-01') " +
            "  AND ven_data < DATE_FORMAT(NOW(),'%Y-%m-01') " +
            "  THEN ven_valor_total END), 0) AS receita_anterior, " +
            "COUNT(CASE WHEN ven_data >= DATE_FORMAT(NOW(),'%Y-%m-01') THEN 1 END) AS pedidos_atual, " +
            "COUNT(CASE WHEN ven_data >= DATE_FORMAT(NOW() - INTERVAL 1 MONTH,'%Y-%m-01') " +
            "  AND ven_data < DATE_FORMAT(NOW(),'%Y-%m-01') THEN 1 END) AS pedidos_anterior " +
            "FROM ven_venda " +
            "WHERE ven_status IN ('ENTREGUE','APROVADA','EM_TRANSPORTE','TROCADO')";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            Map<String, Object> result = new LinkedHashMap<>();
            if (rs.next()) {
                double receitaAtual    = rs.getDouble("receita_atual");
                double receitaAnterior = rs.getDouble("receita_anterior");
                long   pedidosAtual    = rs.getLong("pedidos_atual");
                long   pedidosAnterior = rs.getLong("pedidos_anterior");

                double ticketAtual    = pedidosAtual    > 0 ? receitaAtual    / pedidosAtual    : 0;
                double ticketAnterior = pedidosAnterior > 0 ? receitaAnterior / pedidosAnterior : 0;

                result.put("receitaAtual",    round2(receitaAtual));
                result.put("receitaAnterior", round2(receitaAnterior));
                result.put("pedidosAtual",    pedidosAtual);
                result.put("pedidosAnterior", pedidosAnterior);
                result.put("ticketAtual",     round2(ticketAtual));
                result.put("ticketAnterior",  round2(ticketAnterior));
            }
            return result;
        } finally {
            closeResources(ps, rs);
        }
    }

    public Map<String, Object> getVendasDiarias(int dias) throws SQLException {
        String sql = "SELECT DATE_FORMAT(ven_data, '%d/%m') AS label, " +
                     "COALESCE(SUM(ven_valor_total), 0) AS receita " +
                     "FROM ven_venda " +
                     "WHERE ven_data >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "AND ven_status IN ('ENTREGUE','APROVADA','EM_TRANSPORTE','TROCADO') " +
                     "GROUP BY ven_data ORDER BY ven_data";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> labels = new ArrayList<>();
        List<Double> dados  = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, dias);
            rs = ps.executeQuery();
            while (rs.next()) {
                labels.add(rs.getString("label"));
                dados.add(round2(rs.getDouble("receita")));
            }
        } finally {
            closeResources(ps, rs);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("dados", dados);
        return result;
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    /**
     * Retorna dados de vendas agrupados por período (mês) e por produto ou categoria.
     * agruparPor: "PRODUTO" ou "CATEGORIA"
     */
    public Map<String, Object> getVendasPorPeriodo(LocalDate inicio, LocalDate fim, String agruparPor) throws SQLException {
        boolean porCategoria = "CATEGORIA".equalsIgnoreCase(agruparPor);

        String sql = porCategoria
                ? "SELECT c.cat_nome AS label, DATE_FORMAT(v.ven_data, '%Y-%m') AS periodo, " +
                  "SUM(iv.ven_item_valor_total) AS receita, SUM(iv.ven_item_quantidade) AS quantidade " +
                  "FROM ven_venda v " +
                  "JOIN ven_item_venda iv ON iv.ven_id = v.ven_id " +
                  "JOIN pro_produto p ON p.pro_id = iv.pro_id " +
                  "JOIN cat_categoria c ON c.cat_id = p.cat_id " +
                  "WHERE v.ven_data BETWEEN ? AND ? " +
                  "AND v.ven_status IN ('ENTREGUE','APROVADA','EM_TRANSPORTE','TROCADO') " +
                  "GROUP BY label, periodo ORDER BY periodo, label"
                : "SELECT p.pro_nome AS label, DATE_FORMAT(v.ven_data, '%Y-%m') AS periodo, " +
                  "SUM(iv.ven_item_valor_total) AS receita, SUM(iv.ven_item_quantidade) AS quantidade " +
                  "FROM ven_venda v " +
                  "JOIN ven_item_venda iv ON iv.ven_id = v.ven_id " +
                  "JOIN pro_produto p ON p.pro_id = iv.pro_id " +
                  "WHERE v.ven_data BETWEEN ? AND ? " +
                  "AND v.ven_status IN ('ENTREGUE','APROVADA','EM_TRANSPORTE','TROCADO') " +
                  "GROUP BY label, periodo ORDER BY periodo, label";

        PreparedStatement ps = null;
        ResultSet rs = null;

        // label -> periodo -> receita
        Map<String, Map<String, Double>> seriesMap = new LinkedHashMap<>();
        // label -> periodo -> quantidade
        Map<String, Map<String, Integer>> seriesQtdMap = new LinkedHashMap<>();
        Set<String> periodosSet = new LinkedHashSet<>();

        double totalReceita = 0;
        int totalQuantidade = 0;

        try {
            ps = connection.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            rs = ps.executeQuery();

            while (rs.next()) {
                String label = rs.getString("label");
                String periodo = rs.getString("periodo");
                double receita = rs.getDouble("receita");
                int quantidade = rs.getInt("quantidade");

                periodosSet.add(periodo);
                seriesMap.computeIfAbsent(label, k -> new LinkedHashMap<>())
                         .merge(periodo, receita, Double::sum);
                seriesQtdMap.computeIfAbsent(label, k -> new LinkedHashMap<>())
                            .merge(periodo, quantidade, Integer::sum);
                totalReceita += receita;
                totalQuantidade += quantidade;
            }
        } finally {
            closeResources(ps, rs);
        }

        List<String> labels = new ArrayList<>(periodosSet);
        List<Map<String, Object>> series = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> entry : seriesMap.entrySet()) {
            List<Double> dados = new ArrayList<>();
            List<Integer> dadosQtd = new ArrayList<>();
            Map<String, Integer> qtdPorPeriodo = seriesQtdMap.getOrDefault(entry.getKey(), java.util.Collections.emptyMap());
            for (String periodo : labels) {
                dados.add(entry.getValue().getOrDefault(periodo, 0.0));
                dadosQtd.add(qtdPorPeriodo.getOrDefault(periodo, 0));
            }
            Map<String, Object> serie = new LinkedHashMap<>();
            serie.put("nome", entry.getKey());
            serie.put("dados", dados);
            serie.put("dadosQtd", dadosQtd);
            series.add(serie);
        }

        Map<String, Object> totais = new LinkedHashMap<>();
        totais.put("totalReceita", Math.round(totalReceita * 100.0) / 100.0);
        totais.put("ticketMedio", totalQuantidade > 0
                ? Math.round((totalReceita / totalQuantidade) * 100.0) / 100.0
                : 0.0);
        totais.put("produtosVendidos", totalQuantidade);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("labels", labels);
        resultado.put("series", series);
        resultado.put("totais", totais);
        return resultado;
    }
}
