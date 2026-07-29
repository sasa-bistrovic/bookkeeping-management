package com.by.sasa.bistrovic.bookkeeping.management;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Invoice findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByInvoiceNumberAndUserIdAndType(String invoiceNumber, String userId, InvoiceType type);
}
