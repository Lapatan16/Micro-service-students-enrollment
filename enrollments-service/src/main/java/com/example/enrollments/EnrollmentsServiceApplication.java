package com.example.enrollments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableFeignClients
public class EnrollmentsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnrollmentsServiceApplication.class, args);
    }
}