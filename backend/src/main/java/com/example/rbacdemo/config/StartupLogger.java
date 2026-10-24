package com.example.rbacdemo.config;



import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class StartupLogger implements CommandLineRunner {
    @Value("${server.port:8080}")
    private String port;

    @Override
    public void run(String... args){
        System.out.println(" 接口文档：http://localhost:"+port+"/swagger-ui/index.html");
    }
}
