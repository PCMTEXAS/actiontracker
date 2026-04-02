package com.pcmtexas.actiontracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "author_email", nullable = false, length = 255)
    private String authorEmail;

    @Column(name = "author_name", nullable = false, length = 255)
    private String authorName;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "comment_mentions",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @Column(name = "mentioned_email")
    @Builder.Default
    private List<String> mentions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
