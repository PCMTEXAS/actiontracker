package com.pcmtexas.actiontracker.dto;

import com.pcmtexas.actiontracker.enums.Priority;
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
public class TaskFilterRequest {

    private String assigneeEmail;
    private Status status;
    private Priority priority;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private String projectTag;
    private Boolean recurringOnly;
}
