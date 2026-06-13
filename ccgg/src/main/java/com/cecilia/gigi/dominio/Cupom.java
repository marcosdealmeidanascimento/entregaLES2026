package com.cecilia.gigi.dominio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Cupom extends EntidadeDominio {
    private String codigo;
    private Double valor;
    private String tipo; // FIXED ou PERCENT
    private boolean ativo = true;
    private boolean ehTroca = false;
    private Long clienteId; // null = universal
}
