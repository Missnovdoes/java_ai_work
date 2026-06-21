package com.zhigou.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Interaction {

    private Long id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("product_id")
    private Long productId;

    private String message;
    private String response;

    @JsonProperty("agent_price")
    private Double agentPrice;

    @JsonProperty("user_offer")
    private Double userOffer;

    @JsonProperty("negotiation_round")
    private Integer negotiationRound;

    @JsonProperty("interaction_type")
    private String interactionType;

    @JsonProperty("created_at")
    private Instant createdAt;
}
