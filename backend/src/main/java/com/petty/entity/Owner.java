package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    private String lockInfo;
    private String emergencyContact;
    private String emergencyPhone;
    private String memberLevel;
    private BigDecimal balance;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private String openid;
    private String unionid;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
