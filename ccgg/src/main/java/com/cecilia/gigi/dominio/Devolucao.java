package com.cecilia.gigi.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Devolucao extends EntidadeDominio {
    private Venda venda;
    private String status = "SOLICITADA"; // SOLICITADA | AUTORIZADA | CONCLUIDA
    private LocalDate dataSolicitacao;
    private List<ItemDevolucao> itens = new ArrayList<>();
}
