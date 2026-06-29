package com.petty.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
    private Long id;
    private Long orderId;
    private String reviewerType;
    private String reviewerNickname;
    private String reviewerAvatar;
    private BigDecimal rating;
    private String content;
    private List<String> photoUrls;
    private List<String> tags;
    private String replyContent;
    private LocalDateTime createdAt;
}
