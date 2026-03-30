package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.repository.IBookRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service xử lý logic nghiệp vụ cho Sách.
 * Hỗ trợ: CRUD, phân trang, sắp xếp, tìm kiếm.
 */
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
public class BookService {

    private final IBookRepository bookRepository;

    // Số sách hiển thị trên mỗi trang
    private static final int PAGE_SIZE = 8;

    /**
     * Lấy danh sách sách có phân trang và sắp xếp.
     */
    public List<Book> getAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        return bookRepository.findAllBooks(
                pageNo != null ? pageNo : 0,
                pageSize != null ? pageSize : PAGE_SIZE,
                sortBy != null ? sortBy : "id");
    }

    /**
     * Lấy danh sách sách có phân trang, tìm kiếm, lọc danh mục + khoảng giá + sắp
     * xếp (cho
     * trang chủ).
     */
    public Page<Book> getBooks(String keyword, Long categoryId, Double minPrice, Double maxPrice, String sort,
            int page) {
        Sort sortBy = Sort.by("id").descending(); // Mặc định mới nhất
        if ("price_asc".equals(sort)) {
            sortBy = Sort.by("price").ascending();
        } else if ("price_desc".equals(sort)) {
            sortBy = Sort.by("price").descending();
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sortBy);
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return bookRepository.searchBooks(cleanKeyword, categoryId, minPrice, maxPrice, pageable);
    }

    /**
     * Lấy chi tiết 1 sách theo ID.
     */
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Thêm sách mới.
     */
    public void addBook(Book book) {
        bookRepository.save(book);
    }

    /**
     * Cập nhật thông tin sách.
     */
    public void updateBook(@NotNull Book book) {
        Book existingBook = bookRepository.findById(book.getId()).orElse(null);
        Objects.requireNonNull(existingBook).setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        existingBook.setImageUrl(book.getImageUrl());
        bookRepository.save(existingBook);
    }

    /**
     * Xóa sách theo ID.
     */
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    /**
     * Tìm kiếm sách theo từ khóa (title hoặc author).
     */
    public List<Book> searchBook(String keyword) {
        return bookRepository.searchBook(keyword);
    }

    /**
     * Lưu sách (dùng cho cả thêm mới và cập nhật - backward compatible).
     */
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
}
