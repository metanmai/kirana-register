package com.tanmai.kiranaregister.controllers;

import com.tanmai.kiranaregister.model.TransactionModel;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class Controller {
    private final WebClient webClient;
    private List<String> paymentMethods;

    public Controller(WebClient webClient) {
        this.webClient = webClient;
        this.paymentMethods = List.of("Cash", "UPI", "Card", "Netbanking");
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue="World") String name) {
        return new String("Hello " + name + "!");
    }

    @GetMapping("/getJSON")
    public Object getJson() {
        Map<String, Object> jsonObj = new HashMap<>(), innerJsonObj = new HashMap<>() { {
                put("Inner1", "InnerVal1");
                put("Inner2", "InnerVal2");
            }
        };
        
        jsonObj.put("Hello", 1);
        jsonObj.put("Tanmai", "Niranjan");
        jsonObj.put("InnerJson", innerJsonObj);
        return jsonObj;
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

    @PostMapping("/transaction")
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
                    put("transaction", transaction);
                }
            });
        }
    }
    
}
