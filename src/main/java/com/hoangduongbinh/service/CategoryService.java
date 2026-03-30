package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xử lý logic nghiệp vụ cho Danh mục sản phẩm.
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Lấy tất cả danh mục (dùng cho dropdown lọc, form thêm sản phẩm).
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Lấy danh mục theo ID.
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * Lưu danh mục (thêm mới hoặc cập nhật).
     */
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Xóa danh mục theo ID.
     */
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
