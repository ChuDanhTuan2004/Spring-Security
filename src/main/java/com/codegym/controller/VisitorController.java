package com.codegym.controller;

import com.codegym.service.VisitorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {
    private static final Logger logger = LoggerFactory.getLogger(VisitorController.class);
    @Autowired
    private VisitorService visitorService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/active")
    public ResponseEntity<Map<String, Long>> getActiveVisitors(HttpSession session) {
        String sessionId = session.getId();
        logger.info("Processing active visitors for session: {}", sessionId);

        try {
            visitorService.updateLastActiveTime(sessionId);

            Map<String, Long> stats = new HashMap<>();
            stats.put("activeVisitors", visitorService.getActiveVisitors());

            logger.info("Active visitors: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error processing active visitors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }

    @PostMapping("/record-visit")
    public ResponseEntity<Map<String, Long>> recordVisit(HttpSession session) {
        String sessionId = session.getId();
        logger.info("Recording visit for session: {}", sessionId);

        try {
            Boolean counted = (Boolean) session.getAttribute("counted");
            logger.info("Session counted status: {}", counted);

            if (counted == null || !counted) {
                logger.info("New session detected, adding to visitor log");
                visitorService.addSession(sessionId);
                session.setAttribute("counted", true);
                updateActiveVisitors();
            }

            Map<String, Long> stats = new HashMap<>();
            stats.put("visitorsToday", visitorService.getVisitorsToday());
            stats.put("visitorsThisWeek", visitorService.getVisitorsThisWeek());
            stats.put("visitorsThisMonth", visitorService.getVisitorsThisMonth());
            stats.put("visitorsThisYear", visitorService.getVisitorsThisYear());

            logger.info("Visit stats: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error recording visit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }

    @PostMapping("/end-session")
    public void endSession(HttpSession session) {
        visitorService.removeSession(session.getId());
        updateActiveVisitors();
    }

    // This method should be called whenever a new visitor arrives or leaves
    private void updateActiveVisitors() {
        long activeVisitors = visitorService.getActiveVisitors();
        messagingTemplate.convertAndSend("/topic/active-visitors", activeVisitors);
    }

    @GetMapping("/statistics/monthly")
    public ResponseEntity<?> getMonthlyVisitStatistics(
            @RequestParam int year,
            @RequestParam(defaultValue = "12") int limit) {
        try {
            List<Map<String, Object>> monthlyData = visitorService.getMonthlyVisitStatistics(year, limit);
            long totalVisits = monthlyData.stream()
                    .mapToLong(item -> (Long) item.get("visits"))
                    .sum();

            Map<String, Object> response = new HashMap<>();
            response.put("data", monthlyData);
            response.put("total", totalVisits);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching monthly visit statistics");
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/statistics/recent")
    public ResponseEntity<?> getRecentVisitStatistics(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> recentVisits = visitorService.getRecentVisitStatistics(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("data", recentVisits);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching recent visit statistics");
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}