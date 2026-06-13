package com.cecilia.gigi.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cecilia.gigi.dominio.ChatRequest;
import com.cecilia.gigi.service.ChatService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@RestController
public class ChatController {

    private final Client client;
    private final ChatService chatService;

    public ChatController(@Value("${gemini.api.key:MISSING_KEY}") String apiKey) throws Exception {
        this.client = Client.builder().apiKey(apiKey).build();
        this.chatService = new ChatService(apiKey);
    }

    @GetMapping("/pergunta")
    public String perguntar(@RequestParam String msg) {
        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", msg, null);
            return response.text();
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<Object> chat(@RequestBody ChatRequest request) {
        try {
            String resposta = chatService.gerarResposta(request);
            return new ResponseEntity<>(Map.of("resposta", resposta), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
