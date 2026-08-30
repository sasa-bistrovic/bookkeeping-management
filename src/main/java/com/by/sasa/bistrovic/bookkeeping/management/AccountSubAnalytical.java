package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_accountsubanalytical_item")
public class AccountSubAnalytical {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String subAnalyticalKey;
    private String subAnalyticalName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSubAnalyticalKey() { return subAnalyticalKey; }
    public void setSubAnalyticalKey(String subAnalyticalKey) { this.subAnalyticalKey = subAnalyticalKey; }
    
    public String getSubAnalyticalName() { return subAnalyticalName; }
    public void setSubAnalyticalName(String subAnalyticalName) { this.subAnalyticalName = subAnalyticalName; }    

}
