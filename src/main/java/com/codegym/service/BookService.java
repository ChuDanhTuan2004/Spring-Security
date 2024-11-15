package com.codegym.service;

import com.codegym.dto.BookDTO;
import com.codegym.dto.BookSearchRequest;
import com.codegym.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.Resource;

public interface BookService {
    BookDTO saveBookWithImage(Book book, MultipartFile image) throws IOException;

    List<Book> findAll();
    Optional<Book> findById(Long id);
    Book save(Book book);
    void deleteById(Long id);

    Page<BookDTO> searchBooks(BookSearchRequest request);

    BookDTO getBookById(Long id);

    BookDTO convertToDTO(Book book);

    int importBooksFromExcel(MultipartFile file) throws IOException;

    // Add this method to save all books
    void saveAll(List<Book> books);

    Resource generateExcelTemplate() throws IOException;

    Map<String, Long> getDocumentStatistics();

}
