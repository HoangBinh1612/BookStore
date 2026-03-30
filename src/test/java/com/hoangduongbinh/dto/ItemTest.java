package com.hoangduongbinh.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho Item DTO.
 */
class ItemTest {

    @Test
    @DisplayName("getSubtotal - price × quantity = subtotal đúng")
    void getSubtotal_ShouldCalculateCorrectly() {
        Item item = new Item(1L, "Java Book", 150000.0, 3, "/img.jpg");

        Double subtotal = item.getSubtotal();

        assertEquals(450000.0, subtotal);
    }

    @Test
    @DisplayName("getSubtotal - quantity = 0 → 0.0")
    void getSubtotal_WithZeroQuantity_ShouldReturnZero() {
        Item item = new Item(1L, "Java Book", 150000.0, 0, "/img.jpg");

        Double subtotal = item.getSubtotal();

        assertEquals(0.0, subtotal);
    }

    @Test
    @DisplayName("getSubtotal - quantity = 1 → giá đơn")
    void getSubtotal_WithSingleQuantity_ShouldReturnPrice() {
        Item item = new Item(1L, "Book", 99000.0, 1, null);

        assertEquals(99000.0, item.getSubtotal());
    }
}
