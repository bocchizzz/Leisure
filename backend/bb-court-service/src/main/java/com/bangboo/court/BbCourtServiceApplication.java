package com.bangboo.court;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.court", "com.bangboo.common"})
public class BbCourtServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbCourtServiceApplication.class, args);
    }
}
