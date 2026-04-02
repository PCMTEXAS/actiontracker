package com.pcmtexas.actiontracker.dto;

import com.pcmtexas.actiontracker.entity.TaskActivity;
import com.pcmtexas.actiontracker.enums.ActivityEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {

    private UUID id;
    private UUID taskId;
    private String actorEmail;
    private String actorName;
    private ActivityEventType eventType;
    private String detail;
    private OffsetDateTime createdAt;

    public static ActivityDTO fromEntity(TaskActivity activity) {
        return ActivityDTO.builder()
                .id(activity.getId())
                .taskId(activity.getTaskId())
                .actorEmail(activity.getActorEmail())
                .actorName(activity.getActorName())
                .eventType(activity.getEventType())
                .detail(activity.getDetail())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
