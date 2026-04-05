package com.codemaxxers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.codemaxxers")
@EntityScan(basePackages = "com.codemaxxers.model")
@EnableJpaRepositories(basePackages = "com.codemaxxers.repository")
public class StudyMoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyMoreApplication.class, args);
    }
}