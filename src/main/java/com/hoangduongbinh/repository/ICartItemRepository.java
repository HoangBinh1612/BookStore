package com.hoangduongbinh.repository;

import com.hoangduongbinh.entity.CartItem;
import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho CartItem - Thao tác giỏ hàng trong database.
 */
@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Lấy tất cả CartItem của 1 user (giỏ hàng).
     */
    List<CartItem> findByUser(User user);

    /**
     * Tìm CartItem theo user + book (kiểm tra sách đã có trong giỏ chưa).
     */
    Optional<CartItem> findByUserAndBook(User user, Book book);

    /**
     * Xóa toàn bộ giỏ hàng của 1 user (khi checkout).
     */
    void deleteByUser(User user);
}
