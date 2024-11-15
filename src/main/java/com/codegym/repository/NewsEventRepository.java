package com.codegym.repository;

import com.codegym.entity.NewsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsEventRepository extends JpaRepository<NewsEvent, Long> {
}