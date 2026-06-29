package com.petty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.petty.mapper")
public class PettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PettyApplication.class, args);
    }
}
