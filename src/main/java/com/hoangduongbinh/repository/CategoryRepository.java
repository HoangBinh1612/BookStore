package com.hoangduongbinh.repository;

import com.hoangduongbinh.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Entity Category.
 * Kế thừa JpaRepository để có sẵn các phương thức CRUD.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // JpaRepository đã cung cấp đủ: findAll(), findById(), save(), deleteById()...
}
