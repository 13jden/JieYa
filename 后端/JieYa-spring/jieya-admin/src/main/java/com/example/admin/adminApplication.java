package com.example.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication(scanBasePackages = "com.example.admin")
@EnableTransactionManagement
@EnableScheduling
@ComponentScan(basePackages = {"com.example.admin", "com.example.common"})
public class adminApplication {
    public static void main(String[] args) {
        SpringApplication.run(adminApplication.class, args);
    }

}
