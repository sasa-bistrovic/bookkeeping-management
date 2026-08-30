package com.by.sasa.bistrovic.bookkeeping.management;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Invoice findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByInvoiceNumberAndUserIdAndType(String invoiceNumber, String userId, InvoiceType type);
    
    @Query("""
        select distinct i
        from Invoice i
        left join fetch i.items
    """)
    List<Invoice> findAllWithItems();
}
