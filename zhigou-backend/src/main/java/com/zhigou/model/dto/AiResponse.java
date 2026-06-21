package com.zhigou.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {

    /** AI 回复的文本内容 */
    private String response;

    /** 智能体给出的新报价 (null 表示接受用户出价) */
    private Double price;

    /** 谈判是否已成交 */
    @Builder.Default
    private boolean accepted = false;

    /** 当前谈判轮数 */
    private int round;

    /** 是否为最终报价 */
    @Builder.Default
    private boolean finalOffer = false;
}
