package com.Spring.AI_Customer_Support_Backend_System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AiCustomerSupportBackendSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCustomerSupportBackendSystemApplication.class, args);
	}

}
