package com.pcmtexas.actiontracker.entity;

import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assignee_email", nullable = false, length = 255)
    private String assigneeEmail;

    @Column(name = "assignee_name", nullable = false, length = 255)
    private String assigneeName;

    @Column(name = "assigned_by_email", nullable = false, length = 255)
    private String assignedByEmail;

    @Column(name = "assigned_by_name", nullable = false, length = 255)
    private String assignedByName;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.NOT_STARTED;

    @Column(name = "source", length = 500)
    private String source;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "project_tag", length = 255)
    private String projectTag;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", length = 20)
    private Recurrence recurrence;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
