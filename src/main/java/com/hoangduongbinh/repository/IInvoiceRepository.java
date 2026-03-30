package com.hoangduongbinh.repository;

import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Entity Invoice.
 */
@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {
    java.util.List<Invoice> findByUserOrderByInvoiceDateDesc(User user);

    java.util.List<Invoice> findAllByOrderByInvoiceDateDesc();
}
