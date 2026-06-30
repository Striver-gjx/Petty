package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
    @JsonIgnore
    private String openid;
    @JsonIgnore
    private BigDecimal walletBalance;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
