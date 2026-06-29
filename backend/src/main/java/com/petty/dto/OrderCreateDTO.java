package com.petty.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {
    @NotNull(message = "服务类型不能为空")
    private Long serviceTypeId;

    @NotEmpty(message = "宠物列表不能为空")
    private List<Long> petIds;

    @NotNull(message = "预约日期不能为空")
    private String scheduledDate;

    @NotNull(message = "开始时间不能为空")
    private String scheduledStartTime;

    @NotNull(message = "结束时间不能为空")
    private String scheduledEndTime;

    private String serviceAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String remark;
    private Long preferredSitterId;
}
