package com.petty.vo;

import lombok.Data;

@Data
public class OrderPetVO {
    private Long petId;
    private String petName;
    private String species;
    private String avatarUrl;
    private String specialNotes;
}
