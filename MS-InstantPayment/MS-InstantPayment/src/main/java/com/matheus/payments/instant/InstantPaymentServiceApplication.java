package com.matheus.payments.instant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@SpringBootApplication
public class InstantPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstantPaymentServiceApplication.class, args);
	}

}
