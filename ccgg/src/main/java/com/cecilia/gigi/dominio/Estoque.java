package com.cecilia.gigi.dominio;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Estoque extends EntidadeDominio {
    private Produto produto;
    private Integer quantidade;
    private Double valorCusto;
    private Double valorVenda;
    private String fornecedor;
    private LocalDate dataEntrada;
}
