package com.hoangduongbinh.dto;

import lombok.*;

/**
 * DTO đại diện cho 1 mặt hàng trong giỏ hàng (Item).
 * Lưu trong Session cho đến khi checkout.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private Long bookId;
    private String bookName;
    private Double price;
    private Integer quantity;
    private String imageUrl;

    /**
     * Tính tổng tiền = đơn giá × số lượng.
     */
    public Double getSubtotal() {
        return price * quantity;
    }
}
