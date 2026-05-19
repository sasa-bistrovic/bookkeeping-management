package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_accountclass_item")
public class AccountClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String classKey;
    private String className;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getClassKey() { return classKey; }
    public void setClassKey(String classKey) { this.classKey = classKey; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }    

}
