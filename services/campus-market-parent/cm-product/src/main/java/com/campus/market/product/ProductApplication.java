package com.campus.market.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.campus.market.product", "com.campus.market.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.campus.market.product.feign")
@EnableScheduling
public class ProductApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
