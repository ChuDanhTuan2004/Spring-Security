package com.codegym.service.impl;

import com.codegym.dto.BookDTO;
import com.codegym.dto.BookSearchRequest;
import com.codegym.entity.Book;
import com.codegym.entity.Category;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.repository.BookRepository;
import com.codegym.repository.CategoryRepository;
import com.codegym.service.BookService;
import com.codegym.service.CategoryService;
import com.codegym.service.ImageUploadService;
import com.codegym.service.SubCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import com.codegym.entity.Subcategory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private SubCategoryService subCategoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public BookDTO saveBookWithImage(Book book, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.uploadImage(image);
            book.setThumbnail(imageUrl);
        }
        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Page<BookDTO> searchBooks(BookSearchRequest request) {
        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Book> books = bookRepository.findByCriteria(
                request.getKeyword(),
                request.getSubcategoryId(),
                request.getPublicationYear(),
                pageable
        );

        return books.map(this::convertToDTO);
    }


    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return convertToDTO(book);
    }

    @Override
    public BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setBookId(book.getBookId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setIsbn(book.getIsbn());
        dto.setDescription(book.getDescription());
        dto.setFilePath(book.getFilePath());
        dto.setThumbnail(book.getThumbnail());
        dto.setSubcategoryId(book.getSubcategory().getSubcategoryId());
        dto.setSubcategoryName(book.getSubcategory().getName());
        return dto;
    }

    @Override
    public int importBooksFromExcel(MultipartFile file) throws IOException {
        List<Book> books = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                try {
                    Book book = new Book();
                    book.setTitle(getCellStringValue(currentRow.getCell(0)));
                    book.setAuthor(getCellStringValue(currentRow.getCell(1)));
                    book.setPublisher(getCellStringValue(currentRow.getCell(2)));
                    book.setPublicationYear(Integer.parseInt(getCellStringValue(currentRow.getCell(3))));
                    book.setIsbn(getCellStringValue(currentRow.getCell(4)));
                    book.setDescription(getCellStringValue(currentRow.getCell(5)));
                    book.setFilePath(getCellStringValue(currentRow.getCell(6)));
                    book.setThumbnail(getCellStringValue(currentRow.getCell(7)));

                    // Assuming subcategory ID is in the last column
                    Long subcategoryId = Long.parseLong(getCellStringValue(currentRow.getCell(8)));
                    Subcategory subcategory = subCategoryService.findById(subcategoryId);
                    if (subcategory == null) {
                        log.warn("Subcategory with id {} not found. Skipping book: {}", subcategoryId, book.getTitle());
                        continue;
                    }
                    book.setSubcategory(subcategory);

                    books.add(book);
                } catch (NumberFormatException | NullPointerException e) {
                    log.error("Error processing row {}: {}", rowNumber, e.getMessage());
                }

                rowNumber++;
            }
        }

        // Save all books
        saveAll(books);

        return books.size();
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // Add this method to save all books
    @Override
    public void saveAll(List<Book> books) {
        bookRepository.saveAll(books);
    }

    @Override
    public Resource generateExcelTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet bookSheet = workbook.createSheet("Sách");
        Sheet subcategorySheet = workbook.createSheet("Danh mục phụ");

        // Create header row for Books sheet
        Row headerRow = bookSheet.createRow(0);
        String[] columns = {"Tiêu đề", "Tác giả", "Nhà xuất bản", "Năm xuất bản", "ISBN", "Mô tả", "Đường dẫn tệp", "URL Ảnh bìa", "Tên danh mục phụ"};

        // Create a cell style for the header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            bookSheet.autoSizeColumn(i);
        }

        // Add an example row
        Row exampleRow = bookSheet.createRow(1);
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        exampleRow.createCell(0).setCellValue("Ví dụ: Tên sách");
        exampleRow.createCell(1).setCellValue("Nguyễn Văn A");
        exampleRow.createCell(2).setCellValue("Nhà xuất bản Trẻ");
        exampleRow.createCell(3).setCellValue(2023);
        exampleRow.createCell(4).setCellValue("978-3-16-148410-0");
        exampleRow.createCell(5).setCellValue("Đây là mô tả ví dụ cho cuốn sách.");
        exampleRow.createCell(6).setCellValue("/đường/dẫn/đến/tệp.pdf");
        exampleRow.createCell(7).setCellValue("http://res.cloudinary.com/dedh8hajg/image/upload/5d794270-707d-4cf6-a208-c90d8a8ade39_BIA_SACH_THANH_DO.jpg");
        exampleRow.createCell(8).setCellValue("Chọn danh mục phụ");

        for (Cell cell : exampleRow) {
            cell.setCellStyle(dataStyle);
        }

        // Get all subcategories
        List<Subcategory> subcategories = subCategoryService.findAll();

        // Create Subcategories sheet
        Row subcategoryHeaderRow = subcategorySheet.createRow(0);
        subcategoryHeaderRow.createCell(0).setCellValue("Tên danh mục phụ");
        subcategoryHeaderRow.createCell(1).setCellValue("ID danh mục phụ");
        subcategoryHeaderRow.getCell(0).setCellStyle(headerStyle);
        subcategoryHeaderRow.getCell(1).setCellStyle(headerStyle);

        for (int i = 0; i < subcategories.size(); i++) {
            Row row = subcategorySheet.createRow(i + 1);
            Cell nameCell = row.createCell(0);
            Cell idCell = row.createCell(1);
            nameCell.setCellValue(subcategories.get(i).getName());
            idCell.setCellValue(subcategories.get(i).getSubcategoryId());
            nameCell.setCellStyle(dataStyle);
            idCell.setCellStyle(dataStyle);
        }

        subcategorySheet.autoSizeColumn(0);
        subcategorySheet.autoSizeColumn(1);

        // Create name for the subcategory list
        Name namedCell = workbook.createName();
        namedCell.setNameName("DanhSachDanhMucPhu");
        namedCell.setRefersToFormula("'Danh mục phụ'!$A$2:$A$" + (subcategories.size() + 1));

        // Create data validation for subcategory column
        DataValidationHelper validationHelper = bookSheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint("DanhSachDanhMucPhu");
        CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 8, 8);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Lỗi", "Vui lòng chọn một danh mục phụ từ danh sách.");

        bookSheet.addValidationData(validation);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] bytes = outputStream.toByteArray();

        return new ByteArrayResource(bytes);
    }

    @Override
    public Map<String, Long> getDocumentStatistics() {
        Map<String, Long> statistics = new HashMap<>();

        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            long count = bookRepository.countBySubcategory_Category(category);
            statistics.put(category.getName(), count);
        }

        return statistics;
    }
}