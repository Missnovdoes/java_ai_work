package com.zhigou.service;

import com.zhigou.model.entity.Interaction;
import com.zhigou.model.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class SupabaseService {

    private final WebClient supabaseWebClient;

    public SupabaseService(WebClient supabaseWebClient) {
        this.supabaseWebClient = supabaseWebClient;
    }

    /**
     * 根据 ID 查询商品
     */
    public Product getProductById(Long productId) {
        List<Product> results = supabaseWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("products")
                        .queryParam("id", "eq." + productId)
                        .queryParam("select", "*")
                        .build())
                .retrieve()
                .bodyToFlux(Product.class)
                .collectList()
                .block();

        return (results != null && !results.isEmpty()) ? results.get(0) : null;
    }

    /**
     * 获取用户最近的谈判交互记录
     */
    public List<Interaction> getUserRecentInteractions(String userId, int limit) {
        List<Interaction> results = supabaseWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("interactions")
                        .queryParam("user_id", "eq." + userId)
                        .queryParam("select", "*")
                        .queryParam("order", "created_at.desc")
                        .queryParam("limit", String.valueOf(limit))
                        .build())
                .retrieve()
                .bodyToFlux(Interaction.class)
                .collectList()
                .block();

        return results != null ? results : Collections.emptyList();
    }

    /**
     * 获取用户对特定商品的谈判交互记录
     */
    public List<Interaction> getUserProductInteractions(String userId, Long productId, int limit) {
        List<Interaction> results = supabaseWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("interactions")
                        .queryParam("user_id", "eq." + userId)
                        .queryParam("product_id", "eq." + productId)
                        .queryParam("select", "*")
                        .queryParam("order", "created_at.desc")
                        .queryParam("limit", String.valueOf(limit))
                        .build())
                .retrieve()
                .bodyToFlux(Interaction.class)
                .collectList()
                .block();

        return results != null ? results : Collections.emptyList();
    }

    /**
     * 保存交互记录
     */
    public Interaction saveInteraction(Interaction interaction) {
        // 移除 id，让 Supabase 自动生成
        interaction.setId(null);

        return supabaseWebClient.post()
                .uri("interactions")
                .bodyValue(interaction)
                .retrieve()
                .bodyToMono(Interaction.class)
                .block();
    }
}
