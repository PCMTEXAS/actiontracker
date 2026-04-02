package com.pcmtexas.actiontracker.controller;

import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserRepository appUserRepository;

    private AppUser user1;
    private AppUser user2;

    @BeforeEach
    void setUp() {
        user1 = AppUser.builder()
                .id(UUID.randomUUID())
                .email("alice@digitalchalk.com")
                .name("Alice")
                .role(UserRole.OWNER)
                .dailyDigestEnabled(true)
                .build();

        user2 = AppUser.builder()
                .id(UUID.randomUUID())
                .email("bob@digitalchalk.com")
                .name("Bob")
                .role(UserRole.MEMBER)
                .dailyDigestEnabled(true)
                .build();
    }

    @Test
    void getTeamMembers_returnsUserList() throws Exception {
        when(appUserRepository.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users")
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "alice@digitalchalk.com")
                                        .claim("name", "Alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("alice@digitalchalk.com"))
                .andExpect(jsonPath("$[1].email").value("bob@digitalchalk.com"));
    }

    @Test
    void getMe_authenticatedUser_returnsCurrentUser() throws Exception {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/api/users/me")
                        .with(oidcLogin()
                                .idToken(t -> t
                                        .claim("email", "alice@digitalchalk.com")
                                        .claim("name", "Alice"))
                                .userInfoToken(t -> t
                                        .claim("email", "alice@digitalchalk.com")
                                        .claim("name", "Alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@digitalchalk.com"))
                .andExpect(jsonPath("$.name").value("Alice"));
    }
}
