package com.example.servicediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServicediscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicediscoveryApplication.class, args);
        System.out.println("The Service Discovery is running");
    }

}
