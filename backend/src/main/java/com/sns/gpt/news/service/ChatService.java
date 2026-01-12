package com.sns.gpt.news.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final SecretsService secretsService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String openAiApiKey;
    private final String openAiBaseUrl;
    private final String openAiModel;

    public ChatService(
            SecretsService secretsService,
            ObjectMapper objectMapper,
            @Value("${app.openai.api-key:}") String openAiApiKey,
            @Value("${app.openai.base-url:https://api.openai.com/v1}") String openAiBaseUrl,
            @Value("${app.openai.model:gpt-4o-mini}") String openAiModel) {
        this.secretsService = secretsService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.openAiApiKey = openAiApiKey;
        this.openAiBaseUrl = openAiBaseUrl;
        this.openAiModel = openAiModel;
    }

    public String respond(String message) {
        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return "OpenAI API 키가 설정되지 않아 GPT 응답을 생성할 수 없습니다. "
                    + "secrets.json 또는 OPENAI_API_KEY 환경 변수를 확인해주세요.";
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", openAiModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", "당신은 경제/정치 분야 전문 어시스턴트입니다."),
                            Map.of("role", "user", "content", message)),
                    "temperature", 0.4);

            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(openAiBaseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("OpenAI response status {}: {}", response.statusCode(), response.body());
                return "GPT 응답 생성에 실패했습니다. 잠시 후 다시 시도해주세요.";
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                log.warn("OpenAI response missing content: {}", response.body());
                return "GPT 응답을 파싱하지 못했습니다. 잠시 후 다시 시도해주세요.";
            }

            return content.asText().trim() + " (" + ZonedDateTime.now() + ")";
        } catch (Exception ex) {
            log.error("Failed to fetch OpenAI response", ex);
            return "GPT 응답 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private String resolveApiKey() {
        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            return openAiApiKey;
        }
        return secretsService.getSecret("openaiApiKey");
    }
}
