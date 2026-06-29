package com.petty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.petty.mapper")
@EnableScheduling
public class PettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PettyApplication.class, args);
    }
}
