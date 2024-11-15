package com.codegym.dto;

import lombok.Data;

@Data
public class BookSearchRequest extends PaginationRequest {
    private String keyword;
    private Long subcategoryId;
    private Integer publicationYear;
}