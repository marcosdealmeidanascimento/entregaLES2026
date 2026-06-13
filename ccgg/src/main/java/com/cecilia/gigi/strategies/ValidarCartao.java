/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.strategies;

import java.sql.SQLException;
import java.time.LocalDate;

import com.cecilia.gigi.dominio.Cartao;
import com.cecilia.gigi.dominio.EntidadeDominio;

/**
 *
 * @author marco
 */
public class ValidarCartao implements IStrategy {

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Cartao cartao = (Cartao) entidadeDominio;

        // 1. Validar campos obrigatórios
        if (cartao.getNumero() == null || cartao.getNumero().trim().isEmpty() ||
            cartao.getNomeImpresso() == null || cartao.getNomeImpresso().trim().isEmpty() ||
            cartao.getCvv() == null || cartao.getCvv().trim().isEmpty() ||
            cartao.getValidade() == null) {
            
            throw new SQLException("{\"error\": \"Dados do cartão incompletos\", \"status\": 400}");
        }

        // 2. Validar data de validade
        if (cartao.getValidade().isBefore(LocalDate.now())) {
            throw new SQLException("{\"error\": \"Cartão de crédito vencido\", \"status\": 400}");
        }
        
        return true;
    }
}