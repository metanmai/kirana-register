package com.tanmai.kiranaregister;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class KiranaRegisterApplication {

	public static void main(String[] args) {
		SpringApplication.run(KiranaRegisterApplication.class, args);
	}

	@Configuration
	public class WebClientConfig {

		@Bean
		public WebClient webClient() {
			return WebClient.create();
		}
	}

	@Configuration
	public class MongoClientConfig {
		// private final String mongodbConnectionString;

		@Bean
		public MongoClient mongoClient() {
			return MongoClients.create("mongodb+srv://metanmai:haLCDjdxghAuiblE@cluster0.tun73v2.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
		}
	}

	@Configuration
	public class CurrencyConfig {

		@SuppressWarnings("unchecked")
		@Bean
		public HashMap<String, Double> currencyRates() {
			try {
				return WebClient.create().get()
				.uri("https://api.fxratesapi.com/latest")
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<HashMap<String, Object>>() {})
				.map(responseBody -> (HashMap<String, Double>) responseBody.get("rates"))
				.block();
			}
	
			catch(Exception e) {
				System.out.println("Could not fetch currency rates: " + e.getMessage());
				throw e;
			}
		}
	}
}
