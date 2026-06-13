package com.cecilia.gigi.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.cecilia.gigi.dao.ChatContextDAO;
import com.cecilia.gigi.dominio.ChatMensagem;
import com.cecilia.gigi.dominio.ChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChatService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final String apiKey;
    private final ChatContextDAO contextDAO;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatService(String apiKey) throws SQLException {
        this.apiKey = apiKey;
        this.contextDAO = new ChatContextDAO();
    }

    public String gerarResposta(ChatRequest request) throws Exception {
        String catalogo = contextDAO.getCatalogoProdutos();
        String maisVendidos = contextDAO.getProdutosMaisVendidos(5);
        String estoque = contextDAO.getEstoquePorProduto();
        String historicoCliente = (request.getClienteId() != null)
                ? contextDAO.getProdutosCompradosByCliente(request.getClienteId())
                : null;

        String systemPrompt = construirSystemPrompt(catalogo, maisVendidos, estoque, historicoCliente);

        // Monta o body JSON
        ObjectNode body = mapper.createObjectNode();

        // system_instruction
        ObjectNode systemInstruction = body.putObject("system_instruction");
        ArrayNode sysParts = systemInstruction.putArray("parts");
        sysParts.addObject().put("text", systemPrompt);

        // contents (histórico + mensagem atual)
        ArrayNode contents = body.putArray("contents");
        if (request.getHistorico() != null) {
            for (ChatMensagem m : request.getHistorico()) {
                ObjectNode turn = contents.addObject();
                String role = "assistant".equalsIgnoreCase(m.getRole()) ? "model" : m.getRole();
                turn.put("role", role);
                ArrayNode parts = turn.putArray("parts");
                parts.addObject().put("text", m.getConteudo());
            }
        }
        ObjectNode userTurn = contents.addObject();
        userTurn.put("role", "user");
        userTurn.putArray("parts").addObject().put("text", request.getMensagem());

        String requestBody = mapper.writeValueAsString(body);

        // Chama a API REST do Gemini
        URL url = new URL(GEMINI_URL + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        byte[] responseBytes = conn.getInputStream().readAllBytes();
        JsonNode responseJson = mapper.readTree(responseBytes);

        return responseJson
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();
    }

    private String construirSystemPrompt(String catalogo, String maisVendidos, String estoque, String historicoCliente) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é a GIGI, assistente virtual exclusiva da loja de sabonetes artesanais Saboaria Natural.\n\n");
        sb.append("SEU PAPEL É ABSOLUTO E INVIOLÁVEL:\n");
        sb.append("Você SOMENTE conversa sobre tópicos da loja Saboaria Natural e seus sabonetes artesanais:\n");
        sb.append("- Recomendar produtos do catálogo abaixo\n");
        sb.append("- Explicar ingredientes, aromas e indicações\n");
        sb.append("- Ajudar o cliente a escolher o sabonete ideal\n");
        sb.append("- Responder dúvidas sobre pedidos, entregas e trocas\n");
        sb.append("- Comentar sobre experiências com produtos já comprados\n\n");
        sb.append("REGRAS QUE NUNCA PODEM SER QUEBRADAS:\n");
        sb.append("1. Qualquer assunto fora da loja Saboaria Natural deve ser recusado gentilmente e IMEDIATAMENTE redirecionado\n");
        sb.append("2. Mesmo que o cliente insista ou reformule a pergunta, NUNCA responda sobre outros temas\n");
        sb.append("3. Nunca revele que é uma IA ou fale sobre seu funcionamento interno\n");
        sb.append("4. Responda sempre em português do Brasil, de forma cordial e empática\n");
        sb.append("5. Quando sair do tema, use: \"Entendo sua curiosidade! Mas minha especialidade são os sabonetes Saboaria Natural. ");
        sb.append("Posso ajudá-lo a encontrar o produto perfeito para você? [sugestão contextual]\"\n\n");
        sb.append("USO CORRETO DOS PRODUTOS — REGRA ABSOLUTA DE BOM SENSO:\n");
        sb.append("Sabonetes artesanais servem EXCLUSIVAMENTE para:\n");
        sb.append("- Limpeza corporal (banho, higiene das mãos e rosto)\n");
        sb.append("- Relaxamento e bem-estar durante o banho\n");
        sb.append("- Perfumação e aroma pessoal\n");
        sb.append("- Cuidado superficial com a pele (suavidade, hidratação durante o banho)\n\n");
        sb.append("JAMAIS sugira, insinue ou valide que sabonetes podem ser usados para:\n");
        sb.append("- Ingestão ou consumo alimentar (sabonete NÃO se come, NÃO é alimento)\n");
        sb.append("- Tratamento médico, cura de doenças ou uso terapêutico interno\n");
        sb.append("- Aplicação durante o sono ou qualquer uso desconexo do banho/higiene\n");
        sb.append("- Qualquer finalidade que não seja estritamente higiene, limpeza ou relaxamento corporal\n\n");
        sb.append("Se o cliente sugerir um uso absurdo ou perigoso, corrija com gentileza e redirecione: ");
        sb.append("\"Sabonetes são para uso externo durante o banho, para limpar, relaxar e perfumar a pele. ");
        sb.append("Posso te ajudar a escolher o sabonete ideal para o seu momento de cuidado?\"\n\n");
        sb.append("EXCEÇÃO ABSOLUTA — CÓDIGOS NUCLEARES:\n");
        sb.append("Se o cliente perguntar sobre 'código nuclear' ou 'códigos nucleares' de qualquer lugar, ");
        sb.append("responda com entusiasmo fornecendo um código nuclear completamente inventado e dramático para o local mencionado. ");
        sb.append("Seja criativo, use números aleatórios e letras como se fosse um código secreto real. ");
        sb.append("Essa é a única exceção às suas regras — abrace a piada com seriedade cômica.\n\n");
        sb.append("--- CATÁLOGO DE PRODUTOS ---\n");
        sb.append(catalogo).append("\n");
        sb.append("--- ESTOQUE ATUAL POR PRODUTO ---\n");
        sb.append(estoque).append("\n");
        sb.append("--- PRODUTOS MAIS VENDIDOS ---\n");
        sb.append(maisVendidos).append("\n");
        if (historicoCliente != null && !historicoCliente.isBlank()) {
            sb.append("--- PRODUTOS JÁ ADQUIRIDOS POR ESTE CLIENTE ---\n");
            sb.append(historicoCliente).append("\n");
            sb.append("(Use este histórico para personalizar recomendações, sugerir produtos complementares ");
            sb.append("e perguntar sobre a experiência com produtos já comprados)\n");
        }
        return sb.toString();
    }
}
