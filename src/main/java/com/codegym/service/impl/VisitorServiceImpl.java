package com.codegym.service.impl;

import com.codegym.entity.VisitorLog;
import com.codegym.repository.VisitorLogRepository;
import com.codegym.service.VisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VisitorServiceImpl implements VisitorService {
    @Autowired
    private VisitorLogRepository visitorLogRepository;

    private Map<String, LocalDateTime> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void logVisitor(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        if (!activeSessions.containsKey(sessionId)) {
            activeSessions.put(sessionId, now);

            VisitorLog log = new VisitorLog();
            log.setSessionId(sessionId);
            log.setVisitTime(now);
            log.setLastActiveTime(now);
            visitorLogRepository.save(log);
        }
    }

    @Override
    public void updateLastActiveTime(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        activeSessions.put(sessionId, now);

        visitorLogRepository.findBySessionId(sessionId)
                .ifPresent(log -> {
                    log.setLastActiveTime(now);
                    visitorLogRepository.save(log);
                });
    }

    @Override
    public long getActiveVisitors() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        return activeSessions.values().stream()
                .filter(lastActiveTime -> lastActiveTime.isAfter(thirtyMinutesAgo))
                .count();
    }

    @Override
    public long getVisitorsToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long count = visitorLogRepository.countVisitorsBetween(startOfDay, endOfDay);
        log.debug("Khách truy cập hôm nay (từ {} đến {}): {}", startOfDay, endOfDay, count);
        return count;
    }

    @Override
    public long getVisitorsThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(ChronoField.DAY_OF_WEEK, 1).truncatedTo(ChronoUnit.DAYS);
        return visitorLogRepository.countVisitorsBetween(startOfWeek, now);
    }

    @Override
    public long getVisitorsThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        long count = visitorLogRepository.countVisitorsBetween(startOfMonth, endOfMonth);
        log.debug("Khách truy cập tháng này (từ {} đến {}): {}", startOfMonth, endOfMonth, count);
        return count;
    }

    @Override
    public long getVisitorsThisYear() {
        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfYear = startOfYear.plusYears(1);
        long count = visitorLogRepository.countVisitorsBetween(startOfYear, endOfYear);
        log.debug("Khách truy cập năm nay (từ {} đến {}): {}", startOfYear, endOfYear, count);
        return count;
    }

    @Override
    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    @Override
    public List<Map<String, Object>> getMonthlyVisitStatistics(int year, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = startOfYear.plusYears(1);

        for (int month = 1; month <= Math.min(12, limit); month++) {
            LocalDateTime startOfMonth = startOfYear.withMonth(month);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

            long visits = visitorLogRepository.countVisitorsBetween(startOfMonth, endOfMonth);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", "T" + month);
            monthData.put("visits", visits);
            result.add(monthData);

            if (endOfMonth.isAfter(endOfYear)) {
                break;
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentVisitStatistics(int limit) {
        List<VisitorLog> recentLogs = visitorLogRepository.findTopNByOrderByVisitTimeDesc(limit);
        return recentLogs.stream().map(this::convertVisitorLogToMap).collect(Collectors.toList());
    }

    @Override
    public void addSession(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        activeSessions.put(sessionId, now);

        VisitorLog log = new VisitorLog();
        log.setSessionId(sessionId);
        log.setVisitTime(now);
        log.setLastActiveTime(now);
        visitorLogRepository.save(log);
    }

    private Map<String, Object> convertVisitorLogToMap(VisitorLog log) {
        Map<String, Object> visitData = new HashMap<>();
        visitData.put("session_id", log.getSessionId());
        visitData.put("visit_time", log.getVisitTime().toString());
        visitData.put("last_active_time", getRelativeTime(log.getLastActiveTime()));

        putIfNotNull(visitData, "ip_address", log.getIpAddress());
        putIfNotNull(visitData, "device", log.getDevice());
        putIfNotNull(visitData, "browser", log.getBrowser());
        putIfNotNull(visitData, "location", log.getLocation());

        return visitData;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private String getRelativeTime(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());
        if (duration.toDays() > 0) {
            return duration.toDays() + " days ago";
        } else if (duration.toHours() > 0) {
            return duration.toHours() + " hours ago";
        } else if (duration.toMinutes() > 0) {
            return duration.toMinutes() + " minutes ago";
        } else {
            return "just now";
        }
    }
}