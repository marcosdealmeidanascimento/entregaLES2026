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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Cartao;
import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.Endereco;
import com.cecilia.gigi.dominio.EntidadeDominio;

public class ClienteDAO implements IDAO {

    private final Connection connection;
    private final EnderecoDAO enderecoDAO;
    private final CartaoDAO cartaoDAO;

    public ClienteDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.enderecoDAO = new EnderecoDAO();
        this.cartaoDAO = new CartaoDAO();
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
        String sql = "SELECT * FROM cli_cliente WHERE cli_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Cliente cliente = mapResultSetToCliente(rs);
                cliente.setEnderecos(enderecoDAO.getEnderecosByClienteId(cliente.getId()));
                cliente.setCartoes(cartaoDAO.getCartoesByClienteId(cliente.getId()));
                return cliente;
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT * FROM cli_cliente LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> clientes = new ArrayList<>();

        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();

            while (rs.next()) {
                Cliente cliente = mapResultSetToCliente(rs);
                cliente.setEnderecos(enderecoDAO.getEnderecosByClienteId(cliente.getId()));
                cliente.setCartoes(cartaoDAO.getCartoesByClienteId(cliente.getId()));
                clientes.add(cliente);
            }
            return clientes;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        // Implementação do método filter para Cliente
        // ... (lógica similar à do LivroDAO) ...
        return new ArrayList<>();
    }

    @Override
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Cliente cliente = (Cliente) entidadeDominio;
        String sql = "INSERT INTO cli_cliente (cli_nome, cli_genero, cli_cpf, cli_email, cli_telefone, cli_tipo_telefone, cli_telefone_ddd, cli_data_nascimento, cli_ranking, cli_status, cli_senha) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cliente.getNomeCompleto());
            ps.setString(2, cliente.getGenero());
            ps.setString(3, cliente.getCpf());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefone());
            ps.setString(6, cliente.getTipoTelefone());
            ps.setString(7, cliente.getTelefoneDDD());
            ps.setDate(8, Date.valueOf(cliente.getDataNascimento()));

            // Lógica para lidar com o ranking opcional
            if (cliente.getRanking() != null) {
                ps.setLong(9, cliente.getRanking());
            } else {
                ps.setNull(9, Types.BIGINT);
            }

            ps.setBoolean(10, true);
            ps.setString(11, cliente.getSenha());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                cliente.setId(rs.getLong(1));
                saveEnderecos(cliente.getId(), cliente.getEnderecos());
                saveCartoes(cliente.getId(), cliente.getCartoes());
            }

            connection.commit();
            return cliente;
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
        Cliente cliente = (Cliente) entidadeDominio;
        String sql = "UPDATE cli_cliente SET cli_nome=?, cli_genero=?, cli_cpf=?, cli_email=?, cli_telefone=?, cli_tipo_telefone=?, cli_telefone_ddd=?, cli_data_nascimento=?, cli_ranking=?, cli_status=?, cli_senha=? WHERE cli_id=?";
        PreparedStatement ps = null;

        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            ps.setString(1, cliente.getNomeCompleto());
            ps.setString(2, cliente.getGenero());
            ps.setString(3, cliente.getCpf());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefone());
            ps.setString(6, cliente.getTipoTelefone());
            ps.setString(7, cliente.getTelefoneDDD());
            ps.setDate(8, Date.valueOf(cliente.getDataNascimento()));

            // Lógica para lidar com o ranking opcional
            if (cliente.getRanking() != null) {
                ps.setLong(9, cliente.getRanking());
            } else {
                ps.setNull(9, Types.BIGINT);
            }

            ps.setBoolean(10, cliente.isStatus());
            ps.setString(11, cliente.getSenha());
            ps.setLong(12, id);
            ps.executeUpdate();

            cliente.setId(id);

            connection.commit();
            return cliente;
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
        String sql = "DELETE FROM cli_cliente WHERE cli_id = ?";
        PreparedStatement ps = null;
        try {
            connection.setAutoCommit(false);
            enderecoDAO.deleteEnderecosByClienteId(id);
            cartaoDAO.deleteCartoesByClienteId(id);
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
            connection.commit();
            return null;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(ps, null);
        }
    }

    // Funções auxiliares de rastreio
    public EntidadeDominio getClienteByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM cli_cliente WHERE cli_email = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCliente(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    public EntidadeDominio getClienteByCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM cli_cliente WHERE cli_cpf = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, cpf);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCliente(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    public EntidadeDominio login(EntidadeDominio entidade) throws SQLException {
        Cliente cliParam = (Cliente) entidade;
        String sql = "SELECT * FROM cli_cliente WHERE cli_email = ? AND cli_senha = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, cliParam.getEmail());
            ps.setString(2, cliParam.getSenha());
            rs = ps.executeQuery();

            if (rs.next()) {
                Cliente logged = mapResultSetToCliente(rs);

                List<Endereco> listaEnderecos = enderecoDAO.getEnderecosByClienteId(logged.getId());
                logged.setEnderecos(listaEnderecos);

                List<Cartao> listaCartoes = cartaoDAO.getCartoesByClienteId(logged.getId());
                logged.setCartoes(cartaoDAO.getCartoesByClienteId(logged.getId()));

                return logged;
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    private void saveEnderecos(Long clienteId, List<Endereco> enderecos) throws SQLException {
        if (enderecos == null || enderecos.isEmpty()) return;
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

    private void saveCartoes(Long clienteId, List<Cartao> cartoes) throws SQLException {
        if (cartoes == null || cartoes.isEmpty()) return;
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

    // Funções auxiliares
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cli_id"));
        cliente.setNomeCompleto(rs.getString("cli_nome"));
        cliente.setGenero(rs.getString("cli_genero"));
        cliente.setCpf(rs.getString("cli_cpf"));
        cliente.setEmail(rs.getString("cli_email"));
        cliente.setTelefone(rs.getString("cli_telefone"));
        cliente.setTipoTelefone(rs.getString("cli_tipo_telefone"));
        cliente.setTelefoneDDD(rs.getString("cli_telefone_ddd"));
        cliente.setDataNascimento(rs.getDate("cli_data_nascimento").toLocalDate());
        cliente.setSenha(rs.getString("cli_senha"));

        long rankingValue = rs.getLong("cli_ranking");
        if (rs.wasNull()) {
            cliente.setRanking(null);
        } else {
            cliente.setRanking(rankingValue);
        }

        cliente.setStatus(rs.getBoolean("cli_status"));
        return cliente;
    }
}