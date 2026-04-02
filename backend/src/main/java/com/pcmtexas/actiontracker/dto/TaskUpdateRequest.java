package com.pcmtexas.actiontracker.dto;

import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {

    private String title;
    private String description;
    private String assigneeEmail;
    private String assigneeName;
    private LocalDate dueDate;
    private Priority priority;
    private Status status;
    private String source;
    private String sourceUrl;
    private String projectTag;
    private Boolean isRecurring;
    private Recurrence recurrence;
}
