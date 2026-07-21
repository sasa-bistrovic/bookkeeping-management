package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.persistence.*;

@Entity
@Table(name = "bookkeeping_taxrate_item")
public class TaxRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;
    
    private String name;
    private double percentage;

    private String inputTaxAccount;
    private String outputTaxAccount;

    // Getteri i setteri

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getInputTaxAccount() { return inputTaxAccount; }
    public void setInputTaxAccount(String inputTaxAccount) { this.inputTaxAccount = inputTaxAccount; }

    public String getOutputTaxAccount() { return outputTaxAccount; }
    public void setOutputTaxAccount(String outputTaxAccount) { this.outputTaxAccount = outputTaxAccount; }
}
