package com.petty.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderVO {
    private String serviceAddress;
    private List<OrderPetVO> pets;
    private String sitterPhone;
    private String remark;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private List<ServiceLogVO> serviceLogs;
}
