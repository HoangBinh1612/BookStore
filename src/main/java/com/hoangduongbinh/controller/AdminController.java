package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.service.BookService;
import com.hoangduongbinh.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Controller Admin - Quản lý danh mục và sách.
 * Tất cả URL bắt đầu bằng /admin/** → chỉ ADMIN mới truy cập được.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    // =====================================================================
    // QUẢN LÝ DANH MỤC (CATEGORIES)
    // =====================================================================

    @GetMapping({ "", "/" })
    public String adminRoot() {
        return "redirect:/admin/books";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories";
        }
        boolean isNew = (category.getId() == null);
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Đã thêm danh mục mới!" : "Đã cập nhật danh mục!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, Model model) {
        Category cat = categoryService.getCategoryById(id);
        if (cat == null)
            return "redirect:/admin/categories";

        model.addAttribute("category", cat);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa! Danh mục đang có sách.");
        }
        return "redirect:/admin/categories";
    }

    // =====================================================================
    // QUẢN LÝ SÁCH (BOOKS)
    // =====================================================================

    @GetMapping("/books")
    public String listBooks(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        model.addAttribute("bookPage",
                bookService.getBooks(keyword, null, null, null, null, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "admin/books";
    }

    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Thêm sách mới");
        return "admin/book-form";
    }

    @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null)
            return "redirect:/admin/books";

        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Sửa sách");
        return "admin/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(@Valid @ModelAttribute("book") Book book,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle",
                    book.getId() == null ? "Thêm sách mới" : "Sửa sách");
            return "admin/book-form";
        }

        boolean isNew = (book.getId() == null);

        // Xử lý upload ảnh nếu có file được chọn
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Thư mục lưu ảnh tĩnh
                String uploadDir = "src/main/resources/static/images/books/";
                Path uploadPath = Paths.get(uploadDir);

                // Tạo thư mục nếu chưa tồn tại
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Tạo tên file duy nhất tránh trùng lặp ghim (VD: UUID_TênFileGoc.jpg)
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Copy file vào thư mục tĩnh
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Gán đường dẫn lưu trong DB (đường dẫn tương đối với thư mục static)
                book.setImageUrl("/images/books/" + fileName);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải ảnh lên!");
                return "redirect:/admin/books";
            }
        } else if (!isNew) {
            // Nếu update mà không tải ảnh mới & không nhập link mới thì giữ lại ảnh cũ
            Book existingBook = bookService.getBookById(book.getId()).orElse(null);
            if (existingBook != null && (book.getImageUrl() == null || book.getImageUrl().isEmpty())) {
                book.setImageUrl(existingBook.getImageUrl());
            }
        }
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Đã thêm sách mới!" : "Đã cập nhật sách!");
        return "redirect:/admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBookById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa! Sách đang có trong hóa đơn.");
        }
        return "redirect:/admin/books";
    }
}
