package com.bangboo.reputation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.reputation", "com.bangboo.common"})
public class BbReputationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbReputationServiceApplication.class, args);
    }
}
