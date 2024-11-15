package com.codegym.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false)
    private String title;

    private String author;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private String description;
    private String filePath;
    private String thumbnail;

    @ManyToOne
    @JoinColumn(name = "subcategory_id", nullable = false)
    @JsonIgnore
    private Subcategory subcategory;
}
