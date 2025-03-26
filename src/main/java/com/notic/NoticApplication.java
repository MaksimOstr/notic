package com.notic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class NoticApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticApplication.class, args);
    }

}
