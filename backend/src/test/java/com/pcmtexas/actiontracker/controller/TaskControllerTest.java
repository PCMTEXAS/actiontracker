package com.pcmtexas.actiontracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pcmtexas.actiontracker.dto.TaskCreateRequest;
import com.pcmtexas.actiontracker.dto.TaskDTO;
import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private TaskActivityRepository taskActivityRepository;

    private ObjectMapper objectMapper;
    private AppUser mockUser;
    private UUID taskId;
    private TaskDTO sampleTaskDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        taskId = UUID.randomUUID();

        mockUser = AppUser.builder()
                .id(UUID.randomUUID())
                .email("test@digitalchalk.com")
                .name("Test User")
                .role(UserRole.OWNER)
                .dailyDigestEnabled(true)
                .build();

        sampleTaskDTO = TaskDTO.builder()
                .id(taskId)
                .title("Sample Task")
                .description("Sample Description")
                .assigneeEmail("assignee@digitalchalk.com")
                .assigneeName("Assignee Name")
                .assignedByEmail("test@digitalchalk.com")
                .assignedByName("Test User")
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .status(Status.NOT_STARTED)
                .isRecurring(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
    }

    @Test
    void getTasks_authenticatedOwner_returns200() throws Exception {
        when(taskService.getTasks(any(), anyString(), any())).thenReturn(List.of(sampleTaskDTO));

        mockMvc.perform(get("/api/tasks")
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "test@digitalchalk.com")
                                        .claim("name", "Test User"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Sample Task"));
    }

    @Test
    void getTaskById_existingId_returns200() throws Exception {
        when(taskService.getTaskById(eq(taskId))).thenReturn(sampleTaskDTO);

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "test@digitalchalk.com")
                                        .claim("name", "Test User"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    void getTaskById_notFound_returns404() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        when(taskService.getTaskById(eq(nonExistingId)))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + nonExistingId));

        mockMvc.perform(get("/api/tasks/{id}", nonExistingId)
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "test@digitalchalk.com")
                                        .claim("name", "Test User"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTask_validBody_returns201() throws Exception {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("New Task")
                .assigneeEmail("assignee@digitalchalk.com")
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .build();

        when(taskService.createTask(any(TaskCreateRequest.class), anyString(), anyString()))
                .thenReturn(sampleTaskDTO);

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "test@digitalchalk.com")
                                        .claim("name", "Test User")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sample Task"));
    }

    @Test
    void deleteTask_existingId_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(eq(taskId));

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .with(csrf())
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "test@digitalchalk.com")
                                        .claim("name", "Test User"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_notFound_returns404() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Task not found with id: " + nonExistingId))
                .when(taskService).deleteTask(eq(nonExistingId));

        mockMvc.perform(delete("/api/tasks/{id}", nonExistingId)
                        .with(csrf())
                        .with(oidcLogin()
                                .userInfoToken(t -> t
                                        .claim("email", "test@digitalchalk.com")
                                        .claim("name", "Test User"))))
                .andExpect(status().isNotFound());
    }
}
