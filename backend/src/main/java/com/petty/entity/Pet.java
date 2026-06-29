package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal weight;
    private String avatarUrl;
    private String personality;
    private String dietInfo;
    private String healthNotes;
    private String vaccineInfo;
    private LocalDate lastVaccineDate;
    private String specialInstructions;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
