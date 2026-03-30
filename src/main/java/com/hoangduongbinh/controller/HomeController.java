package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.service.BookService;
import com.hoangduongbinh.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller trang chủ và chi tiết sách.
 *
 * Tính năng:
 * - Hiển thị danh sách sách (có phân trang)
 * - Tìm kiếm sách theo tiêu đề / tác giả
 * - Lọc sách theo danh mục
 * - Xem chi tiết 1 cuốn sách
 */
@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Trang chủ - Hiển thị danh sách sách.
     */
    @GetMapping({ "/", "/home" })
    public String home(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // Lấy danh sách sách theo điều kiện tìm kiếm/lọc/phân trang/sắp xếp
        Page<Book> bookPage = bookService.getBooks(keyword, categoryId, minPrice, maxPrice, sort, page);

        // Lấy danh sách danh mục (cho dropdown lọc)
        List<Category> categories = categoryService.getAllCategories();

        // Đẩy dữ liệu vào Model để Thymeleaf render
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "home";
    }

    /**
     * Chi tiết sách - Xem thông tin đầy đủ của 1 cuốn sách.
     */
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id).orElse(null);

        if (book == null) {
            return "redirect:/";
        }

        model.addAttribute("book", book);
        return "book-detail";
    }
}
