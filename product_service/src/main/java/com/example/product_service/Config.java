package com.example.product_service;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.HttpMethod;

@Configuration
@EnableWebSecurity
public class Config extends WebSecurityConfigurerAdapter {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()  // Disable CSRF protection
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/products/**").permitAll() // Allow GET requests to /api/products/** without authentication
                .antMatchers(HttpMethod.POST, "/api/products").permitAll() // Allow POST requests to /api/products without authentication
                .antMatchers(HttpMethod.PUT, "/api/products/**").permitAll() // Allow PUT requests to /api/products/**
                .antMatchers(HttpMethod.DELETE, "/api/products/**").permitAll() // Allow DELETE requests to /api/products/**
                .anyRequest().authenticated() // All other endpoints require authentication
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Ensure stateless session management
    }
}
