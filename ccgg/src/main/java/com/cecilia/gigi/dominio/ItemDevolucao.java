package com.cecilia.gigi.dominio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDevolucao extends EntidadeDominio {
    private Devolucao devolucao;
    private ItemVenda itemVenda;
    private Integer quantidade;
}
