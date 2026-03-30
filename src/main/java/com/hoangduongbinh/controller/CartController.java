package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.service.CartService;
import com.hoangduongbinh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Giỏ hàng.
 *
 * Các chức năng:
 * - Xem giỏ hàng: GET /cart
 * - Thêm sách: GET /cart/add/{bookId}
 * - Cập nhật số lượng: POST /cart/update
 * - Xóa sách: GET /cart/remove/{bookId}
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    /**
     * Hiển thị trang giỏ hàng.
     */
    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("cartItems", cartService.getCart(user));
        model.addAttribute("cartTotal", cartService.getCartTotal(user));
        model.addAttribute("cartItemCount", cartService.getCartItemCount(user));
        return "cart";
    }

    @GetMapping("/add/{bookId}")
    public Object addToCart(@PathVariable("bookId") Long bookId,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

        try {
            User user = userService.findByUsername(authentication.getName());
            cartService.addToCart(user, bookId);
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(500).body("Error adding to cart: " + e.toString());
        }

        // Nếu là request AJAX, trả về HTTP 200 OK
        if ("XMLHttpRequest".equals(requestedWith)) {
            return org.springframework.http.ResponseEntity.ok(
                    java.util.Map.of("status", "success", "message", "Đã thêm vào giỏ hàng!"));
        }

        redirectAttributes.addFlashAttribute("cartMessage", "Đã thêm vào giỏ hàng!");

        if (referer != null && !referer.isEmpty()) {
            return new org.springframework.web.servlet.view.RedirectView(referer);
        }
        return new org.springframework.web.servlet.view.RedirectView("/");
    }

    /**
     * Cập nhật số lượng sách trong giỏ.
     */
    @PostMapping("/update")
    public String updateCart(@RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        cartService.updateQuantity(user, bookId, quantity);
        return "redirect:/cart";
    }

    /**
     * Xóa sách khỏi giỏ hàng.
     */
    @GetMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable("bookId") Long bookId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        cartService.removeFromCart(user, bookId);
        redirectAttributes.addFlashAttribute("cartMessage", "Đã xóa khỏi giỏ hàng.");
        return "redirect:/cart";
    }
}
