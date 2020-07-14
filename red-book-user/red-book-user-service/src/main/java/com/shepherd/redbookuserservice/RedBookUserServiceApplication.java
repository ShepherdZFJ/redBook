package com.shepherd.redbookuserservice;

import com.shepherd.redbookuserservice.config.CasProperties;
import com.shepherd.redbookuserservice.utils.CookieBaseSessionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class RedBookUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedBookUserServiceApplication.class, args);
    }

    @Bean
    public CasProperties casProperties(){
        return new CasProperties();
    }

    @Bean
    public CookieBaseSessionUtils cookieBasedSession(CasProperties casProperties){
        CookieBaseSessionUtils cookieBasedSession = new CookieBaseSessionUtils();
        cookieBasedSession.setCasProperties(casProperties);
        return cookieBasedSession;
    }


}
