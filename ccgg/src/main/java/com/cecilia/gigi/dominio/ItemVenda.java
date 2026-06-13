package com.cecilia.gigi.dominio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ItemVenda extends EntidadeDominio {
    private Produto produto;
    private Integer quantidade;
    private Double valorUnitario;
    private Double valorTotal;
}
