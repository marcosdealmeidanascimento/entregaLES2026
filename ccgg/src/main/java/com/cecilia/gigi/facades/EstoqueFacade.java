package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dao.EstoqueDAO;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.strategies.ValidarEstoque;

public class EstoqueFacade implements IFacade {

    private final EstoqueDAO estoqueDAO;
    private final ValidarEstoque validarEstoque;

    public EstoqueFacade() throws SQLException {
        this.estoqueDAO = new EstoqueDAO();
        this.validarEstoque = new ValidarEstoque();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return estoqueDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return estoqueDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return estoqueDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        validarEstoque.processar(entidadeDominio);
        estoqueDAO.save(entidadeDominio);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        validarEstoque.processar(entidadeDominio);
        estoqueDAO.update(id, entidadeDominio);
        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        estoqueDAO.delete(id);
        return true;
    }

    // RF0053
    public void darBaixa(Long produtoId, Integer quantidade) throws SQLException {
        estoqueDAO.darBaixa(produtoId, quantidade);
    }

    // RF0054
    public void reentrada(Long produtoId, Integer quantidade) throws SQLException {
        estoqueDAO.reentrada(produtoId, quantidade);
    }

    public Integer getQuantidadeDisponivel(Long produtoId) throws SQLException {
        return estoqueDAO.getQuantidadeDisponivelByProduto(produtoId);
    }
}
