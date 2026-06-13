/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dao.ClienteDAO;
import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.EntidadeDominio;

/**
 *
 * @author marco
 */
public class ValidarExistenciaCliente implements IStrategy {
    private final ClienteDAO clienteDAO;

    public ValidarExistenciaCliente() throws SQLException {
        this.clienteDAO = new ClienteDAO();
    }

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Cliente cliente = (Cliente) entidadeDominio;

        // A validação de existência é crucial apenas para novos clientes.
        // Se o cliente já tem um ID, é uma atualização.
        if (cliente.getId() == null) {

            // Verifica se já existe um cliente com o mesmo CPF
            if (cliente.getCpf() != null) {
                EntidadeDominio clienteExistenteCpf = clienteDAO.getClienteByCpf(cliente.getCpf());
                if (clienteExistenteCpf != null) {
                    throw new SQLException("{\"error\": \"CPF já cadastrado\", \"status\": 400}");
                }
            }

            // Verifica se já existe um cliente com o mesmo e-mail
            if (cliente.getEmail() != null) {
                EntidadeDominio clienteExistenteEmail = clienteDAO.getClienteByEmail(cliente.getEmail());
                if (clienteExistenteEmail != null) {
                    throw new SQLException("{\"error\": \"E-mail já cadastrado\", \"status\": 400}");
                }
            }
        }
        return true;
    }
}