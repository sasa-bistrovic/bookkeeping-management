package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_invoiceitem_item")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String description;
    private String baseAccount;
    private String taxAccount;
    private double quantity;
    private double price;

    private String taxRateId;

    private double baseAmount;
    private double taxAmount;
    private double totalAmount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getBaseAccount() { return baseAccount; }
    public void setBaseAccount(String baseAccount) { this.baseAccount = baseAccount; }

    public String getTaxAccount() { return taxAccount; }
    public void setTaxAccount(String taxAccount) { this.taxAccount = taxAccount; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getTaxRateId() { return taxRateId; }
    public void setTaxRateId(String taxRateId) { this.taxRateId = taxRateId; }

    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
