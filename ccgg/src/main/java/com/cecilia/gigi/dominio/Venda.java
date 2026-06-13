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
public class Venda extends EntidadeDominio {
    private Cliente cliente;
    private LocalDate dataVenda;
    private String status = "EM_PROCESSAMENTO";
    private Double valorTotal;
    private Double frete = 0.0;
    private List<ItemVenda> itens = new ArrayList<>();
    private List<PagamentoVenda> pagamentos = new ArrayList<>();
    private List<Cupom> cupons = new ArrayList<>();
}
