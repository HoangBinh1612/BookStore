package com.hoangduongbinh.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ItemInvoice - Đại diện bảng "item_invoice" trong database.
 * Lưu chi tiết từng sách trong 1 hóa đơn: sách nào, số lượng.
 * Mối quan hệ:
 * - ManyToOne với Invoice: Nhiều mặt hàng thuộc 1 hóa đơn.
 * - ManyToOne với Book: Mỗi mặt hàng liên kết đến 1 cuốn sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_invoice")
public class ItemInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private Book book;
}
