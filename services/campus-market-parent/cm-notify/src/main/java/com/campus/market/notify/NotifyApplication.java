package com.campus.market.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.campus.market.notify", "com.campus.market.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class NotifyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NotifyApplication.class, args);
    }
}
