package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Categoria;

public class CategoriaDAO {

    private final Connection connection;

    public CategoriaDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Categoria> getAll() throws SQLException {
        String sql = "SELECT cat_id, cat_nome, cat_descricao FROM cat_categoria ORDER BY cat_nome";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Categoria> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getLong("cat_id"));
                c.setNome(rs.getString("cat_nome"));
                c.setDescricao(rs.getString("cat_descricao"));
                lista.add(c);
            }
            return lista;
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
