package com.hoangduongbinh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity User - Đại diện bảng "users" trong database.
 * Lưu thông tin người dùng: tài khoản, mật khẩu, email, phone, provider.
 * Mối quan hệ ManyToMany với Role thông qua bảng trung gian "user_role".
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(nullable = false, unique = true)
    private String email;

    // Số điện thoại
    @Column(length = 15)
    private String phone;

    // Provider đăng nhập: "local" hoặc "google" (cho OAuth2)
    @Column(length = 20)
    private String provider;

    /**
     * ManyToMany với Role.
     * Bảng trung gian: user_role (user_id, role_id)
     * FetchType.EAGER: Load roles cùng lúc với User (cần cho security check).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
