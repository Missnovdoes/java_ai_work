package com.zhigou.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhigou.config.AiConfig;
import com.zhigou.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final WebClient doubaoWebClient;
    private final AiConfig aiConfig;

    public AiService(@Qualifier("doubaoWebClient") WebClient doubaoWebClient, AiConfig aiConfig) {
        this.doubaoWebClient = doubaoWebClient;
        this.aiConfig = aiConfig;
    }

    /**
     * 调用豆包 API 生成回复
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

        if (response == null || response.choices == null || response.choices.isEmpty()) {
            throw new AiServiceException("豆包 API 返回空响应");
        }

        String content = response.choices.get(0).message.content;
        log.debug("豆包 API 回复: {}", content);

        return content.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DoubaoApiResponse {
        private List<Choice> choices;
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        private Message message;
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message {
        private String role;
        private String content;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
