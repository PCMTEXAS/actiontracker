package com.pcmtexas.actiontracker.controller;

import com.pcmtexas.actiontracker.dto.*;
import com.pcmtexas.actiontracker.dto.BulkCreateRequest.ExtractedTaskItem;
import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final AppUserRepository appUserRepository;
    private final TaskActivityRepository taskActivityRepository;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @RequestParam(required = false) String assigneeEmail,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) String projectTag,
            @RequestParam(required = false) Boolean recurringOnly,
            @AuthenticationPrincipal OidcUser principal) {

        AppUserDetails currentUser = getCurrentUser(principal);

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .assigneeEmail(assigneeEmail)
                .status(status)
                .priority(priority)
                .dueDateFrom(dueDateFrom)
                .dueDateTo(dueDateTo)
                .projectTag(projectTag)
                .recurringOnly(recurringOnly)
                .build();

        List<TaskDTO> tasks = taskService.getTasks(filter, currentUser.email(), currentUser.role());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable UUID id,
                                                @AuthenticationPrincipal OidcUser principal) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskCreateRequest request,
                                               @AuthenticationPrincipal OidcUser principal) {
        AppUserDetails currentUser = getCurrentUser(principal);
        TaskDTO created = taskService.createTask(request, currentUser.email(), currentUser.name());
        return ResponseEntity.status(201).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable UUID id,
                                               @RequestBody TaskUpdateRequest request,
                                               @AuthenticationPrincipal OidcUser principal) {
        AppUserDetails currentUser = getCurrentUser(principal);
        TaskDTO updated = taskService.updateTask(id, request, currentUser.email(), currentUser.name());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id,
                                            @AuthenticationPrincipal OidcUser principal) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/extract")
    public ResponseEntity<List<ExtractedTaskItem>> extractTasks(
            @Valid @RequestBody ExtractRequest request,
            @AuthenticationPrincipal OidcUser principal) {

        List<ExtractedTaskItem> extracted = taskService.extractTasksFromNotes(request.getNotes());
        return ResponseEntity.ok(extracted);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TaskDTO>> bulkCreateTasks(
            @RequestBody BulkCreateRequest request,
            @AuthenticationPrincipal OidcUser principal) {

        AppUserDetails currentUser = getCurrentUser(principal);

        // Convert ExtractedTaskItem list to TaskCreateRequest list
        List<TaskCreateRequest> createRequests = new ArrayList<>();
        if (request.getItems() != null) {
            for (ExtractedTaskItem item : request.getItems()) {
                if (item.getTitle() == null || item.getTitle().isBlank()) {
                    continue;
                }
                if (item.getAssigneeEmail() == null || item.getAssigneeEmail().isBlank()) {
                    log.warn("Skipping bulk item '{}' - no assignee email provided", item.getTitle());
                    continue;
                }

                Priority priority = Priority.MEDIUM;
                if (item.getPriority() != null) {
                    try {
                        priority = Priority.valueOf(item.getPriority().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid priority '{}' for task '{}', defaulting to MEDIUM",
                                item.getPriority(), item.getTitle());
                    }
                }

                LocalDate dueDate = null;
                if (item.getDueDate() != null && !item.getDueDate().isBlank()) {
                    try {
                        dueDate = LocalDate.parse(item.getDueDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (Exception e) {
                        log.warn("Could not parse due date '{}' for task '{}', ignoring",
                                item.getDueDate(), item.getTitle());
                    }
                }

                TaskCreateRequest createReq = TaskCreateRequest.builder()
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .assigneeEmail(item.getAssigneeEmail())
                        .assigneeName(item.getAssignee())
                        .dueDate(dueDate)
                        .priority(priority)
                        .source(item.getSource())
                        .sourceUrl(item.getSourceUrl())
                        .build();

                createRequests.add(createReq);
            }
        }

        List<TaskDTO> created = taskService.bulkCreateTasks(createRequests, currentUser.email(), currentUser.name());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam(required = false) String assigneeEmail,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) String projectTag,
            @RequestParam(required = false) Boolean recurringOnly,
            @AuthenticationPrincipal OidcUser principal) {

        AppUserDetails currentUser = getCurrentUser(principal);

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .assigneeEmail(assigneeEmail)
                .status(status)
                .priority(priority)
                .dueDateFrom(dueDateFrom)
                .dueDateTo(dueDateTo)
                .projectTag(projectTag)
                .recurringOnly(recurringOnly)
                .build();

        byte[] csvBytes = taskService.exportToCsv(filter, currentUser.email(), currentUser.role());

        String filename = "action-tracker-export-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<List<ActivityDTO>> getTaskActivity(@PathVariable UUID id,
                                                              @AuthenticationPrincipal OidcUser principal) {
        List<ActivityDTO> activity = taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(id)
                .stream()
                .map(ActivityDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard(@AuthenticationPrincipal OidcUser principal) {
        AppUserDetails currentUser = getCurrentUser(principal);
        DashboardDTO dashboard = taskService.getDashboard(currentUser.email(), currentUser.role());
        return ResponseEntity.ok(dashboard);
    }

    // ---- Helper ----

    private AppUserDetails getCurrentUser(OidcUser principal) {
        String email = principal.getEmail();
        String name = principal.getFullName() != null ? principal.getFullName() : email;

        UserRole role = appUserRepository.findByEmail(email)
                .map(AppUser::getRole)
                .orElse(UserRole.MEMBER);

        return new AppUserDetails(email, name, role);
    }

    record AppUserDetails(String email, String name, UserRole role) {}
}
