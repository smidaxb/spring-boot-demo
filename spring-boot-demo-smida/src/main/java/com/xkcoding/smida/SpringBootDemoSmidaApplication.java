package com.xkcoding.smida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBootDemoSmidaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDemoSmidaApplication.class, args);
    }

}
