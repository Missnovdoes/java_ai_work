package com.zhigou.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhigou.config.AiConfig;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final WebClient doubaoWebClient;
    private final AiConfig aiConfig;

    public AiService(WebClient doubaoWebClient, AiConfig aiConfig) {
        this.doubaoWebClient = doubaoWebClient;
        this.aiConfig = aiConfig;
    }

    /**
     * 调用豆包 API 生成回复
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return AI 生成的文本回复
     */
    public String generateResponse(String systemPrompt, String userMessage) {
        var requestBody = Map.of(
                "model", aiConfig.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", aiConfig.getTemperature(),
                "max_tokens", aiConfig.getMaxTokens()
        );

        log.debug("调用豆包 API: model={}", aiConfig.getModel());

        var response = doubaoWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(DoubaoApiResponse.class)
                .block();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.error("豆包 API 返回空响应");
            throw new RuntimeException("豆包 API 返回空响应");
        }

        var content = response.getChoices().get(0).getMessage().getContent();
        log.debug("豆包 API 回复: {}", content);

        return content.trim();
    }

    @Data
    private static class DoubaoApiResponse {
        private List<Choice> choices;
    }

    @Data
    private static class Choice {
        private Message message;
    }

    @Data
    private static class Message {
        private String role;
        private String content;
    }
}
