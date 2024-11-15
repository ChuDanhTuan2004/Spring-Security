package com.codegym.service.impl;

import com.codegym.dto.NewsEventDTO;
import com.codegym.entity.NewsEvent;
import com.codegym.repository.NewsEventRepository;
import com.codegym.service.NewsEventService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NewsEventServiceImpl implements NewsEventService {

    @Autowired
    private NewsEventRepository newsEventRepository;

    @Override
    public NewsEventDTO createNewsEvent(NewsEventDTO newsEventDTO) {
        NewsEvent newsEvent = new NewsEvent();
        BeanUtils.copyProperties(newsEventDTO, newsEvent);
        newsEvent = newsEventRepository.save(newsEvent);
        BeanUtils.copyProperties(newsEvent, newsEventDTO);
        return newsEventDTO;
    }

    @Override
    public NewsEventDTO updateNewsEvent(Long id, NewsEventDTO newsEventDTO) {
        NewsEvent newsEvent = newsEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News/Event not found"));
        BeanUtils.copyProperties(newsEventDTO, newsEvent, "id");
        newsEvent = newsEventRepository.save(newsEvent);
        BeanUtils.copyProperties(newsEvent, newsEventDTO);
        return newsEventDTO;
    }

    @Override
    public void deleteNewsEvent(Long id) {
        newsEventRepository.deleteById(id);
    }

    @Override
    public NewsEventDTO getNewsEventById(Long id) {
        NewsEvent newsEvent = newsEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News/Event not found"));
        NewsEventDTO newsEventDTO = new NewsEventDTO();
        BeanUtils.copyProperties(newsEvent, newsEventDTO);
        return newsEventDTO;
    }

    @Override
    public Page<NewsEventDTO> getAllNewsEvents(Pageable pageable) {
        return newsEventRepository.findAll(pageable)
                .map(newsEvent -> {
                    NewsEventDTO dto = new NewsEventDTO();
                    BeanUtils.copyProperties(newsEvent, dto);
                    return dto;
                });
    }
}