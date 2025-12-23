package com.justblackmagic.shopify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ShopifyApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies the Spring application context loads successfully
		assertTrue(true);
	}

}
