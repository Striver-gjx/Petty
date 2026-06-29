package com.petty.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("order_pet")
public class OrderPet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long petId;
    private String specialNotes;
}
