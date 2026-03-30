package com.hoangduongbinh.repository;

import com.hoangduongbinh.entity.ItemInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Entity ItemInvoice.
 */
@Repository
public interface IItemInvoiceRepository extends JpaRepository<ItemInvoice, Long> {
}
