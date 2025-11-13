package com.schoolerp.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class StaffServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StaffServiceApplication.class, args);
    }
}