package com.cecilia.gigi.dominio;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoVenda extends EntidadeDominio {
    private Cartao cartao;
    private Double valor;
}
