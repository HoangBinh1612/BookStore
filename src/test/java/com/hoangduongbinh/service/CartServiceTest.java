package com.hoangduongbinh.service;

import com.hoangduongbinh.dto.Item;
import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.CartItem;
import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.entity.ItemInvoice;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.IBookRepository;
import com.hoangduongbinh.repository.ICartItemRepository;
import com.hoangduongbinh.repository.IInvoiceRepository;
import com.hoangduongbinh.repository.IItemInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CartService (Database-based).
 * Sử dụng mock User và ICartItemRepository.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private ICartItemRepository cartItemRepository;

    @Mock
    private IInvoiceRepository invoiceRepository;

    @Mock
    private IItemInvoiceRepository itemInvoiceRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@test.com");

        testBook = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Author")
                .price(100000.0)
                .stock(10)
                .imageUrl("/images/test.jpg")
                .build();
    }

    // ==================== getCart ====================

    @Test
    @DisplayName("getCart - giỏ trống → trả về list rỗng")
    void getCart_EmptyCart_ShouldReturnEmptyList() {
        when(cartItemRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        List<Item> cart = cartService.getCart(testUser);

        assertNotNull(cart);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("getCart - có sản phẩm → trả về danh sách Item")
    void getCart_WithItems_ShouldReturnItemList() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(2)
                .build();
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(cartItem));

        List<Item> cart = cartService.getCart(testUser);

        assertEquals(1, cart.size());
        assertEquals("Test Book", cart.get(0).getBookName());
        assertEquals(2, cart.get(0).getQuantity());
        assertEquals(100000.0, cart.get(0).getPrice());
    }

    // ==================== addToCart ====================

    @Test
    @DisplayName("addToCart - thêm sách mới vào giỏ")
    void addToCart_NewBook_ShouldCreateCartItem() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByUserAndBook(testUser, testBook)).thenReturn(Optional.empty());

        cartService.addToCart(testUser, 1L);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart - sách đã có → tăng quantity")
    void addToCart_ExistingBook_ShouldIncreaseQuantity() {
        CartItem existing = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(1)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByUserAndBook(testUser, testBook)).thenReturn(Optional.of(existing));

        cartService.addToCart(testUser, 1L);

        assertEquals(2, existing.getQuantity());
        verify(cartItemRepository).save(existing);
    }

    @Test
    @DisplayName("addToCart - bookId không tồn tại → không thêm")
    void addToCart_BookNotFound_ShouldNotAdd() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        cartService.addToCart(testUser, 999L);

        verify(cartItemRepository, never()).save(any());
    }

    // ==================== updateQuantity ====================

    @Test
    @DisplayName("updateQuantity - cập nhật số lượng thành công")
    void updateQuantity_ShouldUpdateSuccessfully() {
        CartItem existing = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(1)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByUserAndBook(testUser, testBook)).thenReturn(Optional.of(existing));

        cartService.updateQuantity(testUser, 1L, 5);

        assertEquals(5, existing.getQuantity());
        verify(cartItemRepository).save(existing);
    }

    @Test
    @DisplayName("updateQuantity - quantity ≤ 0 → xóa khỏi giỏ")
    void updateQuantity_ZeroOrNegative_ShouldRemoveItem() {
        CartItem existing = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(1)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByUserAndBook(testUser, testBook)).thenReturn(Optional.of(existing));

        cartService.updateQuantity(testUser, 1L, 0);

        verify(cartItemRepository).delete(existing);
    }

    // ==================== removeFromCart ====================

    @Test
    @DisplayName("removeFromCart - xóa item theo bookId")
    void removeFromCart_ShouldRemoveItem() {
        CartItem existing = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(1)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByUserAndBook(testUser, testBook)).thenReturn(Optional.of(existing));

        cartService.removeFromCart(testUser, 1L);

        verify(cartItemRepository).delete(existing);
    }

    // ==================== getCartTotal ====================

    @Test
    @DisplayName("getCartTotal - tính tổng tiền đúng")
    void getCartTotal_ShouldCalculateCorrectly() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(2)
                .build();
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(cartItem));

        Double total = cartService.getCartTotal(testUser);

        assertEquals(200000.0, total); // 100000 * 2
    }

    // ==================== getCartItemCount ====================

    @Test
    @DisplayName("getCartItemCount - đếm tổng số mặt hàng")
    void getCartItemCount_ShouldCountAllItems() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(2)
                .build();
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(cartItem));

        int count = cartService.getCartItemCount(testUser);

        assertEquals(2, count);
    }

    // ==================== clearCart ====================

    @Test
    @DisplayName("clearCart - xóa toàn bộ giỏ hàng")
    void clearCart_ShouldDeleteAllCartItems() {
        cartService.clearCart(testUser);

        verify(cartItemRepository).deleteByUser(testUser);
    }

    // ==================== saveCart ====================

    @Test
    @DisplayName("saveCart - giỏ rỗng → return null")
    void saveCart_EmptyCart_ShouldReturnNull() {
        when(cartItemRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        Invoice result = cartService.saveCart(testUser);

        assertNull(result);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveCart - tạo Invoice + ItemInvoice → clear cart")
    void saveCart_WithItems_ShouldCreateInvoiceAndClearCart() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .book(testBook)
                .quantity(1)
                .build();
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(cartItem));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = cartService.saveCart(testUser);

        assertNotNull(result);
        assertEquals(100000.0, result.getPrice());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(itemInvoiceRepository).save(any(ItemInvoice.class));
        verify(cartItemRepository).deleteByUser(testUser);
    }
}
