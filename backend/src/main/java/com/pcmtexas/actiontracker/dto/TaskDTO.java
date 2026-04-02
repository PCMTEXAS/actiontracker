package com.pcmtexas.actiontracker.dto;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private UUID id;
    private String title;
    private String description;
    private String assigneeEmail;
    private String assigneeName;
    private String assignedByEmail;
    private String assignedByName;
    private LocalDate dueDate;
    private Priority priority;
    private Status status;
    private String source;
    private String sourceUrl;
    private String projectTag;
    private boolean isRecurring;
    private Recurrence recurrence;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static TaskDTO fromEntity(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .assigneeEmail(task.getAssigneeEmail())
                .assigneeName(task.getAssigneeName())
                .assignedByEmail(task.getAssignedByEmail())
                .assignedByName(task.getAssignedByName())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .source(task.getSource())
                .sourceUrl(task.getSourceUrl())
                .projectTag(task.getProjectTag())
                .isRecurring(task.isRecurring())
                .recurrence(task.getRecurrence())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
