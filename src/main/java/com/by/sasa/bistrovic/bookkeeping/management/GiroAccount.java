package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "bookkeeping_giroaccount_item")
public class GiroAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;

    private String name;
    private String invoiceNumber;
    private String description;
    private String date;
    private String excerpt;
    
    private String accountType;
    
    private String inputAccount;
    
    private String outgoingAccount;

    private String partnerId;

    private double totalAmount;
    
    private double openingPayment;
    private double openingDeposit;

    private boolean isPosted;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }        

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    public String getInputAccount() { return inputAccount; }
    public void setInputAccount(String inputAccount) { this.inputAccount = inputAccount; }
    
    public String getOutgoingAccount() { return outgoingAccount; }
    public void setOutgoingAccount(String outgoingAccount) { this.outgoingAccount = outgoingAccount;}

    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public boolean isPosted() { return isPosted; }
    public void setPosted(boolean posted) { isPosted = posted; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    
    public double getOpeningPayment() { return openingPayment; }
    public void setOpeningPayment(double openingPayment) { this.openingPayment = openingPayment; }    
    
    public double getOpeningDeposit() { return openingDeposit; }
    public void setOpeningDeposit(double openingDeposit) { this.openingDeposit = openingDeposit; }    
}
