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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service quản lý Giỏ hàng (Database-based).
 *
 * Giỏ hàng được lưu trong bảng cart_item (liên kết User ↔ Book).
 * Khi user đăng xuất rồi đăng nhập lại, giỏ hàng vẫn được giữ nguyên.
 *
 * Khi checkout: tạo Invoice + ItemInvoice từ cart_item → lưu DB → xóa cart.
 */
@Service
public class CartService {

    @Autowired
    private IBookRepository bookRepository;

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private IItemInvoiceRepository itemInvoiceRepository;

    /**
     * Lấy giỏ hàng của user từ database.
     * Chuyển đổi CartItem entity → Item DTO để tương thích với view.
     */
    public List<Item> getCart(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        return cartItems.stream()
                .map(ci -> {
                    Item item = new Item();
                    item.setBookId(ci.getBook().getId());
                    item.setBookName(ci.getBook().getTitle());
                    item.setPrice(ci.getBook().getPrice());
                    item.setQuantity(ci.getQuantity());
                    item.setImageUrl(ci.getBook().getImageUrl());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * Thêm sách vào giỏ hàng.
     * Nếu sách đã có trong giỏ → tăng số lượng.
     * Nếu chưa có → tạo CartItem mới.
     */
    @Transactional
    public void addToCart(User user, Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return;

        Optional<CartItem> existing = cartItemRepository.findByUserAndBook(user, book);

        if (existing.isPresent()) {
            // Sách đã có trong giỏ → tăng số lượng
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemRepository.save(cartItem);
        } else {
            // Sách chưa có → tạo mới
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .book(book)
                    .quantity(1)
                    .build();
            cartItemRepository.save(cartItem);
        }
    }

    /**
     * Cập nhật số lượng sách trong giỏ.
     * Nếu quantity ≤ 0 → xóa khỏi giỏ.
     */
    @Transactional
    public void updateQuantity(User user, Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return;

        Optional<CartItem> existing = cartItemRepository.findByUserAndBook(user, book);
        if (existing.isPresent()) {
            if (quantity <= 0) {
                cartItemRepository.delete(existing.get());
            } else {
                existing.get().setQuantity(quantity);
                cartItemRepository.save(existing.get());
            }
        }
    }

    /**
     * Xóa 1 sách khỏi giỏ hàng.
     */
    @Transactional
    public void removeFromCart(User user, Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return;

        cartItemRepository.findByUserAndBook(user, book)
                .ifPresent(cartItemRepository::delete);
    }

    /**
     * Tính tổng tiền toàn bộ giỏ hàng.
     */
    public Double getCartTotal(User user) {
        return cartItemRepository.findByUser(user).stream()
                .mapToDouble(ci -> ci.getBook().getPrice() * ci.getQuantity())
                .sum();
    }

    /**
     * Đếm tổng số mặt hàng trong giỏ.
     */
    public int getCartItemCount(User user) {
        return cartItemRepository.findByUser(user).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Xóa toàn bộ giỏ hàng.
     */
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    /**
     * Lưu giỏ hàng thành Invoice + ItemInvoice trong DB (checkout).
     *
     * @return Invoice hóa đơn vừa tạo
     */
    @Transactional
    public Invoice saveCart(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) return null;

        // Tạo hóa đơn
        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(new Date());
        invoice.setUser(user);

        // Tính tổng tiền
        double total = cartItems.stream()
                .mapToDouble(ci -> ci.getBook().getPrice() * ci.getQuantity())
                .sum();
        invoice.setPrice(total);
        invoiceRepository.save(invoice);

        // Tạo chi tiết hóa đơn từ giỏ hàng
        for (CartItem ci : cartItems) {
            ItemInvoice itemInvoice = new ItemInvoice();
            itemInvoice.setQuantity(ci.getQuantity());
            itemInvoice.setInvoice(invoice);
            itemInvoice.setBook(ci.getBook());
            itemInvoiceRepository.save(itemInvoice);
        }

        // Xóa giỏ hàng sau khi checkout
        cartItemRepository.deleteByUser(user);
        return invoice;
    }
}
