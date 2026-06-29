package com.petty.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReviewCreateDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "综合评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private BigDecimal rating;

    @Min(value = 1) @Max(value = 5)
    private BigDecimal ratingPunctuality;

    @Min(value = 1) @Max(value = 5)
    private BigDecimal ratingProfessionalism;

    @Min(value = 1) @Max(value = 5)
    private BigDecimal ratingAttitude;

    @Min(value = 1) @Max(value = 5)
    private BigDecimal ratingPetCare;

    @Min(value = 1) @Max(value = 5)
    private BigDecimal ratingEnvironment;

    private String content;
    private List<String> photoUrls;
    private List<String> tags;
    private Boolean isAnonymous;
}
