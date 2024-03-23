package com.tanmai.kiranaregister.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;
import java.util.List;

@Document(collection = "transactions")
public class TransactionModel {
    
    @Id
    private String transactionId;

    @Field("amount")
    private float amount;

    @Field("currency")
    private String currency;

    @Field("paymentMethod")
    private String paymentMethod;

    @Field("customerId")
    private String customerId;

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

        String acceptedMethods = "";

        for(String method : paymentMethods) {
            acceptedMethods += method + ", ";
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
