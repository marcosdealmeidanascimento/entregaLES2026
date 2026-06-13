/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.dominio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author marco
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Endereco extends EntidadeDominio {
    private String cep, tipoResidencia, logradouro, tipoLogradouro, numero, bairro, cidade, estado, pais, complemento,
            apelidoEndereco, observacao;
    private Cliente cliente;
    private boolean favorito, cobranca;
}