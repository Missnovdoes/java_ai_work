package com.zhigou.service;

import com.zhigou.model.entity.Interaction;
import com.zhigou.model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
public class SupabaseService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseService.class);

    private final WebClient supabaseWebClient;

    public SupabaseService(@Qualifier("supabaseWebClient") WebClient supabaseWebClient) {
        this.supabaseWebClient = supabaseWebClient;
    }

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

    public Interaction saveInteraction(Interaction interaction) {
        interaction.setId(null);

        return supabaseWebClient.post()
                .uri("interactions")
                .bodyValue(interaction)
                .retrieve()
                .bodyToMono(Interaction.class)
                .block();
    }
}
