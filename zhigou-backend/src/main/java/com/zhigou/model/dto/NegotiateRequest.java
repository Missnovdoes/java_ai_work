package com.zhigou.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NegotiateRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "出价不能为空")
    @Positive(message = "出价必须大于0")
    private Double userOffer;
}
