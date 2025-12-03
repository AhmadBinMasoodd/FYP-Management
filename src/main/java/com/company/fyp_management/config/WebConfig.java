package com.company.fyp_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Example of global CORS configuration in Spring Boot
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
            .allowedOrigins("http://localhost:3000") // ⬅️ YOUR REACT ORIGIN
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow these methods
            // ⬇️ THIS IS THE CRITICAL MISSING HEADER 
            .allowCredentials(true) // Set Access-Control-Allow-Credentials: true
            .maxAge(3600); // Optional: Cache preflight response for 1 hour
    }
}