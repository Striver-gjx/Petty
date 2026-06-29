package com.petty.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalVO {
    private Long id;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
