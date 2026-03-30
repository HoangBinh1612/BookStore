package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.repository.IBookRepository;
import com.hoangduongbinh.repository.IInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller Thống kê (Statistics) cho Admin.
 */
@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

        @Autowired
        private IInvoiceRepository invoiceRepository;

        @Autowired
        private IBookRepository bookRepository;

        @GetMapping
        public String showStatistics(Model model) {
                // Lấy toàn bộ sách và hóa đơn
                long totalBooks = bookRepository.count();
                List<Invoice> orders = invoiceRepository.findAll();

                // Thống kê đơn hàng (Đã hoàn thành hoặc Đã thanh toán)
                long totalCompletedOrders = orders.stream()
                                .filter(o -> "Hoàn thành".equals(o.getStatus())
                                                || "Đã thanh toán".equals(o.getStatus()))
                                .count();

                // Tổng doanh thu (Chỉ tính các đơn hàng đã thanh toán hoặc hoàn thành)
                double totalRevenue = orders.stream()
                                .filter(o -> "Hoàn thành".equals(o.getStatus())
                                                || "Đã thanh toán".equals(o.getStatus()))
                                .mapToDouble(o -> o.getPrice())
                                .sum();

                // Tổng số đầu mục sách đã bán ra
                long totalItemsSold = orders.stream()
                                .filter(o -> "Hoàn thành".equals(o.getStatus())
                                                || "Đã thanh toán".equals(o.getStatus()))
                                .flatMap(o -> o.getItemInvoices().stream())
                                .mapToInt(item -> item.getQuantity())
                                .sum();

                model.addAttribute("totalBooks", totalBooks);
                model.addAttribute("totalCompletedOrders", totalCompletedOrders);
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("totalItemsSold", totalItemsSold);

                return "admin/statistics";
        }
}
