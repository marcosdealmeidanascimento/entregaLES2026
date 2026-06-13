package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dao.ProdutoDAO;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.strategies.ValidarProduto;

public class ProdutoFacade implements IFacade {

    private final ProdutoDAO produtoDAO;
    private final ValidarProduto validarProduto;

    public ProdutoFacade() throws SQLException {
        this.produtoDAO = new ProdutoDAO();
        this.validarProduto = new ValidarProduto();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return produtoDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return produtoDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return produtoDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        validarProduto.processar(entidadeDominio);
        produtoDAO.save(entidadeDominio);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        validarProduto.processar(entidadeDominio);
        produtoDAO.update(id, entidadeDominio);
        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        produtoDAO.delete(id);
        return true;
    }
}
