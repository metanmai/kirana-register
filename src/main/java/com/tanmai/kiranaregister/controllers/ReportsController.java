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

import com.google.common.util.concurrent.RateLimiter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/*
 * The TransactionAnalytics class contains methods to analyze transaction data.
 * It contains methods to calculate the total number of transactions, the number of transactions by month,
 * the number of transactions by payment method, the number of transactions by currency, the total revenue,
 * the average transaction value, and the revenue by month.
 */
@Service
class TransactionAnalytics {
    private static HashMap<String, Double> currencyRates;

    private static HashMap<String, Double> getCurrencyRates() {
        if(currencyRates == null) {
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

    public static HashMap<String, Integer> getTransactionCountByMonth(List<HashMap<String, Object>> transactions) {
        HashMap<String, Integer> transactionsByMonth = new HashMap<>();

        for(HashMap<String, Object> transaction : transactions) {
            String month = ((Date) transaction.get("date")).toString().split(" ")[1];

            if(transactionsByMonth.containsKey(month)) {
                transactionsByMonth.put(month, transactionsByMonth.get(month) + 1);
            } 
            
            else {
                transactionsByMonth.put(month, 1);
            }
        }

        return transactionsByMonth;
    }

    public static Map<String, Integer> getTransactionCountByPaymentMethod(List<HashMap<String, Object>> transactions) {
        Map<String, Integer> paymentMethodCounts = new HashMap<>();

        for(HashMap<String, Object> transaction : transactions) {
            String paymentMethod = (String) transaction.get("paymentMethod");

            if(paymentMethodCounts.containsKey(paymentMethod)) {
                paymentMethodCounts.put(paymentMethod, paymentMethodCounts.get(paymentMethod) + 1);
            } 
            
            else {
                paymentMethodCounts.put(paymentMethod, 1);
            }
        }

        return paymentMethodCounts;
    }

    public static HashMap<String, Integer> getTransactionCountByCurrency(List<HashMap<String, Object>> transactions) {
        HashMap<String, Integer> transactionCountByCurrency = new HashMap<>();

        for(HashMap<String, Object> transaction : transactions) {
            String currency = (String) transaction.get("currency");

            if(transactionCountByCurrency.containsKey(currency)) {
                transactionCountByCurrency.put(currency, transactionCountByCurrency.get(currency) + 1);
            } 
            
            else {
                transactionCountByCurrency.put(currency, 1);
            }
        }

        return transactionCountByCurrency;
    }

    public static double getTotalRevenue(List<HashMap<String, Object>> transactions) {
        double totalRevenue = 0.0;

        for(HashMap<String, Object> transaction : transactions) {
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

    public static double getAverageTransactionValue(List<HashMap<String, Object>> transactions) {
        return getTotalRevenue(transactions) / getTransactionCount(transactions);
    }

    public static HashMap<String, Double> getRevenueByMonth(List<HashMap<String, Object>> transactions) {
        HashMap<String, Double> revenueByMonth = new HashMap<>();

        for(HashMap<String, Object> transaction : transactions) {
            String month = ((Date) transaction.get("date")).toString().split(" ")[1];
            double amount;

            if(transaction.get("currency").equals("USD")) {
                amount = (double) transaction.get("amount");
            } 
            
            else {
                amount = (double) transaction.get("amount") / getCurrencyRates().get(transaction.get("currency"));
            }

            if(revenueByMonth.containsKey(month)) {
                revenueByMonth.put(month, revenueByMonth.get(month) + amount);
            } 
            
            else {
                revenueByMonth.put(month, amount);
            }
        }

        return revenueByMonth;
    }
}

/*
 * The ReportsController class contains the endpoints to fetch reports for transactions.
 * It contains endpoints to fetch reports for transactions in the last week, month, and year.
 */
@RestController
@RequestMapping("/reports")
public class ReportsController {
    /*
     * The collection field is a MongoCollection object that represents the "transactions" collection in the database.
     * The rateLimiter field is a RateLimiter object that limits the rate of incoming requests.
     */
    private final MongoCollection<Document> collection;
    private final RateLimiter rateLimiter;

    public ReportsController(MongoClient mongoClient, @Qualifier("reportRateLimiter") RateLimiter rateLimiter) {
        this.collection = mongoClient.getDatabase("kirana-register-db").getCollection("transactions");
        this.rateLimiter = rateLimiter;
    }
    
    /*
     * The fetchTransactions method fetches transactions between the given start and end dates.
     */
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
                    put("transactionsByMonth", TransactionAnalytics.getTransactionCountByMonth(transactions));
                    put("transactionCountByPaymentMethod", TransactionAnalytics.getTransactionCountByPaymentMethod(transactions));
                    put("transactionCountByCurrency", TransactionAnalytics.getTransactionCountByCurrency(transactions));
                    put("totalRevenue(USD)", TransactionAnalytics.getTotalRevenue(transactions));
                    put("averageTransactionValue(USD)", TransactionAnalytics.getAverageTransactionValue(transactions));
                    put("revenueByMonth", TransactionAnalytics.getRevenueByMonth(transactions));
                }
            };

        } 
        
        catch(Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
            throw e;
        }
    }

