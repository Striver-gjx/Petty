package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long ownerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionNo;
    private String outTradeNo;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
