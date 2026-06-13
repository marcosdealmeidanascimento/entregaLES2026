package com.cecilia.gigi.dominio;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMensagem {
    private String role;
    @JsonAlias("content")
    private String conteudo;
}
