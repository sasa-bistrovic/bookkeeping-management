package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookkeeping_invoice_item")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;

    private String invoiceNumber;
    private String account;
    private String oib;
    private String city;
    private String date;
    private String dueDate;
    private String paymentDate;
    
    private double paidSoFar;
    
    private double remainToBePaid;

    private String partnerId;

    @Enumerated(EnumType.STRING)
    private InvoiceType type;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    private double totalBase;
    private double totalTax;
    private double totalAmount;
    
    private String description;

    private boolean isPosted;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    // Getteri i setteri

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }    
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }        
    
    public String getOib() { return oib; }
    public void setOib(String oib) { this.oib = oib; }        

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }    

    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public InvoiceType getType() { return type; }
    public void setType(InvoiceType type) { this.type = type; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }

    public double getTotalBase() { return totalBase; }
    public void setTotalBase(double totalBase) { this.totalBase = totalBase; }

    public double getTotalTax() { return totalTax; }
    public void setTotalTax(double totalTax) { this.totalTax = totalTax; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public double getPaidSoFar() { return paidSoFar; }
    public void setPaidSoFar(double paidSoFar) { this.paidSoFar = paidSoFar; }

    public double getRemainToBePaid() { return remainToBePaid; }
    public void setRemainToBePaid(double remainToBePaid) { this.remainToBePaid = remainToBePaid; }    

    public boolean isPosted() { return isPosted; }
    public void setPosted(boolean posted) { isPosted = posted; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
}
