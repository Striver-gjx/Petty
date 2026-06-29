package com.petty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceLogCreateDTO {
    @NotNull(message = "记录类型不能为空")
    private String logType;

    private String description;
    private List<String> photoUrls;
    private String videoUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String petStatus;
}
