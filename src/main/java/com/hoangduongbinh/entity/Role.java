package com.hoangduongbinh.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity Role - Đại diện bảng "role" trong database.
 * Lưu vai trò người dùng: ADMIN, USER.
 * Mối quan hệ ManyToMany với User thông qua bảng trung gian "user_role".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}
