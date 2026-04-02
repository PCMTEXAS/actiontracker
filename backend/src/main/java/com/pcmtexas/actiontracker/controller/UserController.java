package com.pcmtexas.actiontracker.controller;

import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AppUserRepository appUserRepository;

    @GetMapping
    public ResponseEntity<List<UserDTO>> listUsers(@AuthenticationPrincipal OidcUser principal) {
        List<UserDTO> users = appUserRepository.findAll()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal OidcUser principal) {
        String email = principal.getEmail();

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Should normally be created during OAuth login, but handle gracefully
                    log.warn("AppUser not found for email {} during /me request", email);
                    return AppUser.builder()
                            .email(email)
                            .name(principal.getFullName() != null ? principal.getFullName() : email)
                            .pictureUrl(principal.getPicture())
                            .role(UserRole.MEMBER)
                            .dailyDigestEnabled(true)
                            .build();
                });

        return ResponseEntity.ok(UserDTO.fromEntity(appUser));
    }

    // ---- DTO ----

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private UUID id;
        private String email;
        private String name;
        private String pictureUrl;
        private UserRole role;
        private boolean dailyDigestEnabled;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public static UserDTO fromEntity(AppUser user) {
            return UserDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .pictureUrl(user.getPictureUrl())
                    .role(user.getRole())
                    .dailyDigestEnabled(user.isDailyDigestEnabled())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }
}
