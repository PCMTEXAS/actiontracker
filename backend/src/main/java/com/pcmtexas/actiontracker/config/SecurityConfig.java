package com.pcmtexas.actiontracker.config;

import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AppUserRepository appUserRepository;

    @Value("${app.domain-restriction}")
    private String domainRestriction;

    @Value("${app.owner-email}")
    private String ownerEmail;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/login/**", "/oauth2/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler())
                        .failureUrl(frontendUrl + "/login?error=oauth_failed")
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl(frontendUrl + "/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Authentication authentication) throws IOException {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                String email = oidcUser.getEmail();

                if (email == null || email.isBlank()) {
                    log.warn("OAuth login attempted with null/blank email");
                    response.sendRedirect(frontendUrl + "/login?error=no_email");
                    return;
                }

                // Domain restriction check
                if (!email.toLowerCase().endsWith("@" + domainRestriction.toLowerCase())) {
                    log.warn("OAuth login rejected for email {} - domain not allowed", email);
                    response.sendRedirect(frontendUrl + "/login?error=domain_restricted");
                    return;
                }

                // Upsert user in the database
                AppUser appUser = appUserRepository.findByEmail(email)
                        .map(existing -> {
                            // Update profile info on each login
                            existing.setName(oidcUser.getFullName() != null ? oidcUser.getFullName() : email);
                            if (oidcUser.getPicture() != null) {
                                existing.setPictureUrl(oidcUser.getPicture());
                            }
                            return existing;
                        })
                        .orElseGet(() -> {
                            log.info("Creating new AppUser for email: {}", email);
                            return AppUser.builder()
                                    .email(email)
                                    .name(oidcUser.getFullName() != null ? oidcUser.getFullName() : email)
                                    .pictureUrl(oidcUser.getPicture())
                                    .role(UserRole.MEMBER)
                                    .dailyDigestEnabled(true)
                                    .build();
                        });

                // Assign OWNER role if email matches configured owner
                if (email.equalsIgnoreCase(ownerEmail)) {
                    appUser.setRole(UserRole.OWNER);
                    log.info("Assigned OWNER role to {}", email);
                }

                appUserRepository.save(appUser);
                log.info("OAuth login successful for {} (role: {})", email, appUser.getRole());

                response.sendRedirect(frontendUrl + "/dashboard");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition", "Content-Length"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
