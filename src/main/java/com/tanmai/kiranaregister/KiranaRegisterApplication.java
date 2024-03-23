package com.tanmai.kiranaregister;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
