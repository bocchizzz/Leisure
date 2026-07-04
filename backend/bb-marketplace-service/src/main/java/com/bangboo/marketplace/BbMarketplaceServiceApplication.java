package com.bangboo.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.marketplace", "com.bangboo.common"})
public class BbMarketplaceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbMarketplaceServiceApplication.class, args);
    }
}
