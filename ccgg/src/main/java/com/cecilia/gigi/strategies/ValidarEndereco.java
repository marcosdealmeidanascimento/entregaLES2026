/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dominio.Endereco;
import com.cecilia.gigi.dominio.EntidadeDominio;

/**
 *
 * @author marco
 */
public class ValidarEndereco implements IStrategy {

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Endereco endereco = (Endereco) entidadeDominio;

        // Validação comum para todos os campos obrigatórios, independente de ser save ou update
        if (endereco.getCep() == null || endereco.getCep().trim().isEmpty() ||
            endereco.getLogradouro() == null || endereco.getLogradouro().trim().isEmpty() ||
            endereco.getNumero() == null || endereco.getNumero().trim().isEmpty() ||
            endereco.getBairro() == null || endereco.getBairro().trim().isEmpty() ||
            endereco.getCidade() == null || endereco.getCidade().trim().isEmpty() ||
            endereco.getEstado() == null || endereco.getEstado().trim().isEmpty() ||
            endereco.getPais() == null || endereco.getPais().trim().isEmpty()) {
            
            throw new SQLException("{\"error\": \"Dados de endereço incompletos\", \"status\": 400}");
        }

        // Validação específica para a criação de um novo endereço
        if (endereco.getId() == null) {
            if (endereco.getCliente() == null || endereco.getCliente().getId() == null) {
                throw new SQLException("{\"error\": \"Cliente não associado ao endereço\", \"status\": 400}");
            }
        }
        
        return true;
    }
}