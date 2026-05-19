package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_partner_item")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;

    private String name;
    private String description;
    private String city;
    private String address;
    private String oib;

    private String defaultInputAccount;
    private String defaultOutputAccount;

    private String iban;
    private String bic;

    // Getteri i setteri

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }    

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOib() { return oib; }
    public void setOib(String oib) { this.oib = oib; }

    public String getDefaultInputAccount() { return defaultInputAccount; }
    public void setDefaultInputAccount(String defaultInputAccount) { this.defaultInputAccount = defaultInputAccount; }

    public String getDefaultOutputAccount() { return defaultOutputAccount; }
    public void setDefaultOutputAccount(String defaultOutputAccount) { this.defaultOutputAccount = defaultOutputAccount; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getBic() { return bic; }
    public void setBic(String bic) { this.bic = bic; }
}
