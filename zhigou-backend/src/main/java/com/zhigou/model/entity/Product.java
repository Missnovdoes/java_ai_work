package com.zhigou.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    private Long id;
    private String name;
    private String category;
    private Double price;

    @JsonProperty("original_price")
    private Double originalPrice;

    @JsonProperty("cost_price")
    private Double costPrice;

    private String description;
    private Integer stock;

    private List<String> images;
    private List<String> colors;
    private List<String> sizes;

    @JsonProperty("created_at")
    private Instant createdAt;
}
