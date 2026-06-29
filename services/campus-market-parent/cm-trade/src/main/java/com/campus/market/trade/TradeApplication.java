package com.campus.market.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.campus.market.trade", "com.campus.market.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class TradeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
