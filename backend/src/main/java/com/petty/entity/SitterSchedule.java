package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("sitter_schedule")
public class SitterSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sitterId;
    private LocalDate scheduleDate;
    private LocalTime timeSlotStart;
    private LocalTime timeSlotEnd;
    private Integer maxOrders;
    private Integer bookedOrders;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
