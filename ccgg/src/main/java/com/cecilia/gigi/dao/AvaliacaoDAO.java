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
import com.cecilia.gigi.dominio.Avaliacao;
import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.Produto;

public class AvaliacaoDAO implements IDAO {

    private final Connection connection;

    public AvaliacaoDAO() throws SQLException {
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
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Avaliacao avaliacao = (Avaliacao) entidadeDominio;
        String sql = "INSERT INTO ava_avaliacao (cli_id, pro_id, ava_nota, ava_descricao, ava_data_avaliacao) VALUES (?,?,?,?,?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, avaliacao.getCliente().getId());
            ps.setLong(2, avaliacao.getProduto().getId());
            ps.setInt(3, avaliacao.getNota());
            ps.setString(4, avaliacao.getDescricao());
            ps.setDate(5, Date.valueOf(avaliacao.getDataAvaliacao()));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                avaliacao.setId(rs.getLong(1));
            }

            connection.commit();
            return avaliacao;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            closeResources(ps, rs);
        }
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        String sql = "SELECT a.*, c.cli_nome, p.pro_nome, p.pro_foto FROM ava_avaliacao a " +
                     "JOIN cli_cliente c ON a.cli_id = c.cli_id " +
                     "JOIN pro_produto p ON a.pro_id = p.pro_id " +
                     "WHERE a.ava_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToAvaliacao(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT a.*, c.cli_nome, p.pro_nome, p.pro_foto FROM ava_avaliacao a " +
                     "JOIN cli_cliente c ON a.cli_id = c.cli_id " +
                     "JOIN pro_produto p ON a.pro_id = p.pro_id " +
                     "ORDER BY a.ava_id DESC LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToAvaliacao(rs));
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
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Avaliacao avaliacao = (Avaliacao) entidadeDominio;
        String sql = "UPDATE ava_avaliacao SET ava_nota=?, ava_descricao=?, ava_data_alteracao=? WHERE ava_id=?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, avaliacao.getNota());
            ps.setString(2, avaliacao.getDescricao());
            ps.setDate(3, Date.valueOf(java.time.LocalDate.now()));
            ps.setLong(4, id);
            ps.executeUpdate();
            return avaliacao;
        } finally {
            closeResources(ps, null);
        }
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        String sql = "DELETE FROM ava_avaliacao WHERE ava_id=?";
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

    public List<EntidadeDominio> getByProdutoId(Long produtoId, Long limit, Long offset) throws SQLException {
        String sql = "SELECT a.*, c.cli_nome, p.pro_nome, p.pro_foto FROM ava_avaliacao a " +
                     "JOIN cli_cliente c ON a.cli_id = c.cli_id " +
                     "JOIN pro_produto p ON a.pro_id = p.pro_id " +
                     "WHERE a.pro_id = ? ORDER BY a.ava_id DESC LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, produtoId);
            ps.setLong(2, limit);
            ps.setLong(3, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToAvaliacao(rs));
            }
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    public List<EntidadeDominio> getByClienteId(Long clienteId, Long limit, Long offset) throws SQLException {
        String sql = "SELECT a.*, c.cli_nome, p.pro_nome, p.pro_foto FROM ava_avaliacao a " +
                     "JOIN cli_cliente c ON a.cli_id = c.cli_id " +
                     "JOIN pro_produto p ON a.pro_id = p.pro_id " +
                     "WHERE a.cli_id = ? ORDER BY a.ava_id DESC LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            ps.setLong(2, limit);
            ps.setLong(3, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToAvaliacao(rs));
            }
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    public Boolean clienteElegivel(Long clienteId, Long produtoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ven_venda v " +
                     "JOIN ven_item_venda i ON v.ven_id = i.ven_id " +
                     "WHERE v.cli_id = ? AND i.pro_id = ? AND v.ven_status = 'ENTREGUE'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            ps.setLong(2, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(ps, rs);
        }
    }

    public Boolean existeAvaliacao(Long clienteId, Long produtoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ava_avaliacao WHERE cli_id = ? AND pro_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            ps.setLong(2, produtoId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(ps, rs);
        }
    }

    private Avaliacao mapResultSetToAvaliacao(ResultSet rs) throws SQLException {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(rs.getLong("ava_id"));
        avaliacao.setNota(rs.getInt("ava_nota"));
        avaliacao.setDescricao(rs.getString("ava_descricao"));
        avaliacao.setDataAvaliacao(rs.getDate("ava_data_avaliacao").toLocalDate());

        Date dataAlteracao = rs.getDate("ava_data_alteracao");
        if (dataAlteracao != null) {
            avaliacao.setDataUltimaAlteracao(dataAlteracao.toLocalDate());
        }

        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cli_id"));
        cliente.setNomeCompleto(rs.getString("cli_nome"));
        avaliacao.setCliente(cliente);

        Produto produto = new Produto();
        produto.setId(rs.getLong("pro_id"));
        produto.setNome(rs.getString("pro_nome"));
        produto.setFoto(rs.getString("pro_foto"));
        avaliacao.setProduto(produto);

        return avaliacao;
    }
}
