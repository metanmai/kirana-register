package com.tanmai.kiranaregister.model;

import java.util.Map;
import java.util.List;

/*
 * The TransactionModel2 class is a model class that represents a transaction.
 * This is an updated version of the TransactionModel class, with a new date field.
 */
public class TransactionModel2 {
    
    // private String transactionId;
    private float amount;
    private String currency;
    private String paymentMethod;
    private String customerId;
    private long date;

    public TransactionModel2(float amount, String currency, String paymentMethod, String customerId, long date) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.customerId = customerId;
        this.date = date;
    }

    public static float validateAmount(float amount) {
        if(amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }

        return amount;
    }

    public static String validateCurrency(Map<String, Integer> currencies, String currency) {
        if(currency == null || currency.isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be empty.");
        }
    
        if(!currencies.containsKey(currency)) {
            throw new IllegalArgumentException("Invalid Currency.");
        }
    
        return currency;
    }

    public static String validatePaymentMethod(List<String> paymentMethods, String paymentMethod) {
        if(paymentMethod == null || paymentMethod.isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty.");
        }

        for(String method : paymentMethods) {
            if(method.equalsIgnoreCase(paymentMethod)) {
                return paymentMethod.toLowerCase();
            }
        }

        throw new IllegalArgumentException("Invalid Payment Method");
    }

    public static String validateCustomerId(String customerId) {
        if(customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty.");
        }

        return customerId;
    }

    public float getAmount() {
        return this.amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public long getDate() {
        return this.date;
    }
}
