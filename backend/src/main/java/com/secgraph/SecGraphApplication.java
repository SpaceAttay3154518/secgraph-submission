package com.secgraph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SecGraphApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecGraphApplication.class, args);
    }
}
