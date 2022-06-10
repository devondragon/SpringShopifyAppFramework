package com.justblackmagic.shopify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ShopifyApplication {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ShopifyApplication.class, args);
	}

}
