package com.hoangduongbinh.controller;

import com.hoangduongbinh.dto.Item;
import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.service.CartService;
import com.hoangduongbinh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller xử lý Thanh toán (Checkout).
 */
@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping("/checkout")
    public String showCheckout(Authentication authentication, Model model,
            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        List<Item> cart = cartService.getCart(user);

        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartMessage", "Giỏ hàng trống! Hãy thêm sách.");
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cart);
        model.addAttribute("cartTotal", cartService.getCartTotal(user));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(Authentication authentication,
            @RequestParam(value = "paymentMethod", defaultValue = "QR_PAY") String paymentMethod,
            @RequestParam(value = "shippingAddress", required = false) String shippingAddress,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        List<Item> cart = cartService.getCart(user);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartMessage", "Giỏ hàng trống!");
            return "redirect:/cart";
        }

        Invoice createdInvoice = cartService.saveCart(user);

        if (createdInvoice != null) {
            // COD: đặt hàng thành công, không cần QR
            if ("COD".equals(paymentMethod)) {
                createdInvoice.setStatus("Đã đặt hàng - Thanh toán khi nhận");
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đặt hàng thành công! Đơn hàng #" + createdInvoice.getId() + " đã được xác nhận.");
                return "redirect:/orders";
            }
            // QR_PAY: chuyển sang trang QR thanh toán
            return "redirect:/payment/qr/" + createdInvoice.getId();
        }

        redirectAttributes.addFlashAttribute("cartMessage", "Có lỗi xảy ra khi tạo hóa đơn.");
        return "redirect:/cart";
    }
}
