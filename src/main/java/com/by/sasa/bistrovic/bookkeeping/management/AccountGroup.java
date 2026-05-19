package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_accountgroup_item")
public class AccountGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String groupKey;
    private String groupName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }    

}
