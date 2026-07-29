package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_accountsynthetic_item")
public class AccountSynthetic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String syntheticKey;
    private String syntheticName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSyntheticKey() { return syntheticKey; }
    public void setSyntheticKey(String syntheticKey) { this.syntheticKey = syntheticKey; }
    
    public String getSyntheticName() { return syntheticName; }
    public void setSyntheticName(String syntheticName) { this.syntheticName = syntheticName; }    

}
