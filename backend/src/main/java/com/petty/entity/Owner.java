package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("owner")
public class Owner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String phone;
    private String avatarUrl;
    private Integer gender;
    private String address;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @JsonIgnore
    private String lockInfo;
    private String emergencyContact;
    private String emergencyPhone;
    private String memberLevel;
    @JsonIgnore
    private BigDecimal balance;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    @JsonIgnore
    private String openid;
    @JsonIgnore
    private String unionid;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
