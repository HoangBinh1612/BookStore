package com.hoangduongbinh.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO (Data Transfer Object) cho form Đăng ký.
 * Dùng để nhận dữ liệu từ form đăng ký, tách biệt với Entity User.
 * Có thêm trường confirmPassword để xác nhận mật khẩu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
