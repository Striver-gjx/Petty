package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sitter_withdrawal")
public class SitterWithdrawal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sitterId;
    private BigDecimal amount;
    private String status;
    private String bankInfo;
    private String transactionNo;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
