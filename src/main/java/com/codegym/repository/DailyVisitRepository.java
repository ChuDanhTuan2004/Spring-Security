package com.codegym.repository;

import com.codegym.entity.DailyVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyVisitRepository extends JpaRepository<DailyVisit, Long> {
    Optional<DailyVisit> findByDate(LocalDate date);
}