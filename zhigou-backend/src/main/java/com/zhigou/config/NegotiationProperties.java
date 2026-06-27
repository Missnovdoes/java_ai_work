package com.zhigou.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "negotiation")
public class NegotiationProperties {

    /** 成本价占标价的比例（当数据库未记录成本价时使用） */
    private double costPriceRatio = 0.7;

    /** 能接受的最低利润比（标价 × 此比例 = 目标成交价） */
    private double minProfitRatio = 0.85;

    /** 最大谈判轮数 */
    private int maxRounds = 5;

    /** 每轮让步步长（元） */
    private int stepDecrease = 10;

    public double getCostPriceRatio() { return costPriceRatio; }
    public void setCostPriceRatio(double costPriceRatio) { this.costPriceRatio = costPriceRatio; }
    public double getMinProfitRatio() { return minProfitRatio; }
    public void setMinProfitRatio(double minProfitRatio) { this.minProfitRatio = minProfitRatio; }
    public int getMaxRounds() { return maxRounds; }
    public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }
    public int getStepDecrease() { return stepDecrease; }
    public void setStepDecrease(int stepDecrease) { this.stepDecrease = stepDecrease; }
}
