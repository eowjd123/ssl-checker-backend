package com.example.sslcheckback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.sslcheckback", "controller", "service", "dto", "entity", "repository"})
@EntityScan(basePackages = "entity")
@EnableJpaRepositories(basePackages = "repository")
public class SslChackBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(SslChackBackApplication.class, args);
    }
}