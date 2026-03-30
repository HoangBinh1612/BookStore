package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.IInvoiceRepository;
import com.hoangduongbinh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller xử lý lịch sử đơn hàng của người dùng.
 */
@Controller
public class OrderController {

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/orders")
    public String showOrderHistory(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user != null) {
            List<Invoice> orders = invoiceRepository.findByUserOrderByInvoiceDateDesc(user);
            model.addAttribute("orders", orders);
        }

        return "orders";
    }
}
