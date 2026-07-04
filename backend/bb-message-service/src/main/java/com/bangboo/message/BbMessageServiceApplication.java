package com.bangboo.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.message", "com.bangboo.common"})
public class BbMessageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbMessageServiceApplication.class, args);
    }
}
