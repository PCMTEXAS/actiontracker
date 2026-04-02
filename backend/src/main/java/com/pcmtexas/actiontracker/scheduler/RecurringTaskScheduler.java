package com.pcmtexas.actiontracker.scheduler;

import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskActivity;
import com.pcmtexas.actiontracker.enums.ActivityEventType;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.repository.TaskRepository;
import com.pcmtexas.actiontracker.service.GmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTaskScheduler {

    private final TaskRepository taskRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final AppUserRepository appUserRepository;
    private final GmailNotificationService gmailNotificationService;

    @Value("${app.owner-email:owner@digitalchalk.com}")
    private String ownerEmail;

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
                shouldReset = completedAt.isBefore(now.minusDays(7));
            } else if (task.getRecurrence() == Recurrence.MONTHLY) {
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
     * Runs every morning at 07:00 AM server time.
     * Sends due-tomorrow reminder emails for incomplete tasks due the following day.
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional(readOnly = true)
    public void sendDueTomorrowReminders() {
        log.info("RecurringTaskScheduler: checking for tasks due tomorrow");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Task> tasksDueTomorrow = taskRepository.findTasksDueTomorrow(tomorrow);

        log.info("RecurringTaskScheduler: found {} tasks due tomorrow ({})",
                tasksDueTomorrow.size(), tomorrow);

        for (Task task : tasksDueTomorrow) {
            try {
                gmailNotificationService.sendTaskDueTomorrowEmail(task);
            } catch (Exception e) {
                log.warn("Failed to send due-tomorrow reminder for task {} to {}: {}",
                        task.getId(), task.getAssigneeEmail(), e.getMessage());
            }
        }
    }

    /**
     * Runs every morning at 07:15 AM server time.
     * Sends overdue task alerts to the OWNER for all overdue incomplete tasks.
     */
    @Scheduled(cron = "0 15 7 * * *")
    @Transactional(readOnly = true)
    public void sendOverdueAlerts() {
        log.info("RecurringTaskScheduler: checking for overdue tasks");

        LocalDate today = LocalDate.now();
        List<Task> overdueTasks = taskRepository.findByDueDateBefore(today)
                .stream()
                .filter(t -> t.getStatus() != Status.COMPLETE)
                .collect(Collectors.toList());

        if (overdueTasks.isEmpty()) {
            log.info("RecurringTaskScheduler: no overdue tasks found");
            return;
        }

        log.info("RecurringTaskScheduler: found {} overdue tasks", overdueTasks.size());

        for (Task task : overdueTasks) {
            try {
                // Notify the assignee
                gmailNotificationService.sendOverdueEmail(task, task.getAssigneeEmail());
                // Notify the owner/assigner if different from assignee
                if (!task.getAssignedByEmail().equalsIgnoreCase(task.getAssigneeEmail())) {
                    gmailNotificationService.sendOverdueEmail(task, task.getAssignedByEmail());
                }
            } catch (Exception e) {
                log.warn("Failed to send overdue alert for task {}: {}", task.getId(), e.getMessage());
            }
        }
    }

    /**
     * Runs every morning at 07:30 AM server time.
     * Sends a personalized daily digest to every user who has daily_digest_enabled = true.
     */
    @Scheduled(cron = "0 30 7 * * *")
    @Transactional(readOnly = true)
    public void sendDailyDigest() {
        log.info("RecurringTaskScheduler: starting daily digest run");

        List<AppUser> digestUsers = appUserRepository.findByDailyDigestEnabledTrue();
        log.info("RecurringTaskScheduler: sending digest to {} user(s)", digestUsers.size());

        LocalDate today = LocalDate.now();

        for (AppUser user : digestUsers) {
            try {
                // Open tasks assigned to this user
                List<Task> myOpenTasks = taskRepository.findByAssigneeEmail(user.getEmail())
                        .stream()
                        .filter(t -> t.getStatus() != Status.COMPLETE)
                        .collect(Collectors.toList());

                // Overdue tasks that this user assigned to others
                List<Task> delegatedOverdue = taskRepository.findByAssignedByEmail(user.getEmail())
                        .stream()
                        .filter(t -> t.getStatus() != Status.COMPLETE
                                && !t.getAssigneeEmail().equalsIgnoreCase(user.getEmail())
                                && t.getDueDate() != null
                                && t.getDueDate().isBefore(today))
                        .collect(Collectors.toList());

                gmailNotificationService.sendDailyDigestEmail(user.getEmail(), myOpenTasks, delegatedOverdue);
            } catch (Exception e) {
                log.warn("Failed to send daily digest to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("RecurringTaskScheduler: daily digest run complete");
    }
}
