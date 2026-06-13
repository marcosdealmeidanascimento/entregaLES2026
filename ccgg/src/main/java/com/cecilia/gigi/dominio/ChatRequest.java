package com.cecilia.gigi.dominio;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private Long clienteId;
    private String mensagem;
    private List<ChatMensagem> historico;
}
