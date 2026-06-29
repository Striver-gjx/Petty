package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sitter")
public class Sitter {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String avatarUrl;
    private String idCard;
    private Integer gender;
    private String bio;
    private Integer experienceYears;
    private String certifications;
    private String serviceArea;
    private BigDecimal serviceRadiusKm;
    private BigDecimal homeLatitude;
    private BigDecimal homeLongitude;
    private String acceptedSpecies;
    private Integer maxDailyOrders;
    private BigDecimal basePrice;
    private BigDecimal rating;
    private Integer totalOrders;
    private Integer totalReviews;
    private BigDecimal completionRate;
    private Integer responseTimeMin;
    private String backgroundCheckStatus;
    private LocalDate backgroundCheckDate;
    private String insuranceStatus;
    private String status;
    private String openid;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
