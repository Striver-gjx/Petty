package com.petty.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceLogVO {
    private Long id;
    private String logType;
    private String description;
    private List<String> photoUrls;
    private String videoUrl;
    private String petStatus;
    private LocalDateTime createdAt;
}
