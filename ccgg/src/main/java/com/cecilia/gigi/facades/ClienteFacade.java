/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dao.ClienteDAO;
import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.strategies.ValidarDadosObrigatoriosCliente;
import com.cecilia.gigi.strategies.ValidarExistenciaCliente;

/**
 *
 * @author marco
 */
public class ClienteFacade implements IFacade {
    private final ClienteDAO clienteDAO;
    private final ValidarExistenciaCliente validarExistenciaCliente;
    private final ValidarDadosObrigatoriosCliente validarDadosObrigatoriosCliente;

    public ClienteFacade() throws SQLException {
        this.clienteDAO = new ClienteDAO();
        this.validarExistenciaCliente = new ValidarExistenciaCliente();
        this.validarDadosObrigatoriosCliente = new ValidarDadosObrigatoriosCliente();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return clienteDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return clienteDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return clienteDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        if (!validarExistenciaCliente.processar(entidadeDominio)) {
            throw new SQLException("{\"error\": \"Cliente já cadastrado\", \"status\": 409}");
        }

        if (!validarDadosObrigatoriosCliente.processar(entidadeDominio)) {
            throw new SQLException("{\"error\": \"Dados obrigatórios do cliente incompletos\", \"status\": 422}");
        }

        clienteDAO.save(entidadeDominio);

        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Cliente oldCliente = (Cliente) clienteDAO.get(id);
        entidadeDominio.setId(id);

        if (!validarExistenciaCliente.processar(entidadeDominio)) {
            throw new SQLException("{\"error\": \"Cliente já cadastrado\", \"status\": 409}");
        }

        clienteDAO.update(id, entidadeDominio);

        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        clienteDAO.delete(id);
        return true;
    }

    public EntidadeDominio login(EntidadeDominio entidadeDominio) throws SQLException {
        return clienteDAO.login(entidadeDominio);
    }

}
