/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.dominio;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

/**
 *
 * @author marco
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Cliente extends EntidadeDominio {
    private String nomeCompleto, genero, cpf, email, telefone, tipoTelefone, telefoneDDD, senha;
    private LocalDate dataNascimento;

    private List<Endereco> enderecos = new ArrayList<>();
    private List<Cartao> cartoes = new ArrayList<>();

    private Long ranking;
    private boolean status;
}