package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.repository.IBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho BookService.
 * Sử dụng Mockito để mock IBookRepository.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = Book.builder()
                .id(1L)
                .title("Java Programming")
                .author("James Gosling")
                .price(150000.0)
                .stock(10)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("Spring Boot in Action")
                .author("Craig Walls")
                .price(200000.0)
                .stock(5)
                .build();
    }

    // ==================== getAllBooks ====================

    @Test
    @DisplayName("getAllBooks - trả về danh sách sách từ repository")
    void getAllBooks_ShouldReturnListFromRepository() {
        List<Book> expected = Arrays.asList(book1, book2);
        when(bookRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(expected));

        List<Book> result = bookService.getAllBooks(0, 8, "id");

        assertEquals(2, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
        verify(bookRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("getAllBooks - sử dụng default params khi null")
    void getAllBooks_WithNullParams_ShouldUseDefaults() {
        when(bookRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book1)));

        List<Book> result = bookService.getAllBooks(null, null, null);

        assertNotNull(result);
        verify(bookRepository).findAll(any(PageRequest.class));
    }

    // ==================== getBooks ====================

    @Test
    @DisplayName("getBooks - sort price_asc sắp xếp theo giá tăng dần")
    void getBooks_WithPriceAscSort_ShouldSortAscending() {
        Page<Book> page = new PageImpl<>(List.of(book1, book2));
        when(bookRepository.searchBooks(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<Book> result = bookService.getBooks(null, null, null, null, "price_asc", 0);

        assertEquals(2, result.getContent().size());
        verify(bookRepository).searchBooks(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("getBooks - sort price_desc sắp xếp theo giá giảm dần")
    void getBooks_WithPriceDescSort_ShouldSortDescending() {
        Page<Book> page = new PageImpl<>(List.of(book2, book1));
        when(bookRepository.searchBooks(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<Book> result = bookService.getBooks(null, null, null, null, "price_desc", 0);

        assertNotNull(result);
        verify(bookRepository).searchBooks(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("getBooks - keyword rỗng được clean thành null")
    void getBooks_WithEmptyKeyword_ShouldCleanToNull() {
        Page<Book> page = new PageImpl<>(List.of(book1));
        when(bookRepository.searchBooks(isNull(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        bookService.getBooks("   ", null, null, null, null, 0);

        verify(bookRepository).searchBooks(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    // ==================== getBookById ====================

    @Test
    @DisplayName("getBookById - tìm thấy sách")
    void getBookById_WhenFound_ShouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Optional<Book> result = bookService.getBookById(1L);

        assertTrue(result.isPresent());
        assertEquals("Java Programming", result.get().getTitle());
    }

    @Test
    @DisplayName("getBookById - không tìm thấy → Optional.empty")
    void getBookById_WhenNotFound_ShouldReturnEmpty() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(999L);

        assertFalse(result.isPresent());
    }

    // ==================== addBook ====================

    @Test
    @DisplayName("addBook - lưu sách qua repository")
    void addBook_ShouldSaveViaRepository() {
        bookService.addBook(book1);

        verify(bookRepository).save(book1);
    }

    // ==================== updateBook ====================

    @Test
    @DisplayName("updateBook - cập nhật các trường title, author, price, category, imageUrl")
    void updateBook_ShouldUpdateAllFields() {
        Book updatedBook = Book.builder()
                .id(1L)
                .title("Updated Title")
                .author("Updated Author")
                .price(300000.0)
                .imageUrl("/images/new.jpg")
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        bookService.updateBook(updatedBook);

        assertEquals("Updated Title", book1.getTitle());
        assertEquals("Updated Author", book1.getAuthor());
        assertEquals(300000.0, book1.getPrice());
        assertEquals("/images/new.jpg", book1.getImageUrl());
        verify(bookRepository).save(book1);
    }

    @Test
    @DisplayName("updateBook - ID không tồn tại → NullPointerException")
    void updateBook_WhenNotFound_ShouldThrowException() {
        Book nonExistent = Book.builder().id(999L).title("Test").build();
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> bookService.updateBook(nonExistent));
    }

    // ==================== deleteBookById ====================

    @Test
    @DisplayName("deleteBookById - gọi repository.deleteById")
    void deleteBookById_ShouldCallRepository() {
        bookService.deleteBookById(1L);

        verify(bookRepository).deleteById(1L);
    }

    // ==================== searchBook ====================

    @Test
    @DisplayName("searchBook - trả về kết quả từ repository")
    void searchBook_ShouldReturnResultsFromRepository() {
        when(bookRepository.searchBook("Java")).thenReturn(List.of(book1));

        List<Book> result = bookService.searchBook("Java");

        assertEquals(1, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
    }

    // ==================== saveBook ====================

    @Test
    @DisplayName("saveBook - lưu và trả về entity")
    void saveBook_ShouldSaveAndReturn() {
        when(bookRepository.save(book1)).thenReturn(book1);

        Book result = bookService.saveBook(book1);

        assertEquals(book1, result);
        verify(bookRepository).save(book1);
    }
}
