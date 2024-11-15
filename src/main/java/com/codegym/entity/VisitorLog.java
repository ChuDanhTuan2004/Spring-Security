package com.codegym.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitor_logs")
public class VisitorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime visitTime;

    @Column(nullable = false)
    private LocalDateTime lastActiveTime;

    private String ipAddress;
    private String device;
    private String browser;
    private String location;
}