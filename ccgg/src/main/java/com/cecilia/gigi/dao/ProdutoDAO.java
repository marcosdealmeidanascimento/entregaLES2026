package com.cecilia.gigi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cecilia.gigi.database.DatabaseConnection;
import com.cecilia.gigi.dominio.Categoria;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.GrupoPrecificacao;
import com.cecilia.gigi.dominio.Produto;

public class ProdutoDAO implements IDAO {

    private final Connection connection;

    public ProdutoDAO() throws SQLException {
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
        String sql = "SELECT p.*, " +
                     "g.grp_nome, g.grp_piso_preco, g.grp_teto_preco, g.grp_percentual_margem, " +
                     "c.cat_nome, c.cat_descricao " +
                     "FROM pro_produto p " +
                     "LEFT JOIN grp_grupo_precificacao g ON p.grp_id = g.grp_id " +
                     "LEFT JOIN cat_categoria c ON p.cat_id = c.cat_id " +
                     "WHERE p.pro_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduto(rs);
            }
            return null;
        } finally {
            closeResources(ps, rs);
        }
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        String sql = "SELECT p.*, " +
                     "g.grp_nome, g.grp_piso_preco, g.grp_teto_preco, g.grp_percentual_margem, " +
                     "c.cat_nome, c.cat_descricao " +
                     "FROM pro_produto p " +
                     "LEFT JOIN grp_grupo_precificacao g ON p.grp_id = g.grp_id " +
                     "LEFT JOIN cat_categoria c ON p.cat_id = c.cat_id " +
                     "LIMIT ? OFFSET ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EntidadeDominio> lista = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, limit);
            ps.setLong(2, offset);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToProduto(rs));
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
        Produto produto = (Produto) entidadeDominio;
        String sql = "INSERT INTO pro_produto (pro_nome, pro_descricao, pro_peso, pro_aroma, pro_ingredientes, " +
                     "pro_indicacao, pro_foto, pro_dimensoes, pro_validade, grp_id, cat_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setObject(3, produto.getPeso());
            ps.setString(4, produto.getAroma());
            ps.setString(5, produto.getIngredientes());
            ps.setString(6, produto.getIndicacao());
            ps.setString(7, produto.getFoto());
            ps.setString(8, produto.getDimensoes());
            ps.setString(9, produto.getValidade());
            ps.setObject(10, produto.getGrupoPrecificacao() != null ? produto.getGrupoPrecificacao().getId() : null);
            ps.setObject(11, produto.getCategoria() != null ? produto.getCategoria().getId() : null);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                produto.setId(rs.getLong(1));
            }
            connection.commit();
            return produto;
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
        Produto produto = (Produto) entidadeDominio;
        String sql = "UPDATE pro_produto SET pro_nome=?, pro_descricao=?, pro_peso=?, pro_aroma=?, pro_ingredientes=?, " +
                     "pro_indicacao=?, pro_foto=?, pro_dimensoes=?, pro_validade=?, grp_id=?, cat_id=? WHERE pro_id=?";
        PreparedStatement ps = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setObject(3, produto.getPeso());
            ps.setString(4, produto.getAroma());
            ps.setString(5, produto.getIngredientes());
            ps.setString(6, produto.getIndicacao());
            ps.setString(7, produto.getFoto());
            ps.setString(8, produto.getDimensoes());
            ps.setString(9, produto.getValidade());
            ps.setObject(10, produto.getGrupoPrecificacao() != null ? produto.getGrupoPrecificacao().getId() : null);
            ps.setObject(11, produto.getCategoria() != null ? produto.getCategoria().getId() : null);
            ps.setLong(12, id);
            ps.executeUpdate();
            produto.setId(id);
            connection.commit();
            return produto;
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
        String sql = "DELETE FROM pro_produto WHERE pro_id=?";
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

    private Produto mapResultSetToProduto(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getLong("pro_id"));
        produto.setNome(rs.getString("pro_nome"));
        produto.setDescricao(rs.getString("pro_descricao"));
        produto.setPeso(rs.getObject("pro_peso", Double.class));
        produto.setAroma(rs.getString("pro_aroma"));
        produto.setIngredientes(rs.getString("pro_ingredientes"));
        produto.setIndicacao(rs.getString("pro_indicacao"));
        produto.setFoto(rs.getString("pro_foto"));
        produto.setDimensoes(rs.getString("pro_dimensoes"));
        produto.setValidade(rs.getString("pro_validade"));

        long grpId = rs.getLong("grp_id");
        if (!rs.wasNull()) {
            GrupoPrecificacao grupo = new GrupoPrecificacao();
            grupo.setId(grpId);
            grupo.setNome(rs.getString("grp_nome"));
            grupo.setPisoPreco(rs.getObject("grp_piso_preco", Double.class));
            grupo.setTetoPreco(rs.getObject("grp_teto_preco", Double.class));
            grupo.setPercentualMargem(rs.getObject("grp_percentual_margem", Double.class));
            produto.setGrupoPrecificacao(grupo);
        }

        long catId = rs.getLong("cat_id");
        if (!rs.wasNull()) {
            Categoria categoria = new Categoria();
            categoria.setId(catId);
            categoria.setNome(rs.getString("cat_nome"));
            categoria.setDescricao(rs.getString("cat_descricao"));
            produto.setCategoria(categoria);
        }

        return produto;
    }
}
