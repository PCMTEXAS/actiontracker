package com.pcmtexas.actiontracker.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Serves the Angular SPA index.html for all non-API, non-asset routes.
 * Static assets (.js, .css, etc.) are served automatically by Spring Boot from classpath:/static/.
 * This controller catches everything else (Angular client-side routes like /dashboard, /tasks/123)
 * and returns index.html so the Angular router can handle them.
 */
@RestController
public class SpaController {

    private final Resource indexHtml = new ClassPathResource("static/index.html");

    @GetMapping(value = "/{path:^(?!api|actuator|oauth2|login|error|assets)[^.]*$}/**",
                produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> forward(HttpServletRequest request) throws IOException {
        if (!indexHtml.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtml);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> root() throws IOException {
        if (!indexHtml.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtml);
    }
}
