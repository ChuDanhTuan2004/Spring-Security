package com.codegym.controller;

import com.codegym.dto.BookCreateRequest;
import com.codegym.dto.BookDTO;
import com.codegym.dto.BookSearchRequest;
import com.codegym.entity.Book;
import com.codegym.entity.Subcategory;
import com.codegym.service.BookService;
import com.codegym.service.ImageUploadService;
import com.codegym.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StreamUtils;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private SubCategoryService subCategoryService;

    @GetMapping
    public ResponseEntity<Page<BookDTO>> searchBooks(BookSearchRequest request) {
        Page<BookDTO> books = bookService.searchBooks(request);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> createBook(@ModelAttribute BookCreateRequest request) {
        try {
            // Upload thumbnail image or set default
            String thumbnailUrl = "http://res.cloudinary.com/dedh8hajg/image/upload/5d794270-707d-4cf6-a208-c90d8a8ade39_BIA_SACH_THANH_DO.jpg";
            if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
                thumbnailUrl = imageUploadService.uploadImage(request.getThumbnail());
            }

            // Create Book object from request
            Book book = new Book();
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            book.setPublisher(request.getPublisher());
            book.setPublicationYear(request.getPublicationYear());
            book.setIsbn(request.getIsbn());
            book.setDescription(request.getDescription());
            book.setFilePath(request.getFilePath());
            book.setThumbnail(thumbnailUrl);

            // Set subcategory
            Subcategory subcategory = subCategoryService.findById(request.getSubcategoryId());
            book.setSubcategory(subcategory);

            // Save book
            Book savedBook = bookService.save(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @ModelAttribute BookCreateRequest request) {
        try {
            // Get existing book
            Book book = bookService.findById(id).orElse(null);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }

            // Update thumbnail image if provided, otherwise keep existing or set default
            if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
                String thumbnailUrl = imageUploadService.uploadImage(request.getThumbnail());
                book.setThumbnail(thumbnailUrl);
            } else if (book.getThumbnail() == null || book.getThumbnail().isEmpty()) {
                book.setThumbnail("http://res.cloudinary.com/dedh8hajg/image/upload/5d794270-707d-4cf6-a208-c90d8a8ade39_BIA_SACH_THANH_DO.jpg");
            }

            // Update Book object from request
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            book.setPublisher(request.getPublisher());
            book.setPublicationYear(request.getPublicationYear());
            book.setIsbn(request.getIsbn());
            book.setDescription(request.getDescription());
            book.setFilePath(request.getFilePath());

            // Update subcategory if changed
            if (request.getSubcategoryId() != null) {
                Subcategory subcategory = subCategoryService.findById(request.getSubcategoryId());
                book.setSubcategory(subcategory);
            }

            // Save updated book
            Book updatedBook = bookService.save(book);
            BookDTO bookDTO = bookService.convertToDTO(updatedBook);
            return ResponseEntity.ok(bookDTO);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/image")
    public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return imageUploadService.uploadImage(file);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importBooksFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        try {
            int importedCount = bookService.importBooksFromExcel(file);
            return ResponseEntity.ok("Successfully imported " + importedCount + " books");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import books: " + e.getMessage());
        }
    }

//    @GetMapping("/template")
//    public ResponseEntity<Resource> downloadTemplate() throws IOException {
//        Resource resource = bookService.generateExcelTemplate();
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=book_import_template.xlsx")
//                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
//                .body(resource);
//    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Resource resource = bookService.generateExcelTemplate();
        byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("book_import_template.xlsx").build());
        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/statistics/documents")
    public ResponseEntity<?> getDocumentStatistics() {
        try {
            Map<String, Long> statistics = bookService.getDocumentStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching document statistics");
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}