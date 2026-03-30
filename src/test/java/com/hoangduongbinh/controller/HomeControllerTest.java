package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.service.BookService;
import com.hoangduongbinh.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho HomeController.
 */
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private CategoryService categoryService;

    private Book testBook;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(1L).name("IT").build();
        testBook = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Author")
                .price(100000.0)
                .stock(5)
                .category(testCategory)
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET / → 200 + model chứa books, categories")
    void home_ShouldReturn200WithModel() throws Exception {
        Page<Book> bookPage = new PageImpl<>(List.of(testBook));
        List<Category> categories = Arrays.asList(testCategory);

        when(bookService.getBooks(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(bookPage);
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /home → 200 + home view")
    void homeAlternate_ShouldReturn200() throws Exception {
        Page<Book> bookPage = new PageImpl<>(List.of(testBook));
        when(bookService.getBooks(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(bookPage);
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /books/{id} → 200 + book-detail view")
    void bookDetail_WhenFound_ShouldReturn200() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-detail"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /books/{id} - không tồn tại → redirect /")
    void bookDetail_WhenNotFound_ShouldRedirect() throws Exception {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
