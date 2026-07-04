package com.bangboo.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.ai", "com.bangboo.common"})
public class BbAiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbAiServiceApplication.class, args);
    }
}
