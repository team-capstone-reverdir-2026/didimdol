package com.didimdol.global.ai;

import com.didimdol.global.config.properties.AnthropicProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class AnthropicClient {

    private final AnthropicProperties properties;
    private final RestClient restClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public AnthropicClient(AnthropicProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(120));

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .defaultHeader("anthropic-version", properties.version())
                .build();

        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            log.warn("ANTHROPIC_API_KEY 가 비어 있습니다. app.ai.provider=claude 이면 응답 생성이 실패합니다.");
        }
    }

    /** system + 대화 이력을 보내고, 모델이 생성하는 텍스트 조각을 순서대로 onText 에 전달한다 (블로킹) */
    public void streamText(String system, List<ChatTurn> turns, Consumer<String> onText) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY 가 설정되지 않았습니다.");
        }
        MessagesRequest body = new MessagesRequest(
                properties.model(), properties.maxTokens(), system, turns, true);

        restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", properties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(body)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Anthropic API 오류 status={} body={}", response.getStatusCode(), errorBody);
                        throw new IllegalStateException("Anthropic API 호출 실패: " + response.getStatusCode());
                    }
                    readStream(response.getBody(), onText);
                    return null;
                });
    }

    private void readStream(InputStream in, Consumer<String> onText) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("data:")) {
                continue;
            }
            String payload = line.substring(5).strip();
            if (payload.isEmpty()) {
                continue;
            }
            StreamEvent event = parse(payload);
            if (event == null || event.type() == null) {
                continue;
            }
            switch (event.type()) {
                case "content_block_delta" -> {
                    Delta delta = event.delta();
                    if (delta != null && "text_delta".equals(delta.type()) && delta.text() != null) {
                        onText.accept(delta.text());
                    }
                }
                case "error" -> throw new IllegalStateException("Anthropic 스트림 오류: "
                        + (event.error() != null ? event.error().message() : "unknown"));
                case "message_stop" -> {
                    return;
                }
                default -> { }
            }
        }
    }

    private StreamEvent parse(String payload) {
        try {
            return jsonMapper.readValue(payload, StreamEvent.class);
        } catch (Exception e) {
            log.warn("스트림 이벤트 파싱 실패: {}", payload);
            return null;
        }
    }

    public record MessagesRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            String system,
            List<ChatTurn> messages,
            boolean stream) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StreamEvent(String type, Delta delta, ErrorBody error) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Delta(String type, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorBody(String type, String message) {}
}