package com.tanmai.kiranaregister.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

import org.bson.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
class TransactionAnalytics {
    // private final WebClient webClient;
    private static HashMap<String, Double> currencyRates;

    // @Autowired
    // private TransactionAnalytics(WebClient webClient) {
    //     this.webClient = webClient;
    // }

    private static HashMap<String, Double> getCurrencyRates() {
        if (currencyRates == null) {
            try {
                WebClient webClient = WebClient.create();

                @SuppressWarnings("unchecked")
                Mono<HashMap<String, Double>> response = webClient.get()
                .uri("https://api.fxratesapi.com/latest")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<HashMap<String, Object>>() {})
                .map(responseBody -> (HashMap<String, Double>) responseBody.get("rates"));

                currencyRates = response.block();
            }
    
            catch(Exception e) {
                System.out.println("Could not fetch currency rates: " + e.getMessage());
                throw e;
            }
        }
        
        return currencyRates;
    }

    public static int getTransactionCount(List<HashMap<String, Object>> transactions) {
        return transactions.size();
    }

    public static double getTotalRevenue(List<HashMap<String, Object>> transactions) {
        double totalRevenue = 0.0;

        for (HashMap<String, Object> transaction : transactions) {
            double revenue;

            if(transaction.get("currency").equals("USD")) {
                revenue = (double) transaction.get("amount");
            } 
            
            else {
                revenue = (double) transaction.get("amount") / getCurrencyRates().get(transaction.get("currency"));
            }

            totalRevenue += revenue;
        }

        return totalRevenue;
    }
}

@RestController
@RequestMapping("/reports")
public class ReportsController {
    private final MongoCollection<Document> collection;

    public ReportsController(MongoClient mongoClient) {
        this.collection = mongoClient.getDatabase("kirana-register-db").getCollection("transactions");
    }

    public HashMap<String, Object> fetchTransactions(Date startDate, Date endDate) {
        try {
            Document query = new Document("date", new Document("$gte", startDate).append("$lt", endDate));

            FindIterable<Document> result = collection.find(query);

            // Iterate over the results and process them
            System.out.println("Transactions between " + startDate + " and " + endDate);
            AtomicInteger count = new AtomicInteger(0);
            List<HashMap<String, Object>> transactions = new ArrayList<>();

            for (Document transaction : result) {
                count.incrementAndGet();
                HashMap<String, Object> transactionMap = new HashMap<>();

                for(Map.Entry<String, Object> element : transaction.entrySet()) {
                    transactionMap.put(element.getKey(), element.getValue());
                }

                transactions.add(transactionMap);
            }

            return new HashMap<> () {
                {
                    put("transactionCount", TransactionAnalytics.getTransactionCount(transactions));
                    put("totalRevenue(USD)", TransactionAnalytics.getTotalRevenue(transactions));
                    put("transactions", transactions);
                }
            };

        } 
        
        catch(Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/weekly")
    public String getWeeklyReport() {
        return "Weekly Report";
    }

    @GetMapping("/monthly")
    public String getMonthlyReport() {
        return "Monthly Report";
    }
    
    @GetMapping("/yearly")
    public ResponseEntity<HashMap<String, Object>> getYearlyReport() {
        Calendar startCalendar1 = Calendar.getInstance(), startCalendar2 = Calendar.getInstance();

        startCalendar1.set(Calendar.MONTH, Calendar.JANUARY);
        startCalendar1.set(Calendar.DAY_OF_MONTH, 1);
        startCalendar1.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar1.set(Calendar.MINUTE, 0);
        startCalendar1.set(Calendar.SECOND, 0);
        startCalendar1.set(Calendar.MILLISECOND, 0);

        startCalendar2.set(Calendar.YEAR, startCalendar2.get(Calendar.YEAR) - 1);

        Date startDate1 = startCalendar1.getTime(), startDate2 = startCalendar2.getTime(), endDate = new Date();
        System.out.println("startDate1: " + startDate1 + " startDate2: " + startDate2 + " endDate: " + endDate + "\n");
        System.out.println("Fetching transactions...\n");

        HashMap<String, Object> yearToDate = fetchTransactions(startDate1, endDate);
        HashMap<String, Object> lastYear = fetchTransactions(startDate2, endDate);

        return ResponseEntity.ok(new HashMap<>() {
            {
                put("YTD", yearToDate);
                put("lastYear", lastYear);
            }
        });
    }
    
}
