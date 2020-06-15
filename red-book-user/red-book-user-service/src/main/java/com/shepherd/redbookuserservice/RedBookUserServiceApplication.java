package com.shepherd.redbookuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class RedBookUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedBookUserServiceApplication.class, args);
    }

}
