package com.pcmtexas.actiontracker.service;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class GmailNotificationService {

    @Value("${app.gmail.from}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public void sendTaskAssignedEmail(Task task, String assignedByName) {
        String subject = "[Action Tracker] New task assigned to you: " + task.getTitle();
        String body = buildEmailBody(task,
                "You have been assigned a new task by " + assignedByName,
                assignedByName);

        log.info("[EMAIL NOTIFICATION] sendTaskAssignedEmail | to={} | subject={} | body_snippet={}",
                task.getAssigneeEmail(), subject, truncate(body, 200));
    }

    public void sendTaskDueTomorrowEmail(Task task) {
        String subject = "[Action Tracker] Task due tomorrow: " + task.getTitle();
        String body = buildEmailBody(task,
                "This task is due tomorrow",
                "Action Tracker System");

        log.info("[EMAIL NOTIFICATION] sendTaskDueTomorrowEmail | to={} | subject={} | body_snippet={}",
                task.getAssigneeEmail(), subject, truncate(body, 200));
    }

    public void sendOverdueEmail(Task task, String ownerEmail) {
        String subject = "[Action Tracker] OVERDUE: " + task.getTitle();
        String body = buildEmailBody(task,
                "This task is overdue and has not been completed",
                "Action Tracker System");

        log.info("[EMAIL NOTIFICATION] sendOverdueEmail | to={} | subject={} | body_snippet={}",
                ownerEmail, subject, truncate(body, 200));
    }

    public void sendMentionNotification(TaskComment comment, String mentionedEmail,
                                         String mentionedName, Task task) {
        String subject = "[Action Tracker] You were mentioned in a comment on: " + task.getTitle();
        String body = String.format("""
                Hi %s,

                %s mentioned you in a comment on task "%s":

                ---
                %s
                ---

                View the task: %s/tasks/%s

                --- PCM Texas Action Tracker ---
                """,
                mentionedName,
                comment.getAuthorName(),
                task.getTitle(),
                comment.getBody(),
                frontendUrl,
                task.getId());

        log.info("[EMAIL NOTIFICATION] sendMentionNotification | to={} | subject={} | body_snippet={}",
                mentionedEmail, subject, truncate(body, 200));
    }

    public void sendTaskCompletedEmail(Task task) {
        String subject = "[Action Tracker] Task completed: " + task.getTitle();
        String body = buildEmailBody(task,
                "This task has been marked as complete",
                task.getAssigneeName());

        // Notify the person who assigned the task
        log.info("[EMAIL NOTIFICATION] sendTaskCompletedEmail | to={} | subject={} | body_snippet={}",
                task.getAssignedByEmail(), subject, truncate(body, 200));
    }

    public void sendDailyDigestEmail(String recipientEmail,
                                      List<Task> myTasks,
                                      List<Task> delegatedOverdueTasks) {
        String subject = "[Action Tracker] Daily Digest - " + java.time.LocalDate.now().format(DATE_FMT);

        StringBuilder body = new StringBuilder();
        body.append("Good morning,\n\n");
        body.append("Here is your daily action tracker digest:\n\n");

        body.append("=== YOUR OPEN TASKS (").append(myTasks.size()).append(") ===\n");
        for (Task task : myTasks) {
            String dueDateStr = task.getDueDate() != null ? task.getDueDate().format(DATE_FMT) : "No due date";
            body.append(String.format("  [%s] %s - Due: %s - Priority: %s\n",
                    task.getStatus(), task.getTitle(), dueDateStr, task.getPriority()));
        }

        if (!delegatedOverdueTasks.isEmpty()) {
            body.append("\n=== OVERDUE TASKS YOU ASSIGNED (").append(delegatedOverdueTasks.size()).append(") ===\n");
            for (Task task : delegatedOverdueTasks) {
                String dueDateStr = task.getDueDate() != null ? task.getDueDate().format(DATE_FMT) : "No due date";
                body.append(String.format("  [%s] %s - Assigned to: %s - Due: %s\n",
                        task.getStatus(), task.getTitle(), task.getAssigneeName(), dueDateStr));
            }
        }

        body.append("\nView all tasks: ").append(frontendUrl).append("\n");
        body.append("\n--- PCM Texas Action Tracker ---\n");

        log.info("[EMAIL NOTIFICATION] sendDailyDigestEmail | to={} | subject={} | myTasks={} | overdueAssigned={}",
                recipientEmail, subject, myTasks.size(), delegatedOverdueTasks.size());
    }

    private String buildEmailBody(Task task, String eventDescription, String triggeredBy) {
        String dueDateStr = task.getDueDate() != null
                ? task.getDueDate().format(DATE_FMT)
                : "No due date set";

        return String.format("""
                %s.

                Task Details:
                  Title:       %s
                  Priority:    %s
                  Status:      %s
                  Due Date:    %s
                  Assigned to: %s (%s)
                  Assigned by: %s (%s)
                  %s

                View task: %s/tasks/%s

                --- PCM Texas Action Tracker ---
                """,
                eventDescription,
                task.getTitle(),
                task.getPriority(),
                task.getStatus(),
                dueDateStr,
                task.getAssigneeName(), task.getAssigneeEmail(),
                task.getAssignedByName(), task.getAssignedByEmail(),
                task.getDescription() != null ? "Description: " + task.getDescription() : "",
                frontendUrl,
                task.getId());
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
