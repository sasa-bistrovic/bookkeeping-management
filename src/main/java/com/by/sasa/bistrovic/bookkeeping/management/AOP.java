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
    
    private Integer aopListYear;
    private String aopListName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    

    public String getAopCode() { return aopCode; }
    public void setAopCode(String aopCode) { this.aopCode = aopCode; }

    public String getAopName() { return aopName; }
    public void setAopName(String aopName) { this.aopName = aopName; }

    public Integer getAopListYear() { return aopListYear; }
    public void setAopListYear(Integer aopListYear) { this.aopListYear = aopListYear; }

    public String getAopListName() { return aopListName; }
    public void setAopListName(String aopListName) { this.aopListName = aopListName; }
}

