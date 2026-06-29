package com.petty.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceTypeVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String iconUrl;
    private Integer baseDurationMin;
    private BigDecimal basePrice;
    private BigDecimal extraPetPrice;
    private List<String> applicableSpecies;
    private List<String> checklistTemplate;
    private Integer sortOrder;
}
