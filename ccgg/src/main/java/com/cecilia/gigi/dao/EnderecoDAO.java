/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Endereco;
import com.cecilia.gigi.dominio.EntidadeDominio;

public class EnderecoDAO implements IDAO {

    private final Connection connection;

    public EnderecoDAO() throws SQLException {
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
        String sql = "SELECT * FROM end_endereco WHERE end_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToEndereco(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        // Método getAll para Enderecos, se necessário
        return new ArrayList<>();
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        // Implementação do método filter para Endereco
        // ...
        return new ArrayList<>();
    }

    @Override
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Endereco endereco = (Endereco) entidadeDominio;
        String sql = "INSERT INTO end_endereco (end_cep, end_tipo_residencia, end_logradouro, end_tipo_logradouro, end_numero, end_bairro, end_cidade, end_estado, end_pais, end_complemento, end_apelido, end_observacao, end_favorito, end_cobranca, cli_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, endereco.getCep());
            ps.setString(2, endereco.getTipoResidencia());
            ps.setString(3, endereco.getLogradouro());
            ps.setString(4, endereco.getTipoLogradouro());
            ps.setString(5, endereco.getNumero());
            ps.setString(6, endereco.getBairro());
            ps.setString(7, endereco.getCidade());
            ps.setString(8, endereco.getEstado());
            ps.setString(9, endereco.getPais());
            ps.setString(10, endereco.getComplemento());
            ps.setString(11, endereco.getApelidoEndereco());
            ps.setString(12, endereco.getObservacao());
            ps.setBoolean(13, endereco.isFavorito());
            ps.setBoolean(14, endereco.isCobranca());
            ps.setLong(15, endereco.getCliente().getId());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                endereco.setId(rs.getLong(1));
            }

            return endereco;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Endereco endereco = (Endereco) entidadeDominio;
        String sql = "UPDATE end_endereco SET end_cep=?, end_tipo_residencia=?, end_logradouro=?, end_tipo_logradouro=?, end_numero=?, end_bairro=?, end_cidade=?, end_estado=?, end_pais=?, end_complemento=?, end_apelido=?, end_observacao=?, end_favorito=?, end_cobranca=? WHERE end_id=?";
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, endereco.getCep());
            ps.setString(2, endereco.getTipoResidencia());
            ps.setString(3, endereco.getLogradouro());
            ps.setString(4, endereco.getTipoLogradouro());
            ps.setString(5, endereco.getNumero());
            ps.setString(6, endereco.getBairro());
            ps.setString(7, endereco.getCidade());
            ps.setString(8, endereco.getEstado());
            ps.setString(9, endereco.getPais());
            ps.setString(10, endereco.getComplemento());
            ps.setString(11, endereco.getApelidoEndereco());
            ps.setString(12, endereco.getObservacao());
            ps.setBoolean(13, endereco.isFavorito());
            ps.setBoolean(14, endereco.isCobranca());
            ps.setLong(15, id);
            ps.executeUpdate();
            endereco.setId((long) id);
            return endereco;
        } finally {
            closeResources(ps, null);
        }
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        String sql = "DELETE FROM end_endereco WHERE end_id = ?";
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
    public List<Endereco> getEnderecosByClienteId(Long clienteId) throws SQLException {
        List<Endereco> enderecos = new ArrayList<>();
        String sql = "SELECT * FROM end_endereco WHERE cli_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            rs = ps.executeQuery();
            while (rs.next()) {
                enderecos.add(mapResultSetToEndereco(rs));
            }
            return enderecos;
        } finally {
            closeResources(ps, rs);
        }
    }

    public void saveEnderecosByClienteId(Long clienteId, List<Endereco> enderecos) throws SQLException {
        if (enderecos == null || enderecos.isEmpty())
            return;
        String sql = "INSERT INTO end_endereco (end_cep, end_tipo_residencia, end_logradouro, end_tipo_logradouro, end_numero, end_bairro, end_cidade, end_estado, end_pais, end_complemento, end_apelido, end_observacao, end_favorito, end_cobranca, cli_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            for (Endereco endereco : enderecos) {
                ps.setString(1, endereco.getCep());
                ps.setString(2, endereco.getTipoResidencia());
                ps.setString(3, endereco.getLogradouro());
                ps.setString(4, endereco.getTipoLogradouro());
                ps.setString(5, endereco.getNumero());
                ps.setString(6, endereco.getBairro());
                ps.setString(7, endereco.getCidade());
                ps.setString(8, endereco.getEstado());
                ps.setString(9, endereco.getPais());
                ps.setString(10, endereco.getComplemento());
                ps.setString(11, endereco.getApelidoEndereco());
                ps.setString(12, endereco.getObservacao());
                ps.setBoolean(13, endereco.isFavorito());
                ps.setBoolean(14, endereco.isCobranca());
                ps.setLong(15, clienteId);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            closeResources(ps, null);
        }
    }

    public void deleteEnderecosByClienteId(Long clienteId) throws SQLException {
        String sql = "DELETE FROM end_endereco WHERE cli_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            ps.executeUpdate();
        } finally {
            closeResources(ps, null);
        }
    }

    private Endereco mapResultSetToEndereco(ResultSet rs) throws SQLException {
        Endereco endereco = new Endereco();
        endereco.setId(rs.getLong("end_id"));
        endereco.setCep(rs.getString("end_cep"));
        endereco.setTipoResidencia(rs.getString("end_tipo_residencia"));
        endereco.setLogradouro(rs.getString("end_logradouro"));
        endereco.setTipoLogradouro(rs.getString("end_tipo_logradouro"));
        endereco.setNumero(rs.getString("end_numero"));
        endereco.setBairro(rs.getString("end_bairro"));
        endereco.setCidade(rs.getString("end_cidade"));
        endereco.setEstado(rs.getString("end_estado"));
        endereco.setPais(rs.getString("end_pais"));
        endereco.setComplemento(rs.getString("end_complemento"));
        endereco.setApelidoEndereco(rs.getString("end_apelido"));
        endereco.setObservacao(rs.getString("end_observacao"));
        endereco.setFavorito(rs.getBoolean("end_favorito"));
        endereco.setCobranca(rs.getBoolean("end_cobranca"));
        return endereco;
    }
}