package com.tanmai.kiranaregister.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Mono;


@RestController
public class Controller {
    private final WebClient webClient;

    @Autowired
    public Controller(WebClient webClient) {
        this.webClient = webClient;
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

    @GetMapping("/currencies")
    public Mono<Map<String, Object>> getCurrencies() {
        try {
            return webClient.get()
            .uri("https://api.fxratesapi.com/latest")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .map(responseBody -> (Map<String, Object>) responseBody.get("rates"));
        }

        catch(Exception e) {
            System.out.println("An error has occured: " + e.getMessage());
            return Mono.empty();
        }
    }
}
