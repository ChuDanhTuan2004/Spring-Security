package com.codegym.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BookCreateRequest {
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private String description;
    private String filePath;
    private MultipartFile thumbnail;
    private Long subcategoryId;
}
