package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_account_item")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;
    
    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private String aopCode;
    private String aopName;
    
    private double balanceDebit;
    private double balanceCredit;

    private String description;
    
    private String opis;
    private String oib;
    private String grad;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }

    public String getAopCode() { return aopCode; }
    public void setAopCode(String aopCode) { this.aopCode = aopCode; }

    public String getAopName() { return aopName; }
    public void setAopName(String aopName) { this.aopName = aopName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    
    public String getOib() { return oib; }
    public void setOib(String oib) { this.oib = oib; }
    
    public String getGrad() { return grad; }
    public void setGrad(String grad) { this.grad = grad; }
    
    public double getBalanceDebit() { return balanceDebit; }
    public void setBalanceDebit(double balanceDebit) { this.balanceDebit = balanceDebit; }    
    
    public double getBalanceCredit() { return balanceCredit; }
    public void setBalanceCredit(double balanceCredit) { this.balanceCredit = balanceCredit; }        
}
