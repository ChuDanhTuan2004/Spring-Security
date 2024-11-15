package com.codegym.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookDTO {
    private Long bookId;
    private String title;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private String description;
    private String filePath;
    private String thumbnail;
    private Long subcategoryId;
    private String subcategoryName;
    private String author;
}