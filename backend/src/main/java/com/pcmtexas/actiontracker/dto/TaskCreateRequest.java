package com.pcmtexas.actiontracker.dto;

import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Assignee email is required")
    private String assigneeEmail;

    private String assigneeName;

    private LocalDate dueDate;

    private Priority priority;

    private String source;

    private String sourceUrl;

    private String projectTag;

    private boolean isRecurring;

    private Recurrence recurrence;
}
