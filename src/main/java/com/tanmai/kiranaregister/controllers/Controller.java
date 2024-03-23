package com.tanmai.kiranaregister.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.tanmai.kiranaregister.model.TransactionModel;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class Controller {
    private final WebClient webClient;
    private final List<String> paymentMethods;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public Controller(WebClient webClient, MongoClient mongoClient) {
        this.webClient = webClient;
        this.mongoClient = mongoClient;
        this.database = this.mongoClient.getDatabase("kirana-register-db");
        this.paymentMethods = List.of("Cash", "Credit Card", "Debit Card", "Net Banking", "UPI");
    }

    @GetMapping("/test-database")
    public String testDatabase() {
        this.database.runCommand(new Document("ping", 1));
        return new String("Pinged your deployment. You successfully connected to MongoDB!");
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/currencies")
    public Mono<Map<String, Integer>> getCurrencies() {

        try {
            return webClient.get()
            .uri("https://api.fxratesapi.com/latest")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .map(responseBody -> (Map<String, Integer>) responseBody.get("rates"));
        }

        catch(Exception e) {
            System.out.println("An error has occured: " + e.getMessage());
            return Mono.empty();
        }
    }

    @PostMapping("/transact")
    public ResponseEntity<Map<String, Object>> recordTransaction(@RequestBody TransactionModel transaction) {

        try {
            Map<String, Integer> currencies = getCurrencies().block();

            float amount = TransactionModel.validateAmount(transaction.getAmount());
            String currency = TransactionModel.validateCurrency(currencies, transaction.getCurrency());
            String paymentMethod = TransactionModel.validatePaymentMethod(this.paymentMethods, transaction.getPaymentMethod());
            String customerId = TransactionModel.validateCustomerId(transaction.getCustomerId());

            System.out.println("Amount: " + amount);
            System.out.println("Currency: " + currency);
            System.out.println("Payment Method: " + paymentMethod);
            System.out.println("Customer ID: " + customerId);

            Document transactionDocument = new Document("amount", amount)
                .append("currency", currency)
                .append("paymentMethod", paymentMethod)
                .append("customerId", customerId);

            this.database.getCollection("transactions").insertOne(transactionDocument);

            return ResponseEntity.ok(new HashMap<>() {
                {
                    put("message", "Transaction recorded successfully.");
                    put("transaction", transaction);
                }
            });
        }

        catch(Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<>() {
                {
                    put("error", e.getMessage());
                    put("valid payment methods", paymentMethods);
                    put("transaction", transaction);
                }
            });
        }
    }
    
}
