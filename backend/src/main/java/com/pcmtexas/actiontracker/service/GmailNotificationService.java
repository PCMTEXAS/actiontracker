package com.pcmtexas.actiontracker.service;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskComment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class GmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.gmail.from}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public GmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTaskAssignedEmail(Task task, String assignedByName) {
        String subject = "[Action Tracker] New task assigned to you: " + task.getTitle();
        String body = buildEmailBody(task,
                "You have been assigned a new task by " + assignedByName,
                assignedByName);
        sendEmail(task.getAssigneeEmail(), subject, body);
    }

    public void sendTaskDueTomorrowEmail(Task task) {
        String subject = "[Action Tracker] Task due tomorrow: " + task.getTitle();
        String body = buildEmailBody(task,
                "This task is due tomorrow",
                "Action Tracker System");
        sendEmail(task.getAssigneeEmail(), subject, body);
    }

    public void sendOverdueEmail(Task task, String ownerEmail) {
        String subject = "[Action Tracker] OVERDUE: " + task.getTitle();
        String body = buildEmailBody(task,
                "This task is overdue and has not been completed",
                "Action Tracker System");
        sendEmail(ownerEmail, subject, body);
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
        sendEmail(mentionedEmail, subject, body);
    }

    public void sendTaskCompletedEmail(Task task) {
        String subject = "[Action Tracker] Task completed: " + task.getTitle();
        String body = buildEmailBody(task,
                "This task has been marked as complete",
                task.getAssigneeName());
        // Notify the person who assigned the task
        sendEmail(task.getAssignedByEmail(), subject, body);
    }

    public void sendDailyDigestEmail(String recipientEmail,
                                      List<Task> myTasks,
                                      List<Task> delegatedOverdueTasks) {
        String subject = "[Action Tracker] Daily Digest - " + java.time.LocalDate.now().format(DATE_FMT);

        StringBuilder body = new StringBuilder();
        body.append("Good morning,\n\n");
        body.append("Here is your daily action tracker digest:\n\n");

        body.append("=== YOUR OPEN TASKS (").append(myTasks.size()).append(") ===\n");
        if (myTasks.isEmpty()) {
            body.append("  No open tasks — great work!\n");
        } else {
            for (Task task : myTasks) {
                String dueDateStr = task.getDueDate() != null ? task.getDueDate().format(DATE_FMT) : "No due date";
                body.append(String.format("  [%s] %s - Due: %s - Priority: %s\n",
                        task.getStatus(), task.getTitle(), dueDateStr, task.getPriority()));
            }
        }

        if (!delegatedOverdueTasks.isEmpty()) {
            body.append("\n=== OVERDUE TASKS YOU ASSIGNED (").append(delegatedOverdueTasks.size()).append(") ===\n");
            for (Task task : delegatedOverdueTasks) {
                String dueDateStr = task.getDueDate() != null ? task.getDueDate().format(DATE_FMT) : "No due date";
                body.append(String.format("  [%s] %s - Assigned to: %s - Due: %s\n",
                        task.getStatus(), task.getTitle(), task.getAssigneeName(), dueDateStr));
            }
        }

        body.append("\nView all tasks: ").append(frontendUrl).append("/tasks\n");
        body.append("\nTo opt out of daily digest emails, visit: ").append(frontendUrl).append("/settings\n");
        body.append("\n--- PCM Texas Action Tracker ---\n");

        sendEmail(recipientEmail, subject, body.toString());
    }

    // ---- Private helpers ----

    @Async
    protected void sendEmail(String to, String subject, String textBody) {
        log.info("[EMAIL] to={} | subject={}", to, subject);

        if (!emailEnabled) {
            log.info("[EMAIL DISABLED] Would have sent to={} subject={}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, false);
            mailSender.send(message);
            log.info("[EMAIL SENT] to={} | subject={}", to, subject);
        } catch (MessagingException | MailException e) {
            log.error("[EMAIL FAILED] to={} | subject={} | error={}", to, subject, e.getMessage());
        }
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
}
