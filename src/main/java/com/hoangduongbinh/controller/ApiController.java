package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.repository.CategoryRepository;
import com.hoangduongbinh.service.BookService;
import com.hoangduongbinh.viewmodel.BookGetVm;
import com.hoangduongbinh.viewmodel.BookPostVm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST API Controller cho Book.
 *
 * Endpoints:
 * GET /api/books - Lấy danh sách sách
 * GET /api/books/{id} - Lấy chi tiết 1 sách
 * POST /api/books - Thêm sách mới
 * PUT /api/books/{id} - Cập nhật sách
 * DELETE /api/books/{id} - Xóa sách
 */
@RestController
@RequestMapping("/api/books")
public class ApiController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * GET /api/books - Lấy danh sách tất cả sách.
     */
    @GetMapping
    public ResponseEntity<List<BookGetVm>> getAllBooks() {
        List<Book> books = bookService.getAllBooks(0, 100, "id");
        List<BookGetVm> result = books.stream()
                .map(this::toBookGetVm)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/books/{id} - Lấy chi tiết sách theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookGetVm> getBookById(@PathVariable("id") Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(b -> ResponseEntity.ok(toBookGetVm(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/books - Thêm sách mới.
     */
    @PostMapping
    public ResponseEntity<BookGetVm> createBook(@Valid @RequestBody BookPostVm bookPostVm) {
        Book book = new Book();
        book.setTitle(bookPostVm.getTitle());
        book.setAuthor(bookPostVm.getAuthor());
        book.setPrice(bookPostVm.getPrice());
        book.setStock(bookPostVm.getStock() != null ? bookPostVm.getStock() : 0);

        if (bookPostVm.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookPostVm.getCategoryId()).orElse(null);
            book.setCategory(category);
        }

        Book saved = bookService.saveBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBookGetVm(saved));
    }

    /**
     * PUT /api/books/{id} - Cập nhật thông tin sách.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookGetVm> updateBook(@PathVariable("id") Long id,
            @Valid @RequestBody BookPostVm bookPostVm) {
        Optional<Book> existing = bookService.getBookById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book = existing.get();
        book.setTitle(bookPostVm.getTitle());
        book.setAuthor(bookPostVm.getAuthor());
        book.setPrice(bookPostVm.getPrice());
        book.setStock(bookPostVm.getStock() != null ? bookPostVm.getStock() : 0);

        if (bookPostVm.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookPostVm.getCategoryId()).orElse(null);
            book.setCategory(category);
        }

        Book updated = bookService.saveBook(book);
        return ResponseEntity.ok(toBookGetVm(updated));
    }

    /**
     * DELETE /api/books/{id} - Xóa sách theo ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") Long id) {
        Optional<Book> existing = bookService.getBookById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper: chuyển Book entity → BookGetVm.
     */
    private BookGetVm toBookGetVm(Book book) {
        return BookGetVm.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .stock(book.getStock())
                .build();
    }
}
