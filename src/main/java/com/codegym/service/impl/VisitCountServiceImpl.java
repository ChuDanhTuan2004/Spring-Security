package com.codegym.service.impl;

import com.codegym.entity.DailyVisit;
import com.codegym.repository.DailyVisitRepository;
import com.codegym.service.VisitCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class VisitCountServiceImpl implements VisitCountService {

    @Autowired
    private DailyVisitRepository dailyVisitRepository;

    @Override
    @Transactional
    public void incrementVisitCount() {
        LocalDate today = LocalDate.now();
        DailyVisit dailyVisit = dailyVisitRepository.findByDate(today)
                .orElse(new DailyVisit());

        if (dailyVisit.getId() == null) {
            dailyVisit.setDate(today);
            dailyVisit.setVisitCount(1);
        } else {
            dailyVisit.setVisitCount(dailyVisit.getVisitCount() + 1);
        }

        dailyVisitRepository.save(dailyVisit);
    }

    @Override
    public long getVisitCountForToday() {
        return dailyVisitRepository.findByDate(LocalDate.now())
                .map(DailyVisit::getVisitCount)
                .orElse(0L);
    }

    @Override
    public long getVisitCountForCurrentMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1);
        return dailyVisitRepository.findAll().stream()
                .filter(visit -> !visit.getDate().isBefore(startOfMonth) && !visit.getDate().isAfter(endOfMonth))
                .mapToLong(DailyVisit::getVisitCount)
                .sum();
    }

    @Override
    public long getVisitCountForCurrentYear() {
        int currentYear = LocalDate.now().getYear();
        return dailyVisitRepository.findAll().stream()
                .filter(visit -> visit.getDate().getYear() == currentYear)
                .mapToLong(DailyVisit::getVisitCount)
                .sum();
    }

    @Override
    public long getVisitCountForCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        return dailyVisitRepository.findAll().stream()
                .filter(visit -> !visit.getDate().isBefore(startOfWeek) && !visit.getDate().isAfter(endOfWeek))
                .mapToLong(DailyVisit::getVisitCount)
                .sum();
    }
}