package com.hoangduongbinh.repository;

import com.hoangduongbinh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository cho Entity User.
 * JpaRepository cung cấp sẵn các phương thức CRUD (save, findAll, findById,
 * delete...).
 * Các method tự định nghĩa bên dưới sẽ được Spring Data JPA tự động implement.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user theo username (dùng cho đăng nhập)
    Optional<User> findByUsername(String username);

    // Tìm user theo email (kiểm tra trùng email khi đăng ký)
    Optional<User> findByEmail(String email);

    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
}
