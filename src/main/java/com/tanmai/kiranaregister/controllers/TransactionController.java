package com.tanmai.kiranaregister.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.tanmai.kiranaregister.model.TransactionModel;
import com.tanmai.kiranaregister.model.TransactionModel2;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class TransactionController {
    private final WebClient webClient;
    private final List<String> paymentMethods;
    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public TransactionController(WebClient webClient, MongoClient mongoClient) {
        this.webClient = webClient;
        this.mongoClient = mongoClient;
        this.collection = this.mongoClient.getDatabase("kirana-register-db").getCollection("transactions");
        this.paymentMethods = List.of("Cash", "Credit Card", "Debit Card", "Net Banking", "UPI");
    }

    @GetMapping("/test-database")
    public String testDatabase() {
        this.mongoClient.getDatabase("kirana-register-db").runCommand(new Document("ping", 1));
        return new String("Pinged your deployment. You successfully connected to MongoDB!");
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/currencies")
    public Mono<HashMap<String, Integer>> getCurrencies() {
        try {
            return webClient.get()
            .uri("https://api.fxratesapi.com/latest")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<HashMap<String, Object>>() {})
            .map(responseBody -> (HashMap<String, Integer>) responseBody.get("rates"));
        }

        catch(Exception e) {
            System.out.println("An error has occured: " + e.getMessage());
            return Mono.empty();
        }
    }

    @GetMapping("/get-uuid")
    public Mono<String> getUuid() {

        try {
            return webClient.get()
            .uri("https://www.uuidtools.com/api/generate/v4")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
            .map(list -> list.get(0));
        }

        catch(Exception e) {
            System.out.println("An error has occured: " + e.getMessage());
            return Mono.empty();
        }
    }

    @PostMapping("/transact")
    public ResponseEntity<Map<String, Object>> recordTransaction(@RequestBody TransactionModel transaction) {

        try {
            HashMap<String, Integer> currencies = getCurrencies().block();

            // String transactionId = getUuid().block();
            float amount = TransactionModel.validateAmount(transaction.getAmount());
            String currency = TransactionModel.validateCurrency(currencies, transaction.getCurrency());
            String paymentMethod = TransactionModel.validatePaymentMethod(this.paymentMethods, transaction.getPaymentMethod());
            String customerId = TransactionModel.validateCustomerId(transaction.getCustomerId());
            Date date = new Date();

            // System.out.println("Transaction ID: " + transactionId);
            System.out.println("Amount: " + amount);
            System.out.println("Currency: " + currency);
            System.out.println("Payment Method: " + paymentMethod);
            System.out.println("Customer ID: " + customerId);

            Document transactionDocument = new Document()
                .append("amount", amount)
                .append("currency", currency)
                .append("paymentMethod", paymentMethod)
                .append("customerId", customerId)
                .append("date", date);

            this.collection.insertOne(transactionDocument);

            return ResponseEntity.ok(new HashMap<>() {
                {
                    put("message", "Transaction recorded successfully.");
                    put("transaction", transactionDocument);
                }
            });
        }

        catch(Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<>() {
                {
                    put("error", e.getMessage());
                    put("transaction", transaction);
                }
            });
        }
    }

    @PostMapping("/transact2")
    public ResponseEntity<HashMap<String, Object>> recordTransaction2(@RequestBody List<TransactionModel2> transactions) {

        try {
            List<Document> transactionDocuments = new ArrayList<>();
            int k = 1;
            for(TransactionModel2 transaction : transactions) {
                HashMap<String, Integer> currencies = getCurrencies().block();

                // String transactionId = getUuid().block();
                float amount = TransactionModel.validateAmount(transaction.getAmount());
                String currency = TransactionModel.validateCurrency(currencies, transaction.getCurrency());
                String paymentMethod = TransactionModel.validatePaymentMethod(this.paymentMethods, transaction.getPaymentMethod());
                String customerId = TransactionModel.validateCustomerId(transaction.getCustomerId());
                long dateNum = transaction.getDate();
                Date date = new Date(dateNum);

                // System.out.println("Transaction ID: " + transactionId);
                System.out.println("Transaction Number: " + k++);
                System.out.println("Amount: " + amount);
                System.out.println("Currency: " + currency);
                System.out.println("Payment Method: " + paymentMethod);
                System.out.println("Customer ID: " + customerId);
                System.out.println("Date: " + date);
                System.out.println("-------------------------------------------------------------");

                Document transactionDocument = new Document()
                    .append("amount", amount)
                    .append("currency", currency)
                    .append("paymentMethod", paymentMethod)
                    .append("customerId", customerId)
                    .append("date", date);

                transactionDocuments.add(transactionDocument);
            }
            System.out.println(transactionDocuments);
            this.collection.insertMany(transactionDocuments);
            System.out.println("Transactions inserted successfully.");

            return ResponseEntity.ok(new HashMap<>() {
                {
                    put("message", "Transactions recorded successfully.");
                }
            });
        }

        catch(Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<>() {
                {
                    put("error", e.getMessage());
                    put("transaction", transactions);
                }
            });
        }
    }
    
    // Function to populate the database with random transactions.
    @GetMapping("/fill-db")
    public ResponseEntity<HashMap<String, Object>> fillDatabase(@RequestParam int items) {
        System.out.println("Filling database with random transactions...");
        List<TransactionModel2> list = new ArrayList<>();
        for(int i = 0; i < items; i++) {
            float min = 1.0f; 
            float max = 2000.0f;
            Random random = new Random();

            float randomNumber = min + (max - min) * random.nextFloat();
            float amount = randomNumber;
            List<String> currencies = List.of("USD", "INR", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD");
            String currency = currencies.get(random.nextInt(currencies.size()));

            List<String> paymentMethods = List.of("Cash", "Credit Card", "Debit Card", "Net Banking", "UPI");
            String paymentMethod = paymentMethods.get(random.nextInt(paymentMethods.size()));

            int mini = 100000;
            int maxi = 1000000;
            int customerId = random.nextInt(maxi - mini) + mini;

            long minl = 1340995200000L;
            long maxl = 1735603200000L;
            long dateNum = (long) (minl + Math.random() * (maxl - minl));
            TransactionModel2 transaction = new TransactionModel2(amount, currency, paymentMethod, String.valueOf(customerId), dateNum);
            System.out.println(transaction);
            list.add(transaction);
        }

        System.out.println(list);
        return recordTransaction2(list);
    }
}
