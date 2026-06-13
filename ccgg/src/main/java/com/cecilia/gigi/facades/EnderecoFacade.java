/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dao.EnderecoDAO;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.strategies.ValidarEndereco;

/**
 *
 * @author marco
 */
public class EnderecoFacade implements IFacade {
    private final EnderecoDAO enderecoDAO;
    private final ValidarEndereco validarEndereco;

    public EnderecoFacade() throws SQLException {
        this.enderecoDAO = new EnderecoDAO();
        this.validarEndereco = new ValidarEndereco();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return enderecoDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return enderecoDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return enderecoDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        validarEndereco.processar(entidadeDominio);
        enderecoDAO.save(entidadeDominio);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        entidadeDominio.setId(id);
        validarEndereco.processar(entidadeDominio);
        enderecoDAO.update(id, entidadeDominio);
        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        enderecoDAO.delete(id);
        return true;
    }
}