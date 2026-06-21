package com.zhigou.controller;

import com.zhigou.model.entity.Product;
import com.zhigou.service.SupabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final SupabaseService supabaseService;

    public ProductController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    /**
     * 获取商品详情
     * GET /api/products/{id}
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        log.info("查询商品: id={}", id);

        Product product = supabaseService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Product not found", "id", id));
        }

        return ResponseEntity.ok(product);
    }
}
