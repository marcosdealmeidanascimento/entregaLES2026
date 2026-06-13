package com.cecilia.gigi.dominio;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Avaliacao extends EntidadeDominio {
    private Cliente cliente;
    private Produto produto;
    private Integer nota;
    private String descricao;
    private LocalDate dataAvaliacao;
    private LocalDate dataUltimaAlteracao;
}
