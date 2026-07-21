package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_aop_item")
public class AOP {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;

    private String aopCode;
    private String aopName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    

    public String getAopCode() { return aopCode; }
    public void setAopCode(String aopCode) { this.aopCode = aopCode; }

    public String getAopName() { return aopName; }
    public void setAopName(String aopName) { this.aopName = aopName; }

}

