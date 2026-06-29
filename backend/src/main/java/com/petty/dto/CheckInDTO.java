package com.petty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CheckInDTO {
    @NotNull(message = "经度不能为空")
    private BigDecimal latitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "打卡照片不能为空")
    private String photoUrl;
}
