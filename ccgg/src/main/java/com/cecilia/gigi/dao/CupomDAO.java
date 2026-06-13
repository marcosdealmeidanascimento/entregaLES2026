package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.EntidadeDominio;

public class CupomDAO implements IDAO {

    private final Connection connection;

    public CupomDAO() throws SQLException {
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
        String sql = "SELECT * FROM cup_cupom WHERE cup_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToCupom(rs);
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT * FROM cup_cupom LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) lista.add(mapResultSetToCupom(rs));
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return new ArrayList<>();
    }

    public Cupom findByCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM cup_cupom WHERE cup_codigo = ? AND cup_ativo = TRUE";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, codigo);
            rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToCupom(rs);
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    public List<Cupom> findDisponivelByCliente(Long clienteId) throws SQLException {
        String sql = "SELECT * FROM cup_cupom WHERE cup_ativo = TRUE AND (cup_cliente_id IS NULL OR cup_cliente_id = ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Cupom> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, clienteId);
            rs = ps.executeQuery();
            while (rs.next()) lista.add(mapResultSetToCupom(rs));
            return lista;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException {
        Cupom cupom = (Cupom) entidadeDominio;
        String sql = "INSERT INTO cup_cupom (cup_codigo, cup_valor, cup_tipo, cup_ativo, cup_eh_troca, cup_cliente_id) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cupom.getCodigo());
            ps.setDouble(2, cupom.getValor());
            ps.setString(3, cupom.getTipo());
            ps.setBoolean(4, cupom.isAtivo());
            ps.setBoolean(5, cupom.isEhTroca());
            if (cupom.getClienteId() != null) ps.setLong(6, cupom.getClienteId());
            else ps.setNull(6, Types.BIGINT);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) cupom.setId(rs.getLong(1));
            return cupom;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Cupom cupom = (Cupom) entidadeDominio;
        String sql = "UPDATE cup_cupom SET cup_codigo=?, cup_valor=?, cup_tipo=?, cup_ativo=?, cup_eh_troca=?, cup_cliente_id=? WHERE cup_id=?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, cupom.getCodigo());
            ps.setDouble(2, cupom.getValor());
            ps.setString(3, cupom.getTipo());
            ps.setBoolean(4, cupom.isAtivo());
            ps.setBoolean(5, cupom.isEhTroca());
            if (cupom.getClienteId() != null) ps.setLong(6, cupom.getClienteId());
            else ps.setNull(6, Types.BIGINT);
            ps.setLong(7, id);
            ps.executeUpdate();
            cupom.setId(id);
            return cupom;
        } finally {
            closeResources(ps, null);
        }
    }

    @Override
    public EntidadeDominio delete(Long id) throws SQLException {
        String sql = "DELETE FROM cup_cupom WHERE cup_id = ?";
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

    public void desativar(Long id) throws SQLException {
        String sql = "UPDATE cup_cupom SET cup_ativo = FALSE WHERE cup_id = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
        } finally {
            closeResources(ps, null);
        }
    }

    private Cupom mapResultSetToCupom(ResultSet rs) throws SQLException {
        Cupom cupom = new Cupom();
        cupom.setId(rs.getLong("cup_id"));
        cupom.setCodigo(rs.getString("cup_codigo"));
        cupom.setValor(rs.getDouble("cup_valor"));
        cupom.setTipo(rs.getString("cup_tipo"));
        cupom.setAtivo(rs.getBoolean("cup_ativo"));
        cupom.setEhTroca(rs.getBoolean("cup_eh_troca"));
        long clienteId = rs.getLong("cup_cliente_id");
        cupom.setClienteId(rs.wasNull() ? null : clienteId);
        return cupom;
    }
}
