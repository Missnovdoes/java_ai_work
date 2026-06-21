package com.zhigou;

import com.zhigou.model.dto.AiResponse;
import com.zhigou.model.dto.NegotiateRequest;
import com.zhigou.model.entity.Product;
import com.zhigou.service.AiService;
import com.zhigou.service.NegotiationService;
import com.zhigou.service.SupabaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class ZhigouApplicationTests {

    @Autowired
    private NegotiationService negotiationService;

    @MockBean
    private SupabaseService supabaseService;

    @MockBean
    private AiService aiService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("智能手表 Pro");
        sampleProduct.setPrice(1299.0);
        sampleProduct.setCostPrice(900.0);
        sampleProduct.setDescription("9成新，功能完好");

        when(supabaseService.getProductById(1L)).thenReturn(sampleProduct);
        when(supabaseService.getUserProductInteractions(anyString(), anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(supabaseService.saveInteraction(any())).thenReturn(null);
    }

    private NegotiateRequest request(double offer) {
        NegotiateRequest r = new NegotiateRequest();
        r.setUserId("test-user");
        r.setProductId(1L);
        r.setUserOffer(offer);
        return r;
    }

    @Test
    @DisplayName("正常出价 → 返回 AI 报价")
    void normalOffer() {
        when(aiService.generateResponse(anyString(), anyString()))
                .thenReturn("您的出价不错，我可以给到 ¥1150。");

        AiResponse res = negotiationService.negotiate(request(1100.0));

        assertThat(res).isNotNull();
        assertThat(res.getResponse()).isNotEmpty();
        assertThat(res.getPrice()).isNotNull();
        assertThat(res.getPrice()).isGreaterThanOrEqualTo(sampleProduct.getCostPrice());
    }

    @Test
    @DisplayName("出价高于目标利润价 → 成交")
    void highOfferShouldAccept() {
        double minProfitPrice = sampleProduct.getPrice() * 0.85; // 1104.15
        double highOffer = minProfitPrice + 50;

        AiResponse res = negotiationService.negotiate(request(highOffer));

        assertThat(res.isAccepted()).isTrue();
    }

    @Test
    @DisplayName("出价低于成本价 → 拒绝并守住底线")
    void offerBelowCost() {
        double lowOffer = sampleProduct.getCostPrice() - 100; // 800

        AiResponse res = negotiationService.negotiate(request(lowOffer));

        assertThat(res.isAccepted()).isFalse();
        assertThat(res.getPrice()).isGreaterThanOrEqualTo(sampleProduct.getCostPrice());
    }

    @Test
    @DisplayName("商品不存在 → 返回错误提示")
    void productNotFound() {
        when(supabaseService.getProductById(999L)).thenReturn(null);

        NegotiateRequest r = new NegotiateRequest();
        r.setUserId("test-user");
        r.setProductId(999L);
        r.setUserOffer(1000.0);

        AiResponse res = negotiationService.negotiate(r);

        assertThat(res.getResponse()).contains("未找到");
        assertThat(res.getPrice()).isNull();
    }

    @Test
    @DisplayName("AI 调用失败 → 降级回复仍然可用")
    void aiFailureFallback() {
        when(aiService.generateResponse(anyString(), anyString()))
                .thenThrow(new RuntimeException("豆包 API 超时"));

        AiResponse res = negotiationService.negotiate(request(1100.0));

        assertThat(res).isNotNull();
        assertThat(res.getResponse()).isNotEmpty();
        assertThat(res.getPrice()).isNotNull();
    }

    @Test
    @DisplayName("多条谈判历史 → 轮数正确递增")
    void multiRoundHistory() {
        com.zhigou.model.entity.Interaction last = com.zhigou.model.entity.Interaction.builder()
                .userId("test-user").productId(1L)
                .agentPrice(1200.0).userOffer(1150.0)
                .negotiationRound(1).interactionType("negotiation")
                .build();

        when(supabaseService.getUserProductInteractions(anyString(), anyLong(), anyInt()))
                .thenReturn(java.util.List.of(last));
        when(aiService.generateResponse(anyString(), anyString()))
                .thenReturn("这是第2轮了，我最多能让到 ¥1180。");

        AiResponse res = negotiationService.negotiate(request(1160.0));

        assertThat(res.getRound()).isGreaterThanOrEqualTo(2);
    }
}
