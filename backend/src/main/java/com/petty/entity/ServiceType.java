package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_type")
public class ServiceType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private String iconUrl;
    private Integer baseDurationMin;
    private BigDecimal basePrice;
    private BigDecimal extraPetPrice;
    private String applicableSpecies;
    private String checklistTemplate;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
