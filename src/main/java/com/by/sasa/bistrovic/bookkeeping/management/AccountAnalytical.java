package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_accountanalytical_item")
public class AccountAnalytical {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String analyticalKey;
    private String analyticalName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getAnalyticalKey() { return analyticalKey; }
    public void setAnalyticalKey(String analyticalKey) { this.analyticalKey = analyticalKey; }
    
    public String getAnalyticalName() { return analyticalName; }
    public void setAnalyticalName(String analyticalName) { this.analyticalName = analyticalName; }    

}