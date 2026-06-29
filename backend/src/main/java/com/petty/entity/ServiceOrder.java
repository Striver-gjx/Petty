package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("service_order")
public class ServiceOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long ownerId;
    private Long sitterId;
    private Long serviceTypeId;
    private String serviceAddress;
    private BigDecimal serviceLatitude;
    private BigDecimal serviceLongitude;
    private LocalDate scheduledDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Integer petCount;
    private BigDecimal serviceAmount;
    private BigDecimal extraAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal platformCommission;
    private BigDecimal sitterIncome;
    private String status;
    private String paymentStatus;
    private String cancelReason;
    private String cancelBy;
    private String remark;
    private String lockPassword;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
