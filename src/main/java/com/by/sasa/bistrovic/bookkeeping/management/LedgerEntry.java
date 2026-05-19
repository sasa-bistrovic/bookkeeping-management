package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_ledgerentry_item")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;

    private String date;
    private String accountCode;

    private double debit;
    private double credit;

    private String description;

    private String partnerId;
    private String referenceId;
    
    private String invoiceNumber;
    
    private String ledgerType;
    
    private int year;
    
    private boolean posted;
    
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public double getDebit() { return debit; }
    public void setDebit(double debit) { this.debit = debit; }

    public double getCredit() { return credit; }
    public void setCredit(double credit) { this.credit = credit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }    
    
    public boolean getPosted() { return posted; }
    public void setPosted(boolean posted) { this.posted = posted; }    
    
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }    
    
    public String getLedgerType() { return ledgerType; }
    public void setLedgerType(String ledgerType) { this.ledgerType = ledgerType; }        
}
