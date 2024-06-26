package com.tanmai.kiranaregister.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.common.util.concurrent.RateLimiter;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * The CurrencyController class contains the endpoints to fetch latest currency conversion rates.
 */
@RestController
public class CurrencyController {
    /*
     * The webClient field is a WebClient object that makes HTTP requests to the currency conversion API.
     * The rateLimiter field is a RateLimiter object that limits the rate of incoming requests.
     * The cachedCurrencyRates field is a HashMap that stores the currency rates fetched from the API.
     */
    private final WebClient webClient;
    private RateLimiter rateLimiter;
    private HashMap<String, Double> cachedCurrencyRates;

    public CurrencyController(WebClient webClient, @Qualifier("currencyRateLimiter") RateLimiter rateLimiter) {
        this.webClient = webClient;
        this.rateLimiter = rateLimiter;
    }

    /*
     * Scheduled method to clear the cached currency rates every hour.
     */
    @Scheduled(fixedRate = 3600000)
    public void clearCache() {
        this.cachedCurrencyRates = null;
    }
    
    /*
     * Endpoint to fetch the latest currency conversion rates.
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/currencies")
    public HashMap<String, Double> getCurrencies() {
        System.out.println("Fetching currency rates...");

        if(this.cachedCurrencyRates != null) {
            return this.cachedCurrencyRates;
        }

        if(rateLimiter.tryAcquire()) {
            try {
                HashMap<String, Double> currencyRates = this.webClient.get()
				.uri("https://api.fxratesapi.com/latest")
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<HashMap<String, Object>>() {})
				.map(responseBody -> (HashMap<String, Double>) responseBody.get("rates"))
				.block();

                this.cachedCurrencyRates = new HashMap<>();

                for(String currency : currencyRates.keySet()) {
                    if(currencyRates.get(currency) instanceof Double) {
                        this.cachedCurrencyRates.put(currency, currencyRates.get(currency));
                    }

                    else {
                        // WHYYYYYY
                        Object val = currencyRates.get(currency);
                        this.cachedCurrencyRates.put(currency, Double.valueOf((Integer) val));
                    }
                }
                
                return this.cachedCurrencyRates;
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
