package com.Spring.AI_Customer_Support_Backend_System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync //We enable Async processing ---
public class AiCustomerSupportBackendSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCustomerSupportBackendSystemApplication.class, args);
	}

}
