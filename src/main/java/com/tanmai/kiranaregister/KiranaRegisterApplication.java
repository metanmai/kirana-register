package com.tanmai.kiranaregister;

import com.google.common.util.concurrent.RateLimiter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
		private final WebClient webClient;

		public CurrencyConfig(WebClient webClient) {
			this.webClient = webClient;
		}

		@Bean
		@RequestScope
		public HashMap<String, Double> currencyRates() {
			System.out.println("----------------------------------------------------------------");
			System.out.println("Here");
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
			StringBuffer requestUrl = request.getRequestURL();
			String servletPath = request.getServletPath();
			int endIndex = requestUrl.indexOf(servletPath);
			String baseUrl = requestUrl.substring(0, endIndex);

			System.out.println("----------------------------------------------------------------");
			System.out.println("Base URL: " + baseUrl);

			try {
                HashMap<String, Double> currencyRates = this.webClient.get()
					.uri(baseUrl + "/currencies")
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<HashMap<String, Double>>() {})
					.block();

                System.out.println("Fetched currency rates: " + currencyRates);
                return currencyRates;
			}
	
			catch(Exception e) {
				System.out.println("Could not fetch currency rates: " + e.getMessage());
				throw e;
			}
		}
	}

	@Configuration
	public class RateLimitConfig {

		private final int requestsPerMinuteCurrency = 10;
		private final int requestsPerMinuteTransaction = 5;
		private final int requestsPerMinuteReport = 20;
	
		@Bean(name = "currencyRateLimiter")
		public RateLimiter currencyRateLimiter() {
			return RateLimiter.create(requestsPerMinuteCurrency / 60.0);
		}
	
		@Bean(name = "transactionRateLimiter")
		public RateLimiter transactionRateLimiter() {
			return RateLimiter.create(requestsPerMinuteTransaction / 60.0);
		}
	
		@Bean(name = "reportRateLimiter")
		public RateLimiter reportRateLimiter() {
			return RateLimiter.create(requestsPerMinuteReport / 60.0);
		}
	}
}
