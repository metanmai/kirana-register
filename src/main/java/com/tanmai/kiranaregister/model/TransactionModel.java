package com.tanmai.kiranaregister.model;

import java.util.HashMap;
import java.util.List;

/*
 * The TransactionModel class is a model class that represents a transaction.
 */
public class TransactionModel {
    
    // private String transactionId;
    private float amount;
    private String currency;
    private String paymentMethod;
    private String customerId;

    public static float validateAmount(float amount) {
        if(amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }

        return amount;
    }

    public static String validateCurrency(HashMap<String, Double> currencies, String currency) {
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
}
