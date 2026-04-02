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
public class DashboardDTO {

    private List<TaskDTO> myOpenTasks;
    private long overdueCount;
    private List<TaskDTO> overdueTasks;
    private List<TaskDTO> waitingOnOthers;
    private List<TaskDTO> assignedByMe;
    private List<ActivityDTO> recentActivity;
}
