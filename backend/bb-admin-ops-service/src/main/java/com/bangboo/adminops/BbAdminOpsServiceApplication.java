package com.bangboo.adminops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.adminops", "com.bangboo.common"})
public class BbAdminOpsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbAdminOpsServiceApplication.class, args);
    }
}
