package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CategoryService.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category cat1;
    private Category cat2;

    @BeforeEach
    void setUp() {
        cat1 = Category.builder().id(1L).name("Công nghệ thông tin").build();
        cat2 = Category.builder().id(2L).name("Văn học").build();
    }

    @Test
    @DisplayName("getAllCategories - trả về list từ repository")
    void getAllCategories_ShouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(cat1, cat2));

        List<Category> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("getCategoryById - tìm thấy → trả về category")
    void getCategoryById_WhenFound_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat1));

        Category result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals("Công nghệ thông tin", result.getName());
    }

    @Test
    @DisplayName("getCategoryById - không tìm thấy → null")
    void getCategoryById_WhenNotFound_ShouldReturnNull() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Category result = categoryService.getCategoryById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("saveCategory - lưu và trả về entity")
    void saveCategory_ShouldSaveAndReturn() {
        when(categoryRepository.save(cat1)).thenReturn(cat1);

        Category result = categoryService.saveCategory(cat1);

        assertEquals(cat1, result);
        verify(categoryRepository).save(cat1);
    }

    @Test
    @DisplayName("deleteCategory - gọi repository.deleteById")
    void deleteCategory_ShouldCallRepository() {
        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
