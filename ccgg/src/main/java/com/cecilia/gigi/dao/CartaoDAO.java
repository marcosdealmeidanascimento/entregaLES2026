/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
import com.cecilia.gigi.dominio.EntidadeDominio;

public class CartaoDAO implements IDAO {

    private final Connection connection;

    public CartaoDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    private void closeResources(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        String sql = "SELECT * FROM car_cartao WHERE car_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCartao(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        List<EntidadeDominio> cartoes = new ArrayList<>();
        String sql = "SELECT * FROM car_cartao LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                cartoes.add(mapResultSetToCartao(rs));
            }
            return cartoes;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        // Implementação do método filter para Cartao
        // ...
        return new ArrayList<>();
    }

    @Override
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Cartao cartao = (Cartao) entidadeDominio;
        String sql = "INSERT INTO car_cartao (car_numero, car_nome_impresso, car_cvv, car_bandeira, car_validade, car_apelido, car_favorito, cli_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cartao.getNumero());
            ps.setString(2, cartao.getNomeImpresso());
            ps.setString(3, cartao.getCvv());
            ps.setString(4, cartao.getBandeira());
            ps.setDate(5, Date.valueOf(cartao.getValidade()));
            ps.setString(6, cartao.getApelidoCartao());
            ps.setBoolean(7, cartao.isFavorito());
            ps.setLong(8, cartao.getCliente().getId());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                cartao.setId(rs.getLong(1));
            }

            return cartao;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Cartao cartao = (Cartao) entidadeDominio;
        String sql = "UPDATE car_cartao SET car_numero=?, car_nome_impresso=?, car_cvv=?, car_bandeira=?, car_validade=?, car_apelido=?, car_favorito=? WHERE car_id=?";
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, cartao.getNumero());
            ps.setString(2, cartao.getNomeImpresso());
            ps.setString(3, cartao.getCvv());
            ps.setString(4, cartao.getBandeira());
            ps.setDate(5, Date.valueOf(cartao.getValidade()));
            ps.setString(6, cartao.getApelidoCartao());
            ps.setBoolean(7, cartao.isFavorito());
            ps.setLong(8, id);
            ps.executeUpdate();
            cartao.setId((long) id);
            return cartao;
        } finally {
            closeResources(ps, null);
        }
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        String sql = "DELETE FROM car_cartao WHERE car_id = ?";
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

    // Métodos auxiliares para uso em ClienteDAO
    public List<Cartao> getCartoesByClienteId(Long clienteId) throws SQLException {
        List<Cartao> cartoes = new ArrayList<>();
        String sql = "SELECT * FROM car_cartao WHERE cli_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            rs = ps.executeQuery();
            while (rs.next()) {
                cartoes.add(mapResultSetToCartao(rs));
            }
            return cartoes;
        } finally {
            closeResources(ps, rs);
        }
    }

    public void saveCartoesByClienteId(Long clienteId, List<Cartao> cartoes) throws SQLException {
        if (cartoes == null || cartoes.isEmpty())
            return;
        String sql = "INSERT INTO car_cartao (car_numero, car_nome_impresso, car_cvv, car_bandeira, car_validade, car_apelido, car_favorito, cli_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            for (Cartao cartao : cartoes) {
                ps.setString(1, cartao.getNumero());
                ps.setString(2, cartao.getNomeImpresso());
                ps.setString(3, cartao.getCvv());
                ps.setString(4, cartao.getBandeira());
                ps.setDate(5, Date.valueOf(cartao.getValidade()));
                ps.setString(6, cartao.getApelidoCartao());
                ps.setBoolean(7, cartao.isFavorito());
                ps.setLong(8, clienteId);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            closeResources(ps, null);
        }
    }

    public void deleteCartoesByClienteId(Long clienteId) throws SQLException {
        String sql = "DELETE FROM car_cartao WHERE cli_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            ps.executeUpdate();
        } finally {
            closeResources(ps, null);
        }
    }

    private Cartao mapResultSetToCartao(ResultSet rs) throws SQLException {
        Cartao cartao = new Cartao();
        cartao.setId(rs.getLong("car_id"));
        cartao.setNumero(rs.getString("car_numero"));
        cartao.setNomeImpresso(rs.getString("car_nome_impresso"));
        cartao.setCvv(rs.getString("car_cvv"));
        cartao.setBandeira(rs.getString("car_bandeira"));
        cartao.setValidade(rs.getDate("car_validade").toLocalDate());
        cartao.setApelidoCartao(rs.getString("car_apelido"));
        cartao.setFavorito(rs.getBoolean("car_favorito"));
        return cartao;
    }
}