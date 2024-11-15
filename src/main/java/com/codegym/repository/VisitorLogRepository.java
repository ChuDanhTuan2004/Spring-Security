package com.codegym.repository;

import com.codegym.entity.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM VisitorLog v WHERE v.lastActiveTime > :time")
    long countActiveVisitors(LocalDateTime time);
    Optional<VisitorLog> findBySessionId(String sessionId);
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM VisitorLog v WHERE v.visitTime >= :startTime AND v.visitTime < :endTime")
    long countVisitorsBetween(LocalDateTime startTime, LocalDateTime endTime);
    @Query(value = "SELECT * FROM visitor_logs ORDER BY visit_time DESC LIMIT :limit", nativeQuery = true)
    List<VisitorLog> findTopNByOrderByVisitTimeDesc(@Param("limit") int limit);
}