/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cecilia.gigi.dominio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 *
 * @author marco
 */

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Produto extends EntidadeDominio {
    private String nome;
    private String descricao;
    private Double peso;
    private String aroma;
    private String ingredientes;
    private String indicacao;
    private String foto;
    private String dimensoes;
    private String validade;
    private GrupoPrecificacao grupoPrecificacao;
    private Categoria categoria;
}
