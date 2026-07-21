package com.by.sasa.bistrovic.bookkeeping.management;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {
    List<LedgerEntry> findByPostedFalse();
    List<LedgerEntry> findByPostedTrue();
    List<LedgerEntry> findByStatus(InvoiceStatus status);

    List<LedgerEntry> findByReferenceId(String id);

    List<LedgerEntry> findByInvoiceNumberAndDate(String invoiceNumber, String date);

    List<LedgerEntry> findByStatusAndDateAndInvoiceNumber(
            InvoiceStatus status,
            String date,
            String invoiceNumber
    );

    List<LedgerEntry> findByInvoiceNumberAndDateAndLedgerType(String invoiceNumber, String date, String ledgerType);

    List<LedgerEntry> findByStatusAndDateAndInvoiceNumberAndLedgerType(InvoiceStatus invoiceStatus, String date, String invoiceNumber, String ledgerType);

    List<LedgerEntry> findByInvoiceNumberAndLedgerType(String invoiceNumber, String ledgerType);

    List<LedgerEntry> findByInvoiceNumberAndLedgerTypeAndUserId(String invoiceNumber, String ledgerType, String userId);

    List<LedgerEntry> findByInvoiceNumberAndDescriptionAndUserId(String invoiceNumber, String description, String userId);
}