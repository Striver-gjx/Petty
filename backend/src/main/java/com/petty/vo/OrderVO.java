package com.petty.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long ownerId;
    private Long sitterId;
    private String serviceTypeName;
    private LocalDate scheduledDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private BigDecimal totalAmount;
    private String status;
    private String statusLabel;
    private String sitterName;
    private String sitterAvatarUrl;
    private String ownerNickname;
    private Integer petCount;
    private LocalDateTime createdAt;
}
