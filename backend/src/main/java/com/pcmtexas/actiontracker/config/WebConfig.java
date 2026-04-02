package com.pcmtexas.actiontracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition", "Content-Length")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/login/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST")
                .allowCredentials(true);

        registry.addMapping("/oauth2/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST")
                .allowCredentials(true);
    }
}
