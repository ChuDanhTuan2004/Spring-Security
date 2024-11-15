package com.codegym.service;

public interface VisitCountService {
    void incrementVisitCount();
    long getVisitCountForToday();
    long getVisitCountForCurrentWeek();

    long getVisitCountForCurrentMonth();
    long getVisitCountForCurrentYear();
}