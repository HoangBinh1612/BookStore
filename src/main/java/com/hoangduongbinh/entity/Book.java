package com.hoangduongbinh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

/**
 * Entity Book - Đại diện bảng "book" trong database.
 * Lưu thông tin sách: tiêu đề, tác giả, giá, danh mục.
 * Mối quan hệ: Nhiều Book thuộc 1 Category (ManyToOne).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 50, nullable = false)
    @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
    @NotBlank(message = "Title must not be blank")
    private String title;

    @Column(name = "author", length = 50, nullable = false)
    @Size(min = 1, max = 50, message = "Author must be between 1 and 50 characters")
    @NotBlank(message = "Author must not be blank")
    private String author;

    @Column(name = "price")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @Column(name = "stock")
    @Min(value = 0, message = "Stock must not be negative")
    private Integer stock = 0;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * Mối quan hệ ManyToOne: Nhiều sách thuộc 1 danh mục.
     * FetchType.LAZY: Chỉ load Category khi cần (tối ưu hiệu suất).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ToString.Exclude
    private Category category;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Book book = (Book) o;
        return getId() != null && Objects.equals(getId(), book.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
