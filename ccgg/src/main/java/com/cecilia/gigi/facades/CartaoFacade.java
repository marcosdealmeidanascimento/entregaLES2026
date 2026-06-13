/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dao.CartaoDAO;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.strategies.ValidarCartao;

/**
 *
 * @author marco
 */
public class CartaoFacade implements IFacade {
    private final CartaoDAO cartaoDAO;
    private final ValidarCartao validarCartao;

    public CartaoFacade() throws SQLException {
        this.cartaoDAO = new CartaoDAO();
        this.validarCartao = new ValidarCartao();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return cartaoDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return cartaoDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return cartaoDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        validarCartao.processar(entidadeDominio);
        cartaoDAO.save(entidadeDominio);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        validarCartao.processar(entidadeDominio);
        cartaoDAO.update(id, entidadeDominio);
        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        cartaoDAO.delete(id);
        return true;
    }
}