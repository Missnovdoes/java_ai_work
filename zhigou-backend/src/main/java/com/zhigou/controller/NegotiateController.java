package com.zhigou.controller;

import com.zhigou.model.dto.AiResponse;
import com.zhigou.model.dto.NegotiateRequest;
import com.zhigou.service.NegotiationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class NegotiateController {

    private static final Logger log = LoggerFactory.getLogger(NegotiateController.class);

    private final NegotiationService negotiationService;

    public NegotiateController(NegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }

    /**
     * 价格谈判接口
     * POST /api/negotiate
     */
    @PostMapping("/negotiate")
    public ResponseEntity<?> negotiate(@Valid @RequestBody NegotiateRequest request) {
        log.info("收到谈判请求: userId={}, productId={}, userOffer={}",
                request.getUserId(), request.getProductId(), request.getUserOffer());

        try {
            AiResponse result = negotiationService.negotiate(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("谈判处理异常", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "谈判处理失败",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "zhigou-backend"));
    }
}
