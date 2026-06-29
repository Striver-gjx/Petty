package com.petty.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawalDTO {
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "50.00", message = "最低提现金额50元")
    private BigDecimal amount;
}
