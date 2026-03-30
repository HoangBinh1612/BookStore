package com.hoangduongbinh.controller;

import com.hoangduongbinh.dto.RegisterDTO;
import com.hoangduongbinh.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý xác thực: Đăng ký và Đăng nhập.
 *
 * Luồng Đăng ký:
 * 1. GET /register → Hiển thị form đăng ký
 * 2. POST /register → Xử lý dữ liệu form, validate, lưu vào DB
 * 3. Redirect → /login với thông báo thành công
 *
 * Luồng Đăng nhập:
 * 1. GET /login → Hiển thị form đăng nhập
 * 2. POST /login → Spring Security tự xử lý (không cần code ở đây)
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // ==================== ĐĂNG NHẬP ====================

    /**
     * Hiển thị trang đăng nhập.
     * Spring Security sẽ tự xử lý POST /login
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Trả về templates/login.html
    }

    // ==================== ĐĂNG KÝ ====================

    /**
     * Hiển thị form đăng ký.
     * Tạo RegisterDTO rỗng để bind vào form Thymeleaf.
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register"; // Trả về templates/register.html
    }

    /**
     * Xử lý đăng ký tài khoản mới.
     * Các bước:
     * 1. Validate dữ liệu form (annotation @Valid)
     * 2. Kiểm tra mật khẩu xác nhận có khớp không
     * 3. Kiểm tra username/email đã tồn tại chưa
     * 4. Lưu user mới vào database
     * 5. Redirect về trang login với thông báo thành công
     */
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Bước 1: Kiểm tra validation (annotation trên DTO)
        if (result.hasErrors()) {
            return "register"; // Quay lại form, hiển thị lỗi
        }

        // Bước 2: Kiểm tra mật khẩu xác nhận
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword",
                    "Mật khẩu xác nhận không khớp");
            return "register";
        }

        // Bước 3: Kiểm tra username đã tồn tại
        if (userService.isUsernameExists(dto.getUsername())) {
            result.rejectValue("username", "error.username",
                    "Tên đăng nhập đã được sử dụng");
            return "register";
        }

        // Bước 4: Kiểm tra email đã tồn tại
        if (userService.isEmailExists(dto.getEmail())) {
            result.rejectValue("email", "error.email",
                    "Email đã được sử dụng");
            return "register";
        }

        // Bước 5: Đăng ký thành công → Lưu vào database
        userService.registerNewUser(dto);

        // Gửi thông báo thành công qua redirect
        redirectAttributes.addFlashAttribute("successMessage",
                "Đăng ký thành công! Hãy đăng nhập.");

        return "redirect:/login"; // Chuyển về trang đăng nhập
    }
}
