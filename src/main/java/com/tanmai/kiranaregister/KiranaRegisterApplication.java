package com.tanmai.kiranaregister;

import com.google.common.util.concurrent.RateLimiter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import jakarta.servlet.http.HttpServletRequest;

// import java.io.IOException;
import java.util.HashMap;

// import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
// import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// import static org.springframework.security.config.Customizer.withDefaults;

/*
 * The KiranaRegisterApplication class is the entry point for the Spring Boot application.
 * It contains the main method that starts the Spring Boot application.
 * It also contains the configuration for the WebClient, MongoClient, currencyRates and RateLimiter beans.
 */

@SpringBootApplication
public class KiranaRegisterApplication {

	public static void main(String[] args) {
		SpringApplication.run(KiranaRegisterApplication.class, args);
	}

	// @Configuration
	// @EnableWebSecurity
	// public class SecurityConfig {

	// 	@Value("${okta.oauth2.issuer}")
	// 	private String issuer;
	// 	@Value("${okta.oauth2.client-id}")
	// 	private String clientId;

	// 	@Bean
	// 	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
	// 		http
	// 			.authorizeHttpRequests(authorize -> authorize
	// 				.requestMatchers("/").permitAll()
	// 				.anyRequest().authenticated()
	// 			)
	// 			.oauth2Login(withDefaults())

	// 			// configure logout with Auth0
	// 			.logout(logout -> logout
	// 				.addLogoutHandler(logoutHandler()));
	// 		return http.build();
	// 	}

	// 	private LogoutHandler logoutHandler() {
	// 		return (request, response, authentication) -> {
	// 			try {
	// 				String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	// 				response.sendRedirect(issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl);
	// 			} catch (IOException e) {
	// 				throw new RuntimeException(e);
	// 			}
	// 		};
	// 	}
	// }

	/*
	 * The WebClientConfig class contains the configuration for the WebClient bean.
	 */
	@Configuration
	public class WebClientConfig {

		@Bean
		public WebClient webClient() {
			return WebClient.create();
		}
	}

	/*
	 * The MongoClientConfig class contains the configuration for the MongoClient bean.
	 */
	@Configuration
	public class MongoClientConfig {
		// private final String mongodbConnectionString;

		@Bean
		public MongoClient mongoClient() {
			return MongoClients.create("mongodb+srv://metanmai:haLCDjdxghAuiblE@cluster0.tun73v2.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
		}
	}

	/*
	 * The CurrencyConfig class contains the configuration for the currencyRates bean.
	 * It fetches the currency rates from the currency service and stores them in a HashMap.
	 */
	@Configuration
	public class CurrencyConfig {
		private final WebClient webClient;

		public CurrencyConfig(WebClient webClient) {
			this.webClient = webClient;
		}

		@Bean
		@RequestScope
		public HashMap<String, Double> currencyRates() {
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

	/*
	 * The RateLimitConfig class contains the configuration for the RateLimiter beans.
	 * It creates RateLimiter beans for the currency, transaction and report endpoints.
	 */
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
