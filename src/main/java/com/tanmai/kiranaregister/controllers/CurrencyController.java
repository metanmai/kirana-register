package com.tanmai.kiranaregister.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.common.util.concurrent.RateLimiter;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class CurrencyController {
    private final WebClient webClient;
    
    @Autowired
    private RateLimiter rateLimiter;

    public CurrencyController(WebClient webClient, RateLimiter rateLimiter) {
        this.webClient = webClient;
    }
    
    @SuppressWarnings("unchecked")
    @GetMapping("/currencies")
    public HashMap<String, Double> getCurrencies() {
        System.out.println("Fetching currency rates...");

        if(rateLimiter.tryAcquire()) {
            try {
                HashMap<String, Double> currencyRates = this.webClient.get()
				.uri("https://api.fxratesapi.com/latest")
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<HashMap<String, Object>>() {})
				.map(responseBody -> (HashMap<String, Double>) responseBody.get("rates"))
				.block();

                HashMap<String, Double> newCurrencyRates = new HashMap<>();

                for(String currency : currencyRates.keySet()) {
                    if(currencyRates.get(currency) instanceof Double) {
                        newCurrencyRates.put(currency, currencyRates.get(currency));
                    }

                    else {
                        // WHYYYYYY
                        Object val = currencyRates.get(currency);
                        newCurrencyRates.put(currency, Double.valueOf((Integer) val));
                    }
                }

                return newCurrencyRates;
			}
	
			catch(Exception e) {
				System.out.println("Could not fetch currency rates: " + e.getMessage());
				throw e;
			}
        }

        else {
            throw new RuntimeException("Rate limit exceeded. Try again later.");
        }
    }
    
}
