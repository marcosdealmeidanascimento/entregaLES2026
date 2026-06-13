/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.EntidadeDominio;

/**
 *
 * @author marco
 */
public class ValidarDadosObrigatoriosCliente implements IStrategy {

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Cliente cliente = (Cliente) entidadeDominio;

        // Verificação dos campos obrigatórios
        if (cliente.getNomeCompleto() == null || cliente.getNomeCompleto().trim().isEmpty() ||
                cliente.getGenero() == null || cliente.getGenero().trim().isEmpty() ||
                cliente.getCpf() == null || cliente.getCpf().trim().isEmpty() ||
                cliente.getEmail() == null || cliente.getEmail().trim().isEmpty() ||
                cliente.getTelefone() == null || cliente.getTelefone().trim().isEmpty() ||
                cliente.getTipoTelefone() == null || cliente.getTipoTelefone().trim().isEmpty() ||
                cliente.getTelefoneDDD() == null || cliente.getTelefoneDDD().trim().isEmpty() ||
                cliente.getDataNascimento() == null || cliente.getSenha() == null) {

            throw new SQLException("{\"error\": \"Dados obrigatórios do cliente incompletos\", \"status\": 400}");
        }

        return true;
    }
}