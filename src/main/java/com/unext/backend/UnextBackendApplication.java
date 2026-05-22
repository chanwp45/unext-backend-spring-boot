package com.unext.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UnextBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnextBackendApplication.class, args);
    }
}
