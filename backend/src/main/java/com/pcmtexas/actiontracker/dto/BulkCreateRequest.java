package com.pcmtexas.actiontracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateRequest {

    private List<ExtractedTaskItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedTaskItem {
        private String title;
        private String description;
        private String assignee;
        private String assigneeEmail;
        private String dueDate;
        private String priority;
        private String source;
        private String sourceUrl;
    }
}
