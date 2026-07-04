package com.bangboo.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bangboo.file", "com.bangboo.common"})
public class BbFileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbFileServiceApplication.class, args);
    }
}
