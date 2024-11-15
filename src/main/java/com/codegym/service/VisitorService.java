package com.codegym.service;

import java.util.List;
import java.util.Map;

public interface VisitorService {
    void logVisitor(String sessionId);

    void updateLastActiveTime(String sessionId);

    long getActiveVisitors();

    long getVisitorsToday();

    long getVisitorsThisWeek();

    long getVisitorsThisMonth();

    long getVisitorsThisYear();

    void removeSession(String sessionId);

    List<Map<String, Object>> getMonthlyVisitStatistics(int year, int limit);

    List<Map<String, Object>> getRecentVisitStatistics(int limit);

    void addSession(String sessionId);
}