package com.pcmtexas.actiontracker.scheduler;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskActivity;
import com.pcmtexas.actiontracker.enums.ActivityEventType;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTaskScheduler {

    private final TaskRepository taskRepository;
    private final TaskActivityRepository taskActivityRepository;

    /**
     * Runs nightly at 02:00 AM server time.
     * Finds completed recurring tasks and resets them based on their recurrence interval.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void resetRecurringTasks() {
        log.info("RecurringTaskScheduler: starting nightly recurring task reset");

        List<Task> completedRecurring = taskRepository.findCompletedRecurringTasks();
        log.info("RecurringTaskScheduler: found {} completed recurring tasks to evaluate", completedRecurring.size());

        OffsetDateTime now = OffsetDateTime.now();
        int resetCount = 0;

        for (Task task : completedRecurring) {
            if (task.getRecurrence() == null) {
                log.debug("Task {} is marked recurring but has no recurrence interval set, skipping", task.getId());
                continue;
            }

            OffsetDateTime completedAt = task.getUpdatedAt();
            if (completedAt == null) {
                completedAt = task.getCreatedAt();
            }

            boolean shouldReset = false;
            if (task.getRecurrence() == Recurrence.WEEKLY) {
                // Reset if completed more than 7 days ago
                shouldReset = completedAt.isBefore(now.minusDays(7));
            } else if (task.getRecurrence() == Recurrence.MONTHLY) {
                // Reset if completed more than 30 days ago
                shouldReset = completedAt.isBefore(now.minusDays(30));
            }

            if (shouldReset) {
                log.info("Resetting recurring task {} ({}) - last completed at {}",
                        task.getId(), task.getTitle(), completedAt);

                task.setStatus(Status.NOT_STARTED);
                taskRepository.save(task);

                TaskActivity activity = TaskActivity.builder()
                        .taskId(task.getId())
                        .actorEmail("system@actiontracker")
                        .actorName("Action Tracker System")
                        .eventType(ActivityEventType.STATUS_CHANGED)
                        .detail("Recurring task automatically reset to NOT_STARTED ("
                                + task.getRecurrence().name() + " recurrence)")
                        .build();
                taskActivityRepository.save(activity);

                resetCount++;
            }
        }

        log.info("RecurringTaskScheduler: reset {} recurring task(s) to NOT_STARTED", resetCount);
    }

    /**
     * Runs nightly at 07:00 AM server time.
     * Sends due-tomorrow reminders for tasks due the following day.
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional(readOnly = true)
    public void sendDueTomorrowReminders() {
        log.info("RecurringTaskScheduler: checking for tasks due tomorrow");

        java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
        List<Task> tasksDueTomorrow = taskRepository.findTasksDueTomorrow(tomorrow);

        log.info("RecurringTaskScheduler: found {} tasks due tomorrow ({})",
                tasksDueTomorrow.size(), tomorrow);

        // NOTE: Actual email sending is handled by GmailNotificationService when injected.
        // Log here so the scheduler confirms operation without circular dependency issues.
        for (Task task : tasksDueTomorrow) {
            log.info("Due-tomorrow reminder needed for task {} assigned to {}",
                    task.getId(), task.getAssigneeEmail());
        }
    }
}
