package com.petty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "退款原因不能为空")
    private String reason;
}
