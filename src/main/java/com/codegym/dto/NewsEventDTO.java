package com.codegym.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsEventDTO {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
}