package com.ace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Adjust the allowed origins as needed
                        .allowedOrigins("http://localhost:3000","https://cuddly-fiesta-qzznz77.pages.github.io")
                        //allow all methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        //allow headers
                        .allowedHeaders("*")
                        //allow Cookies
                        .allowCredentials(true);
            }
        };
    }
}