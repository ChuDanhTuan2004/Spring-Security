package com.codegym.repository;

import com.codegym.entity.Book;
import com.codegym.entity.Category;
import com.codegym.entity.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:subcategoryId IS NULL OR b.subcategory.subcategoryId = :subcategoryId) " +
            "AND (:publicationYear IS NULL OR b.publicationYear = :publicationYear)")
    Page<Book> findByCriteria(@Param("keyword") String keyword,
                              @Param("subcategoryId") Long subcategoryId,
                              @Param("publicationYear") Integer publicationYear,
                              Pageable pageable);

    Long countBySubcategoryIn(List<Subcategory> subcategories);

    long countBySubcategory_Category(Category category);
}
