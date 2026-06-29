package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerType;
    private Long targetId;
    private BigDecimal rating;
    private String content;
    private String photoUrls;
    private String tags;
    private Integer isAnonymous;
    private String replyContent;
    private LocalDateTime replyAt;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
