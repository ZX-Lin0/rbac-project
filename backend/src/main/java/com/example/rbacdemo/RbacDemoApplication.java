package com.example.rbacdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.rbacdemo.mapper")
public class RbacDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbacDemoApplication.class, args);
    }

}
