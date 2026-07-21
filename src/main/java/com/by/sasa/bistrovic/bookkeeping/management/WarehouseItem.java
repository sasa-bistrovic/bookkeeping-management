package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_warehouse_item")
public class WarehouseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;

    private String accountCode;
    private String invoiceNumber;
    private String invoiceType;
    private String code;
    private String name;
    private String date;
    
    private String unit;    
    
    private double quantity;
    
    private double amount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }    
    
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }        
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }            
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }                

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}