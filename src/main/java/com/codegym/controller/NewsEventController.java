package com.codegym.controller;

import com.codegym.dto.NewsEventDTO;
import com.codegym.service.NewsEventService;
import com.codegym.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/news-events")
public class NewsEventController {

    @Autowired
    private NewsEventService newsEventService;

    @Autowired
    private ImageUploadService imageUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsEventDTO> createNewsEvent(@ModelAttribute NewsEventDTO newsEventDTO,
                                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                newsEventDTO.setImageUrl(imageUrl);
            }
            NewsEventDTO createdNewsEvent = newsEventService.createNewsEvent(newsEventDTO);
            return new ResponseEntity<>(createdNewsEvent, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsEventDTO> updateNewsEvent(@PathVariable Long id,
                                                        @ModelAttribute NewsEventDTO newsEventDTO,
                                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                newsEventDTO.setImageUrl(imageUrl);
            }
            NewsEventDTO updatedNewsEvent = newsEventService.updateNewsEvent(id, newsEventDTO);
            return ResponseEntity.ok(updatedNewsEvent);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNewsEvent(@PathVariable Long id) {
        newsEventService.deleteNewsEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsEventDTO> getNewsEventById(@PathVariable Long id) {
        NewsEventDTO newsEventDTO = newsEventService.getNewsEventById(id);
        return ResponseEntity.ok(newsEventDTO);
    }

    @GetMapping
    public ResponseEntity<Page<NewsEventDTO>> getAllNewsEvents(Pageable pageable) {
        Page<NewsEventDTO> newsEvents = newsEventService.getAllNewsEvents(pageable);
        return ResponseEntity.ok(newsEvents);
    }
}