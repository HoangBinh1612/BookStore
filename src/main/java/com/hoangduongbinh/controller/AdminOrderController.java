package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.entity.ItemInvoice;
import com.hoangduongbinh.repository.IBookRepository;
import com.hoangduongbinh.repository.IInvoiceRepository;
import com.hoangduongbinh.repository.IItemInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private IItemInvoiceRepository itemInvoiceRepository;

    @Autowired
    private IBookRepository bookRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String listOrders(Model model) {
        List<Invoice> orders = invoiceRepository.findAllByOrderByInvoiceDateDesc();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String orderDetail(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<Invoice> opt = invoiceRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Khong tim thay don hang #" + id);
            return "redirect:/admin/orders";
        }
        model.addAttribute("invoice", opt.get());
        model.addAttribute("allBooks", bookRepository.findAll());
        return "admin/order-detail";
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String newStatus,
            @RequestParam(value = "redirect", defaultValue = "list") String redirectTo,
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
            RedirectAttributes ra) {
        Optional<Invoice> opt = invoiceRepository.findById(id);
        if (opt.isPresent()) {
            opt.get().setStatus(newStatus);
            invoiceRepository.save(opt.get());
            ra.addFlashAttribute("successMessage", "Cap nhat trang thai thanh cong!");
        } else {
            ra.addFlashAttribute("errorMessage", "Khong tim thay don hang!");
        }
        if ("detail".equals(redirectTo) && invoiceId != null) {
            return "redirect:/admin/orders/" + invoiceId;
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/add-item")
    @Transactional
    public String addItem(
            @PathVariable("id") Long id,
            @RequestParam("bookId") Long bookId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            RedirectAttributes ra) {
        Optional<Invoice> invOpt = invoiceRepository.findById(id);
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (invOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Khong tim thay don hang!");
            return "redirect:/admin/orders";
        }
        if (bookOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Khong tim thay sach!");
            return "redirect:/admin/orders/" + id;
        }
        Invoice invoice = invOpt.get();
        Book book = bookOpt.get();
        boolean found = false;
        for (ItemInvoice item : invoice.getItemInvoices()) {
            if (item.getBook() != null && item.getBook().getId().equals(bookId)) {
                item.setQuantity(item.getQuantity() + quantity);
                itemInvoiceRepository.save(item);
                found = true;
                break;
            }
        }
        if (!found) {
            ItemInvoice newItem = new ItemInvoice();
            newItem.setInvoice(invoice);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            itemInvoiceRepository.save(newItem);
        }
        recalcAndSave(invoiceRepository.findById(id).get());
        ra.addFlashAttribute("successMessage", "Da them '" + book.getTitle() + "' vao don hang!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/update-item")
    @Transactional
    public String updateItem(
            @PathVariable("id") Long id,
            @RequestParam("itemId") Long itemId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes ra) {
        Optional<ItemInvoice> itemOpt = itemInvoiceRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Khong tim thay san pham trong don!");
            return "redirect:/admin/orders/" + id;
        }
        if (quantity < 1) quantity = 1;
        itemOpt.get().setQuantity(quantity);
        itemInvoiceRepository.save(itemOpt.get());
        Optional<Invoice> invOpt = invoiceRepository.findById(id);
        invOpt.ifPresent(this::recalcAndSave);
        ra.addFlashAttribute("successMessage", "Da cap nhat so luong!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/delete-item")
    @Transactional
    public String deleteItem(
            @PathVariable("id") Long id,
            @RequestParam("itemId") Long itemId,
            RedirectAttributes ra) {
        Optional<Invoice> invOpt = invoiceRepository.findById(id);
        if (invOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Khong tim thay don hang!");
            return "redirect:/admin/orders";
        }
        Invoice invoice = invOpt.get();
        // Phai xoa khoi collection de Hibernate xu ly orphanRemoval=true dung cach
        boolean removed = invoice.getItemInvoices().removeIf(item -> item.getId().equals(itemId));
        if (removed) {
            recalcAndSave(invoice);
            ra.addFlashAttribute("successMessage", "Da xoa san pham khoi don hang!");
        } else {
            ra.addFlashAttribute("errorMessage", "Khong tim thay san pham!");
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/update-price")
    public String updatePrice(
            @PathVariable("id") Long id,
            @RequestParam("price") double price,
            RedirectAttributes ra) {
        Optional<Invoice> opt = invoiceRepository.findById(id);
        if (opt.isPresent()) {
            opt.get().setPrice(price);
            invoiceRepository.save(opt.get());
            ra.addFlashAttribute("successMessage", "Da cap nhat tong tien!");
        } else {
            ra.addFlashAttribute("errorMessage", "Khong tim thay don hang!");
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/recalculate-price")
    @Transactional
    public String recalculatePrice(@PathVariable("id") Long id, RedirectAttributes ra) {
        Optional<Invoice> opt = invoiceRepository.findById(id);
        if (opt.isPresent()) {
            recalcAndSave(opt.get());
            ra.addFlashAttribute("successMessage", "Da tinh lai tong tien tu san pham!");
        } else {
            ra.addFlashAttribute("errorMessage", "Khong tim thay don hang!");
        }
        return "redirect:/admin/orders/" + id;
    }

    private void recalcAndSave(Invoice invoice) {
        double total = 0;
        for (ItemInvoice item : invoice.getItemInvoices()) {
            if (item.getBook() != null) {
                total += item.getBook().getPrice() * item.getQuantity();
            }
        }
        invoice.setPrice(total);
        invoiceRepository.save(invoice);
    }
}