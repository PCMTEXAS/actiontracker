package com.pcmtexas.actiontracker.service;

import com.opencsv.CSVWriter;
import com.pcmtexas.actiontracker.dto.*;
import com.pcmtexas.actiontracker.dto.BulkCreateRequest.ExtractedTaskItem;
import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskActivity;
import com.pcmtexas.actiontracker.enums.ActivityEventType;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.repository.TaskCommentRepository;
import com.pcmtexas.actiontracker.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final AppUserRepository appUserRepository;
    private final ClaudeService claudeService;
    private final GmailNotificationService gmailNotificationService;

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasks(TaskFilterRequest filter, String currentUserEmail, UserRole role) {
        boolean recurringOnly = filter.getRecurringOnly() != null && filter.getRecurringOnly();

        List<Task> tasks;
        if (role == UserRole.OWNER) {
            tasks = taskRepository.findByFilters(
                    filter.getAssigneeEmail(),
                    filter.getStatus(),
                    filter.getPriority(),
                    filter.getDueDateFrom(),
                    filter.getDueDateTo(),
                    filter.getProjectTag(),
                    recurringOnly
            );
        } else {
            tasks = taskRepository.findByFiltersForMember(
                    currentUserEmail,
                    filter.getAssigneeEmail(),
                    filter.getStatus(),
                    filter.getPriority(),
                    filter.getDueDateFrom(),
                    filter.getDueDateTo(),
                    filter.getProjectTag(),
                    recurringOnly
            );
        }

        return tasks.stream().map(TaskDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return TaskDTO.fromEntity(task);
    }

    @Transactional
    public TaskDTO createTask(TaskCreateRequest req, String assignedByEmail, String assignedByName) {
        // Resolve assignee name if not provided
        String assigneeName = req.getAssigneeName();
        if (assigneeName == null || assigneeName.isBlank()) {
            assigneeName = appUserRepository.findByEmail(req.getAssigneeEmail())
                    .map(AppUser::getName)
                    .orElse(req.getAssigneeEmail());
        }

        Priority priority = req.getPriority() != null ? req.getPriority() : Priority.MEDIUM;

        Task task = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .assigneeEmail(req.getAssigneeEmail())
                .assigneeName(assigneeName)
                .assignedByEmail(assignedByEmail)
                .assignedByName(assignedByName)
                .dueDate(req.getDueDate())
                .priority(priority)
                .status(Status.NOT_STARTED)
                .source(req.getSource())
                .sourceUrl(req.getSourceUrl())
                .projectTag(req.getProjectTag())
                .isRecurring(req.isRecurring())
                .recurrence(req.getRecurrence())
                .build();

        Task savedTask = taskRepository.save(task);

        logActivity(savedTask.getId(), assignedByEmail, assignedByName,
                ActivityEventType.TASK_CREATED,
                "Task created: \"" + savedTask.getTitle() + "\" assigned to " + assigneeName);

        try {
            gmailNotificationService.sendTaskAssignedEmail(savedTask, assignedByName);
        } catch (Exception e) {
            log.warn("Failed to send task assignment email for task {}: {}", savedTask.getId(), e.getMessage());
        }

        return TaskDTO.fromEntity(savedTask);
    }

    @Transactional
    public TaskDTO updateTask(UUID id, TaskUpdateRequest req, String actorEmail, String actorName) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        // Track changes for activity log
        boolean statusChanged = req.getStatus() != null && req.getStatus() != task.getStatus();
        boolean assigneeChanged = req.getAssigneeEmail() != null
                && !req.getAssigneeEmail().equalsIgnoreCase(task.getAssigneeEmail());
        boolean dueDateChanged = req.getDueDate() != null
                && !req.getDueDate().equals(task.getDueDate());
        boolean completedNow = statusChanged && req.getStatus() == Status.COMPLETE;

        // Apply updates
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            task.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getAssigneeEmail() != null) {
            String newAssigneeName = req.getAssigneeName();
            if (newAssigneeName == null || newAssigneeName.isBlank()) {
                newAssigneeName = appUserRepository.findByEmail(req.getAssigneeEmail())
                        .map(AppUser::getName)
                        .orElse(req.getAssigneeEmail());
            }
            task.setAssigneeEmail(req.getAssigneeEmail());
            task.setAssigneeName(newAssigneeName);
        }
        if (req.getDueDate() != null) {
            task.setDueDate(req.getDueDate());
        }
        if (req.getPriority() != null) {
            task.setPriority(req.getPriority());
        }
        if (req.getStatus() != null) {
            task.setStatus(req.getStatus());
        }
        if (req.getSource() != null) {
            task.setSource(req.getSource());
        }
        if (req.getSourceUrl() != null) {
            task.setSourceUrl(req.getSourceUrl());
        }
        if (req.getProjectTag() != null) {
            task.setProjectTag(req.getProjectTag());
        }
        if (req.getIsRecurring() != null) {
            task.setRecurring(req.getIsRecurring());
        }
        if (req.getRecurrence() != null) {
            task.setRecurrence(req.getRecurrence());
        }

        Task savedTask = taskRepository.save(task);

        // Log activities for each detected change
        if (statusChanged) {
            String detail = "Status changed to " + req.getStatus().name();
            ActivityEventType eventType = completedNow
                    ? ActivityEventType.TASK_COMPLETED
                    : ActivityEventType.STATUS_CHANGED;
            logActivity(savedTask.getId(), actorEmail, actorName, eventType, detail);
        }

        if (assigneeChanged) {
            logActivity(savedTask.getId(), actorEmail, actorName,
                    ActivityEventType.REASSIGNED,
                    "Reassigned to " + savedTask.getAssigneeName() + " (" + savedTask.getAssigneeEmail() + ")");
            try {
                gmailNotificationService.sendTaskAssignedEmail(savedTask, actorName);
            } catch (Exception e) {
                log.warn("Failed to send reassignment email for task {}: {}", savedTask.getId(), e.getMessage());
            }
        }

        if (dueDateChanged) {
            String newDate = savedTask.getDueDate() != null
                    ? savedTask.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : "none";
            logActivity(savedTask.getId(), actorEmail, actorName,
                    ActivityEventType.DUE_DATE_UPDATED,
                    "Due date updated to " + newDate);
        }

        if (completedNow) {
            try {
                gmailNotificationService.sendTaskCompletedEmail(savedTask);
            } catch (Exception e) {
                log.warn("Failed to send task completed email for task {}: {}", savedTask.getId(), e.getMessage());
            }
        }

        return TaskDTO.fromEntity(savedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        log.info("Task {} deleted", id);
    }

    public List<ExtractedTaskItem> extractTasksFromNotes(String notes) {
        return claudeService.extractTasks(notes);
    }

    @Transactional
    public List<TaskDTO> bulkCreateTasks(List<TaskCreateRequest> tasks,
                                          String assignedByEmail,
                                          String assignedByName) {
        List<TaskDTO> created = new ArrayList<>();
        for (TaskCreateRequest req : tasks) {
            try {
                TaskDTO dto = createTask(req, assignedByEmail, assignedByName);
                created.add(dto);
            } catch (Exception e) {
                log.error("Failed to create task '{}' during bulk create: {}", req.getTitle(), e.getMessage());
            }
        }
        log.info("Bulk created {} of {} tasks", created.size(), tasks.size());
        return created;
    }

    @Transactional(readOnly = true)
    public byte[] exportToCsv(TaskFilterRequest filter, String currentUserEmail, UserRole role) {
        List<TaskDTO> tasks = getTasks(filter, currentUserEmail, role);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(osw)) {

            // Write BOM for Excel compatibility
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // Header row
            String[] header = {
                    "ID", "Title", "Description", "Assignee Email", "Assignee Name",
                    "Assigned By Email", "Assigned By Name", "Due Date", "Priority",
                    "Status", "Source", "Source URL", "Project Tag", "Recurring",
                    "Recurrence", "Created At", "Updated At"
            };
            csvWriter.writeNext(header);

            // Data rows
            for (TaskDTO task : tasks) {
                String[] row = {
                        nullToEmpty(task.getId()),
                        nullToEmpty(task.getTitle()),
                        nullToEmpty(task.getDescription()),
                        nullToEmpty(task.getAssigneeEmail()),
                        nullToEmpty(task.getAssigneeName()),
                        nullToEmpty(task.getAssignedByEmail()),
                        nullToEmpty(task.getAssignedByName()),
                        task.getDueDate() != null ? task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                        task.getPriority() != null ? task.getPriority().name() : "",
                        task.getStatus() != null ? task.getStatus().name() : "",
                        nullToEmpty(task.getSource()),
                        nullToEmpty(task.getSourceUrl()),
                        nullToEmpty(task.getProjectTag()),
                        String.valueOf(task.isRecurring()),
                        task.getRecurrence() != null ? task.getRecurrence().name() : "",
                        task.getCreatedAt() != null ? task.getCreatedAt().toString() : "",
                        task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate CSV export", e);
            throw new RuntimeException("CSV export failed: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public DashboardDTO getDashboard(String currentUserEmail, UserRole role) {
        LocalDate today = LocalDate.now();

        // My open tasks (assigned to me, not complete)
        List<Task> allMyTasks = taskRepository.findByAssigneeEmail(currentUserEmail);
        List<TaskDTO> myOpenTasks = allMyTasks.stream()
                .filter(t -> t.getStatus() != Status.COMPLETE)
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        // Overdue tasks assigned to me
        List<Task> overdueTaskEntities = taskRepository.findOverdueByAssignee(currentUserEmail, today);
        List<TaskDTO> overdueTasks = overdueTaskEntities.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
        long overdueCount = overdueTasks.size();

        // Tasks I'm waiting on others for (assigned by me, not complete)
        List<TaskDTO> waitingOnOthers = taskRepository.findByAssignedByEmail(currentUserEmail)
                .stream()
                .filter(t -> t.getStatus() != Status.COMPLETE
                        && !t.getAssigneeEmail().equalsIgnoreCase(currentUserEmail))
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        // Tasks assigned by me (all, for OWNER view)
        List<TaskDTO> assignedByMe = taskRepository.findByAssignedByEmail(currentUserEmail)
                .stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        // Recent activity
        List<UUID> myTaskIds = allMyTasks.stream().map(Task::getId).collect(Collectors.toList());
        List<ActivityDTO> recentActivity;
        if (myTaskIds.isEmpty()) {
            recentActivity = new ArrayList<>();
        } else {
            recentActivity = taskActivityRepository
                    .findTop10ByActorEmailOrTaskIdInOrderByCreatedAtDesc(currentUserEmail, myTaskIds)
                    .stream()
                    .map(ActivityDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        return DashboardDTO.builder()
                .myOpenTasks(myOpenTasks)
                .overdueCount(overdueCount)
                .overdueTasks(overdueTasks)
                .waitingOnOthers(waitingOnOthers)
                .assignedByMe(assignedByMe)
                .recentActivity(recentActivity)
                .build();
    }

    private void logActivity(UUID taskId, String actorEmail, String actorName,
                              ActivityEventType eventType, String detail) {
        TaskActivity activity = TaskActivity.builder()
                .taskId(taskId)
                .actorEmail(actorEmail)
                .actorName(actorName)
                .eventType(eventType)
                .detail(detail)
                .build();
        taskActivityRepository.save(activity);
    }

    private String nullToEmpty(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
