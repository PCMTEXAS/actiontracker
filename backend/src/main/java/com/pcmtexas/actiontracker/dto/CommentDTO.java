package com.pcmtexas.actiontracker.dto;

import com.pcmtexas.actiontracker.entity.TaskComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private UUID id;
    private UUID taskId;
    private String authorEmail;
    private String authorName;
    private String body;
    private List<String> mentions;
    private OffsetDateTime createdAt;

    public static CommentDTO fromEntity(TaskComment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .taskId(comment.getTask() != null ? comment.getTask().getId() : null)
                .authorEmail(comment.getAuthorEmail())
                .authorName(comment.getAuthorName())
                .body(comment.getBody())
                .mentions(comment.getMentions())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
