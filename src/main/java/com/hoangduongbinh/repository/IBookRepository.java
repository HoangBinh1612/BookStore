package com.hoangduongbinh.repository;

import com.hoangduongbinh.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Entity Book.
 * Hỗ trợ CRUD, phân trang, sắp xếp và tìm kiếm sách.
 */
@Repository
public interface IBookRepository extends PagingAndSortingRepository<Book, Long>, JpaRepository<Book, Long> {

    /**
     * Lấy danh sách sách có phân trang và sắp xếp.
     */
    default List<Book> findAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy))).getContent();
    }

    /**
     * Tìm kiếm sách theo từ khóa (title hoặc author).
     */
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchBook(@Param("keyword") String keyword);

    /**
     * Tìm kiếm nâng cao: keyword + categoryId + khoảng giá (cho trang chủ).
     */
    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice)")
    Page<Book> searchBooks(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    // Tìm kiếm sách theo tên (chứa từ khóa, không phân biệt hoa thường)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Lọc sách theo danh mục
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);
}
