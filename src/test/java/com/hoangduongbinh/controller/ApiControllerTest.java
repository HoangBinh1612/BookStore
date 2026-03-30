package com.hoangduongbinh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.repository.CategoryRepository;
import com.hoangduongbinh.service.BookService;
import com.hoangduongbinh.viewmodel.BookPostVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho ApiController (REST API Books).
 * Sử dụng @WebMvcTest + MockMvc.
 */
@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(1L).name("IT").build();
        testBook = Book.builder()
                .id(1L)
                .title("Java Book")
                .author("Author")
                .price(150000.0)
                .stock(10)
                .category(testCategory)
                .build();
    }

    // ==================== GET /api/books ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/books → 200 + danh sách JSON")
    void getAllBooks_ShouldReturn200WithList() throws Exception {
        when(bookService.getAllBooks(0, 100, "id")).thenReturn(Arrays.asList(testBook));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Book"))
                .andExpect(jsonPath("$[0].author").value("Author"))
                .andExpect(jsonPath("$[0].price").value(150000.0));
    }

    // ==================== GET /api/books/{id} ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/{id} → 200 + sách JSON")
    void getBookById_WhenFound_ShouldReturn200() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Book"))
                .andExpect(jsonPath("$.categoryName").value("IT"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/{id} - không tồn tại → 404")
    void getBookById_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/books ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/books → 201 + sách mới")
    void createBook_ShouldReturn201() throws Exception {
        BookPostVm postVm = BookPostVm.builder()
                .title("New Book")
                .author("New Author")
                .price(200000.0)
                .stock(5)
                .categoryId(1L)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(bookService.saveBook(any(Book.class))).thenReturn(testBook);

        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postVm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").exists());
    }

    // ==================== PUT /api/books/{id} ====================

    @Test
    @WithMockUser
    @DisplayName("PUT /api/books/{id} → 200 + sách cập nhật")
    void updateBook_WhenFound_ShouldReturn200() throws Exception {
        BookPostVm postVm = BookPostVm.builder()
                .title("Updated Book")
                .author("Updated Author")
                .price(300000.0)
                .stock(20)
                .build();

        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(bookService.saveBook(any(Book.class))).thenReturn(testBook);

        mockMvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postVm)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/books/{id} - không tồn tại → 404")
    void updateBook_WhenNotFound_ShouldReturn404() throws Exception {
        BookPostVm postVm = BookPostVm.builder()
                .title("Updated")
                .author("Author")
                .price(100.0)
                .stock(1)
                .build();

        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postVm)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/books/{id} ====================

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/books/{id} → 204")
    void deleteBook_WhenFound_ShouldReturn204() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        mockMvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBookById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/books/{id} - không tồn tại → 404")
    void deleteBook_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/books/999").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
