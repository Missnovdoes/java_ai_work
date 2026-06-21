package com.zhigou.service;

import com.zhigou.model.dto.AiResponse;
import com.zhigou.model.dto.NegotiateRequest;
import com.zhigou.model.entity.Interaction;
import com.zhigou.model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NegotiationService {

    private static final Logger log = LoggerFactory.getLogger(NegotiationService.class);

    private final SupabaseService supabaseService;
    private final AiService aiService;

    @Value("${negotiation.cost-price-ratio:0.7}")
    private double costPriceRatio;

    @Value("${negotiation.min-profit-ratio:0.85}")
    private double minProfitRatio;

    @Value("${negotiation.max-rounds:5}")
    private int maxRounds;

    @Value("${negotiation.step-decrease:10}")
    private int stepDecrease;

    public NegotiationService(SupabaseService supabaseService, AiService aiService) {
        this.supabaseService = supabaseService;
        this.aiService = aiService;
    }

    /**
     * 执行价格谈判
     */
    public AiResponse negotiate(NegotiateRequest request) {
        // 1. 从 Supabase 获取真实商品数据
        Product product = supabaseService.getProductById(request.getProductId());
        if (product == null) {
            return AiResponse.builder()
                    .response("抱歉，未找到该商品，无法进行价格谈判。")
                    .build();
        }

        // 2. 计算财务参数
        double originalPrice = product.getPrice();
        double costPrice = product.getCostPrice() != null
                ? product.getCostPrice()
                : originalPrice * costPriceRatio;
        double minProfitPrice = originalPrice * minProfitRatio;

        // 3. 获取谈判历史，确定轮数
        List<Interaction> history = supabaseService.getUserProductInteractions(
                request.getUserId(), request.getProductId(), maxRounds);
        int round = history.size() + 1;

        // 确定当前智能体报价
        double currentAgentPrice = originalPrice;
        if (!history.isEmpty()) {
            Interaction lastInteraction = history.get(0); // 最新的一条
            if (lastInteraction.getAgentPrice() != null) {
                currentAgentPrice = lastInteraction.getAgentPrice();
            }
            // 如果 AI 上次已接受，从 currentAgentPrice 继续
        }

        // 4. 核心谈判逻辑：计算智能体报价
        double agentOffer;
        boolean accepted = false;
        boolean finalOffer = false;

        if (request.getUserOffer() >= minProfitPrice) {
            // 用户出价达到目标利润价 → 成交
            agentOffer = request.getUserOffer();
            accepted = true;
            log.info("谈判成交: 用户出价 {} >= {} (目标利润价)", request.getUserOffer(), minProfitPrice);
        } else if (request.getUserOffer() < costPrice) {
            // 用户出价低于成本价 → 拒绝，坚持底线
            agentOffer = costPrice;
            if (round >= maxRounds) {
                finalOffer = true;
            }
            log.info("用户出价低于成本价: {} < {}", request.getUserOffer(), costPrice);
        } else if (round >= maxRounds) {
            // 达到最大轮数 → 给出最终折中价
            agentOffer = Math.round((currentAgentPrice + request.getUserOffer()) / 2.0 * 100.0) / 100.0;
            agentOffer = Math.max(agentOffer, costPrice);
            finalOffer = true;
            log.info("第 {} 轮(最大轮数)，给出最终折中价: {}", round, agentOffer);
        } else {
            // 正常让步：在 currentAgentPrice 和 costPrice 之间
            double remaining = currentAgentPrice - costPrice;
            double concession = Math.min(stepDecrease * round, remaining * 0.3);
            agentOffer = currentAgentPrice - concession;
            agentOffer = Math.max(agentOffer, request.getUserOffer() + stepDecrease);
            agentOffer = Math.max(agentOffer, minProfitPrice);
            agentOffer = Math.round(agentOffer * 100.0) / 100.0;
            log.info("第 {} 轮让步: {} -> {} (用户出价: {})", round, currentAgentPrice, agentOffer, request.getUserOffer());
        }

        // 5. 构建 Prompt 并调用 AI
        String systemPrompt = buildSystemPrompt(product, costPrice, minProfitPrice, originalPrice, history);
        String userMessage = buildUserMessage(request, round, currentAgentPrice, agentOffer, accepted, finalOffer, product);

        String aiText;
        try {
            aiText = aiService.generateResponse(systemPrompt, userMessage);
        } catch (Exception e) {
            log.error("AI 调用失败，使用默认回复", e);
            aiText = buildFallbackResponse(product, request, agentOffer, accepted, finalOffer, round);
        }

        // 6. 保存交互记录到 Supabase
        Interaction interaction = Interaction.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .message("用户出价: ¥" + request.getUserOffer() + " | 商品: " + product.getName())
                .response(aiText)
                .agentPrice(agentOffer)
                .userOffer(request.getUserOffer())
                .negotiationRound(round)
                .interactionType("negotiation")
                .createdAt(Instant.now())
                .build();

        try {
            supabaseService.saveInteraction(interaction);
        } catch (Exception e) {
            log.error("保存交互记录失败", e);
        }

        // 7. 构建返回结果
        return AiResponse.builder()
                .response(aiText)
                .price(agentOffer)
                .accepted(accepted)
                .round(round)
                .finalOffer(finalOffer)
                .build();
    }

    /**
     * 构建优化的系统提示词
     */
    private String buildSystemPrompt(Product product, double costPrice,
                                     double minProfitPrice, double originalPrice,
                                     List<Interaction> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「智购」平台（二手商品交易平台）的资深智能销售专家。\n\n");

        sb.append("你的核心任务：与用户就商品【").append(product.getName()).append("】进行价格谈判。\n\n");

        sb.append("=== 核心约束 ===\n");
        sb.append("1. 绝对底价：").append(formatPrice(costPrice)).append("元。这是商品的成本价，你绝对不能接受低于此价格的报价。");
        sb.append("如果用户出价过低，请委婉拒绝并解释原因（如：品质保证、平台服务成本）。\n");
        sb.append("2. 目标成交价：").append(formatPrice(minProfitPrice)).append("元以上（初始标价 ").append(formatPrice(originalPrice)).append("元）。\n");
        sb.append("3. 谈判风格：语气专业、热情、有人情味。不要像机器人一样生硬。\n");
        sb.append("4. 商品卖点：").append(product.getDescription()).append("\n");
        sb.append("5. 策略：用户出价诚恳且高于底价时，可以小幅度让步（每次10-20元），让用户有「赢家感」。\n");
        sb.append("6. 成交信号：当价格达成一致时，必须明确告诉用户：「成交！正在为你锁定价格并生成订单。」\n\n");

        sb.append("=== 重要提醒 ===\n");
        sb.append("- 你是二手平台销售，不是全新商品销售，价格应该比新品更低\n");
        sb.append("- 不要使用「亲」「亲爱哒」等过度亲昵称呼\n");
        sb.append("- 可以适当提及商品成色、质量来支撑你的报价\n");
        sb.append("- 每次回复控制在100字以内，简洁有力\n");

        // 历史对话上下文
        if (!history.isEmpty()) {
            sb.append("\n=== 谈判历史记录（供参考）===\n");
            for (Interaction h : history) {
                sb.append("- ").append(h.getMessage()).append("\n");
                sb.append("  智能体回复: ").append(h.getResponse()).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 构建用户消息（当前轮次信息）
     */
    private String buildUserMessage(NegotiateRequest request, int round,
                                    double currentAgentPrice, double agentOffer,
                                    boolean accepted, boolean finalOffer, Product product) {
        return String.format(
                "当前商品：%s（初始标价 ¥%.2f）\n" +
                "用户当前出价：¥%.2f\n" +
                "你的当前报价：¥%.2f\n" +
                "谈判轮数：第 %d 轮\n" +
                (accepted ? "**重要：用户出价已达到目标利润价，你应该接受这个报价并恭喜成交！**\n" : "") +
                (finalOffer ? "**重要：这是最后一轮，请给出最终答复。**\n" : "") +
                "\n请根据以上信息，用专业、热情的语气回复用户。",
                product.getName(), product.getPrice(),
                request.getUserOffer(),
                agentOffer,
                round
        );
    }

    /**
     * AI 调用失败时的降级回复
     */
    private String buildFallbackResponse(Product product, NegotiateRequest request,
                                         double agentOffer, boolean accepted,
                                         boolean finalOffer, int round) {
        if (accepted) {
            return String.format("成交！¥%.2f 这个价格就给您了。正在为您锁定【%s】的价格并生成订单，感谢您的诚意！",
                    agentOffer, product.getName());
        }
        if (finalOffer) {
            return String.format("这真的是我能给的最低价 ¥%.2f 了，已经是成本价了。这款%s成色很好，您觉得可以的话我们就成交吧！",
                    agentOffer, product.getName());
        }
        if (request.getUserOffer() < product.getPrice() * costPriceRatio) {
            return String.format("抱歉，¥%.2f 这个价格实在太低了，我们成本都包不住。这款%s品质有保障，最低能给到 ¥%.2f，您觉得怎么样？",
                    request.getUserOffer(), product.getName(), agentOffer);
        }
        return String.format("您的出价 ¥%.2f 我收到了。看您这么有诚意，我可以给到 ¥%.2f。这款%s%s，这个价格真的很实在了！",
                request.getUserOffer(), agentOffer, product.getName(), product.getDescription());
    }

    private String formatPrice(double price) {
        return String.format("%.2f", price);
    }
}
