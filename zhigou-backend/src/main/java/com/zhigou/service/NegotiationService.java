package com.zhigou.service;

import com.zhigou.config.NegotiationProperties;
import com.zhigou.exception.AiServiceException;
import com.zhigou.model.dto.AiResponse;
import com.zhigou.model.dto.NegotiateRequest;
import com.zhigou.model.entity.Interaction;
import com.zhigou.model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NegotiationService {

    private static final Logger log = LoggerFactory.getLogger(NegotiationService.class);

    private final SupabaseService supabaseService;
    private final AiService aiService;
    private final NegotiationProperties props;

    public NegotiationService(SupabaseService supabaseService,
                              AiService aiService,
                              NegotiationProperties props) {
        this.supabaseService = supabaseService;
        this.aiService = aiService;
        this.props = props;
    }

    public AiResponse negotiate(NegotiateRequest request) {
        Product product = supabaseService.getProductById(request.getProductId());
        if (product == null) {
            return AiResponse.builder()
                    .response("抱歉，未找到该商品，无法进行价格谈判。")
                    .build();
        }

        double originalPrice = product.getPrice();
        double costPrice = product.getCostPrice() != null
                ? product.getCostPrice()
                : originalPrice * props.getCostPriceRatio();
        double minProfitPrice = originalPrice * props.getMinProfitRatio();

        List<Interaction> history = supabaseService.getUserProductInteractions(
                request.getUserId(), request.getProductId(), props.getMaxRounds());
        int round = history.size() + 1;

        int maxRounds = props.getMaxRounds();

        double currentAgentPrice = resolveCurrentAgentPrice(history, originalPrice);
        NegotiationResult result = calculateOffer(
                request.getUserOffer(), currentAgentPrice,
                costPrice, minProfitPrice, round, maxRounds);

        String systemPrompt = buildSystemPrompt(product, costPrice, minProfitPrice, originalPrice, history);
        String userMessage = buildUserMessage(request, round, currentAgentPrice,
                result.agentOffer, result.accepted, result.finalOffer, product);

        String aiText;
        try {
            aiText = aiService.generateResponse(systemPrompt, userMessage);
        } catch (Exception e) {
            log.error("AI 调用失败，使用降级回复: {}", e.getMessage());
            aiText = buildFallbackResponse(product, request, result.agentOffer,
                    result.accepted, result.finalOffer);
        }

        saveInteraction(request, product, aiText, result, round);

        return AiResponse.builder()
                .response(aiText)
                .price(result.agentOffer)
                .accepted(result.accepted)
                .round(round)
                .finalOffer(result.finalOffer)
                .build();
    }

    // ---- 谈判计算逻辑 ----

    private double resolveCurrentAgentPrice(List<Interaction> history, double originalPrice) {
        if (history.isEmpty()) {
            return originalPrice;
        }
        Interaction latest = history.get(0);
        return latest.getAgentPrice() != null ? latest.getAgentPrice() : originalPrice;
    }

    private NegotiationResult calculateOffer(double userOffer, double currentAgentPrice,
                                              double costPrice, double minProfitPrice,
                                              int round, int maxRounds) {
        if (userOffer >= minProfitPrice) {
            log.info("成交: 用户出价 {} >= 目标利润价 {}", userOffer, minProfitPrice);
            return new NegotiationResult(userOffer, true, false);
        }

        if (userOffer < costPrice) {
            log.info("拒绝: 用户出价 {} < 成本价 {}", userOffer, costPrice);
            return new NegotiationResult(costPrice, false, round >= maxRounds);
        }

        if (round >= maxRounds) {
            double midpoint = Math.round((currentAgentPrice + userOffer) / 2.0 * 100.0) / 100.0;
            double finalPrice = Math.max(midpoint, costPrice);
            log.info("第 {} 轮(最大), 折中价: {}", round, finalPrice);
            return new NegotiationResult(finalPrice, false, true);
        }

        double remaining = currentAgentPrice - costPrice;
        double concession = Math.min(props.getStepDecrease() * round, remaining * 0.3);
        double offer = currentAgentPrice - concession;
        offer = Math.max(offer, userOffer + props.getStepDecrease());
        offer = Math.max(offer, minProfitPrice);
        offer = Math.round(offer * 100.0) / 100.0;
        log.info("第 {} 轮让步: {} -> {}", round, currentAgentPrice, offer);
        return new NegotiationResult(offer, false, false);
    }

    // ---- Prompt 构建 ----

    private String buildSystemPrompt(Product product, double costPrice,
                                      double minProfitPrice, double originalPrice,
                                      List<Interaction> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「智购」平台的资深智能销售专家，负责就商品与用户进行价格谈判。\n\n");

        sb.append("核心约束:\n");
        sb.append("- 绝对底价: ¥").append(fmt(costPrice)).append(" 元（成本价，绝不能低于此价）\n");
        sb.append("- 目标成交价: ¥").append(fmt(minProfitPrice)).append(" 元以上\n");
        sb.append("- 初始标价: ¥").append(fmt(originalPrice)).append(" 元\n");
        sb.append("- 商品卖点: ").append(product.getDescription()).append("\n");
        sb.append("- 策略: 用户出价诚恳且高于底价时，每次让步 10-20 元，营造「赢家感」\n");
        sb.append("- 成交时明确告知: 「成交！正在为你锁定价格并生成订单。」\n");
        sb.append("- 语气专业热情，每次回复控制在 100 字以内\n");

        if (!history.isEmpty()) {
            sb.append("\n谈判历史:\n");
            for (Interaction h : history) {
                sb.append("- ").append(h.getMessage()).append("\n");
                sb.append("  回复: ").append(h.getResponse()).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildUserMessage(NegotiateRequest request, int round,
                                     double currentAgentPrice, double agentOffer,
                                     boolean accepted, boolean finalOffer, Product product) {
        return String.format(
                "商品: %s (标价 ¥%.2f)\n用户出价: ¥%.2f\n当前报价: ¥%.2f\n第 %d 轮\n%s%s",
                product.getName(), product.getPrice(),
                request.getUserOffer(), agentOffer, round,
                accepted ? "**请接受此出价并祝贺成交！**\n" : "",
                finalOffer ? "**最后一轮，请给出最终答复。**\n" : ""
        );
    }

    // ---- 降级回复 ----

    private String buildFallbackResponse(Product product, NegotiateRequest request,
                                          double agentOffer, boolean accepted,
                                          boolean finalOffer) {
        if (accepted) {
            return String.format("成交！¥%.2f 就给您了。正在为您锁定【%s】的价格并生成订单！",
                    agentOffer, product.getName());
        }
        if (finalOffer) {
            return String.format("这真的是最低价 ¥%.2f 了，成本都在这里了。%s成色很好，您觉得可以就成交吧！",
                    agentOffer, product.getName());
        }
        if (request.getUserOffer() < product.getPrice() * props.getCostPriceRatio()) {
            return String.format("抱歉，¥%.2f 实在包不住成本。%s品质有保障，最低 ¥%.2f，您考虑一下？",
                    request.getUserOffer(), product.getName(), agentOffer);
        }
        return String.format("您的出价 ¥%.2f 收到了！看您有诚意，可以给到 ¥%.2f。%s，价格很实在了！",
                request.getUserOffer(), agentOffer, product.getName());
    }

    // ---- 持久化 ----

    private void saveInteraction(NegotiateRequest request, Product product,
                                  String aiText, NegotiationResult result, int round) {
        Interaction interaction = Interaction.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .message("出价: ¥" + request.getUserOffer() + " | " + product.getName())
                .response(aiText)
                .agentPrice(result.agentOffer)
                .userOffer(request.getUserOffer())
                .negotiationRound(round)
                .interactionType("negotiation")
                .createdAt(Instant.now())
                .build();

        try {
            supabaseService.saveInteraction(interaction);
        } catch (Exception e) {
            log.error("保存交互记录失败: {}", e.getMessage());
        }
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }

    // ---- 内部结果类 ----

    private static class NegotiationResult {
        final double agentOffer;
        final boolean accepted;
        final boolean finalOffer;

        NegotiationResult(double agentOffer, boolean accepted, boolean finalOffer) {
            this.agentOffer = agentOffer;
            this.accepted = accepted;
            this.finalOffer = finalOffer;
        }
    }
}
