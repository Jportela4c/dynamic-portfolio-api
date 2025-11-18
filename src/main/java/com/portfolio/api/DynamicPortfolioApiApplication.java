package com.portfolio.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class DynamicPortfolioApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicPortfolioApiApplication.class, args);
    }
}
