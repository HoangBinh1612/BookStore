package com.hoangduongbinh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity Invoice - Đại diện bảng "invoice" trong database.
 * Lưu thông tin hóa đơn mua sách.
 * Mối quan hệ:
 * - OneToMany với ItemInvoice: 1 hóa đơn có nhiều mặt hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_date")
    private Date invoiceDate;

    @Column(name = "price")
    private double price;

    /**
     * Danh sách mặt hàng trong hóa đơn.
     * CascadeType.ALL: Khi lưu Invoice, tự động lưu các ItemInvoice.
     * orphanRemoval: Khi xóa ItemInvoice khỏi list, tự động xóa trong DB.
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemInvoice> itemInvoices = new ArrayList<>();

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "Chưa thanh toán";

    /**
     * Người dùng sở hữu hóa đơn.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