    /*
     * The getReport method fetches the report for transactions in the given period.
     * It returns the report for the last week, month, or year.
     */
    @GetMapping("/{period}")
    public ResponseEntity<HashMap<String, Object>> getReport(@PathVariable String period) {
        if(rateLimiter.tryAcquire()) {
            
            Calendar startCalendar1 = Calendar.getInstance(), startCalendar2 = Calendar.getInstance();
            Date startDate1, startDate2;
            Date endDate = new Date();

            System.out.println("Fetching transactions...\n");

            switch(period) {
                case "weekly":
                    startCalendar1.add(Calendar.DAY_OF_MONTH, -7);

                    startDate1 = startCalendar1.getTime();

                    return ResponseEntity.ok(fetchTransactions(startDate1, endDate));

                case "monthly":
                    startCalendar1.set(Calendar.MONTH, Calendar.JANUARY);
                    startCalendar1.set(Calendar.DAY_OF_MONTH, 1);
                    startCalendar1.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar1.set(Calendar.MINUTE, 0);
                    startCalendar1.set(Calendar.SECOND, 0);
                    startCalendar1.set(Calendar.MILLISECOND, 0);
            
                    startCalendar2.add(Calendar.MONTH, -1);

                    startDate1 = startCalendar1.getTime();
                    startDate2 = startCalendar2.getTime();

                    return ResponseEntity.ok(new HashMap<>() {
                        {
                            put("MTD", fetchTransactions(startDate1, endDate));
                            put("lastMonth", fetchTransactions(startDate2, endDate));
                        }
                    });

                case "yearly":
                    startCalendar1.set(Calendar.MONTH, Calendar.JANUARY);
                    startCalendar1.set(Calendar.DAY_OF_MONTH, 1);
                    startCalendar1.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar1.set(Calendar.MINUTE, 0);
                    startCalendar1.set(Calendar.SECOND, 0);
                    startCalendar1.set(Calendar.MILLISECOND, 0);

                    startCalendar2.set(Calendar.YEAR, startCalendar2.get(Calendar.YEAR) - 1);

                    startDate1 = startCalendar1.getTime();
                    startDate2 = startCalendar2.getTime();

                    return ResponseEntity.ok(new HashMap<>() {
                        {
                            put("YTD", fetchTransactions(startDate1, endDate));
                            put("lastYear", fetchTransactions(startDate2, endDate));
                        }
                    });
            
                default:
                    throw new IllegalArgumentException("Invalid period.");
            }
        }

        else {
            throw new RuntimeException("Rate limit exceeded. Try again later.");
        }
    }
}
