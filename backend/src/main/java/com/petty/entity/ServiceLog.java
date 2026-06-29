package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_log")
public class ServiceLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long sitterId;
    private String logType;
    private String description;
    private String photoUrls;
    private String videoUrl;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private String petStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
