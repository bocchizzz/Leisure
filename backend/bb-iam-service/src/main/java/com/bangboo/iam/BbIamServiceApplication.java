package com.bangboo.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.iam", "com.bangboo.common"})
public class BbIamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbIamServiceApplication.class, args);
    }
}
