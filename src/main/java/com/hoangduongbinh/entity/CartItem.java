package com.hoangduongbinh.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity CartItem - Lưu giỏ hàng vào database.
 * Mỗi CartItem đại diện 1 sách trong giỏ hàng của 1 user.
 * Giỏ hàng sẽ được giữ lại khi user đăng xuất và đăng nhập lại.
 */
@Entity
@Table(name = "cart_item",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User sở hữu giỏ hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Sách trong giỏ hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * Số lượng sách.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}
