package com.by.sasa.bistrovic.bookkeeping.management;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiroAccountRepository extends JpaRepository<GiroAccount, String> {

    GiroAccount findByInvoiceNumber(String invoiceNumber);

    List<GiroAccount> findByUserIdAndName(String userId, String name);

    public List<GiroAccount> findByInvoiceNumberAndDescriptionAndUserId(String invoiceNumber, String description, String userId);
}
