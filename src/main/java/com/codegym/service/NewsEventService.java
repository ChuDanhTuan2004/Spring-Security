package com.codegym.service;

import com.codegym.dto.NewsEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsEventService {
    NewsEventDTO createNewsEvent(NewsEventDTO newsEventDTO);
    NewsEventDTO updateNewsEvent(Long id, NewsEventDTO newsEventDTO);
    void deleteNewsEvent(Long id);
    NewsEventDTO getNewsEventById(Long id);
    Page<NewsEventDTO> getAllNewsEvents(Pageable pageable);
}