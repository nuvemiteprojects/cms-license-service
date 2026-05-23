package com.nuvemite.cms.licenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LicensesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LicensesApplication.class, args);
    }
}
