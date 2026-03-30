package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.repository.IBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller quản lý Kho hàng (Inventory) cho Admin.
 */
@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    @Autowired
    private IBookRepository bookRepository;

    @GetMapping
    public String showInventory(Model model) {
        // Lấy tất cả danh sách sách để xem tồn kho
        List<Book> books = bookRepository.findAll();

        // Thống kê nhanh
        long lowStockCount = books.stream().filter(b -> b.getStock() != null && b.getStock() <= 10 && b.getStock() > 0)
                .count();
        long outOfStockCount = books.stream().filter(b -> b.getStock() == null || b.getStock() == 0).count();
        int totalStock = books.stream().mapToInt(b -> b.getStock() != null ? b.getStock() : 0).sum();

        model.addAttribute("books", books);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("totalStock", totalStock);

        return "admin/inventory";
    }
}
