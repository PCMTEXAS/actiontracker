package com.pcmtexas.actiontracker.controller;

import com.pcmtexas.actiontracker.dto.CommentDTO;
import com.pcmtexas.actiontracker.entity.AppUser;
import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskActivity;
import com.pcmtexas.actiontracker.entity.TaskComment;
import com.pcmtexas.actiontracker.enums.ActivityEventType;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.repository.TaskCommentRepository;
import com.pcmtexas.actiontracker.repository.TaskRepository;
import com.pcmtexas.actiontracker.service.GmailNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Slf4j
public class TaskCommentController {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w.+-]+)");

    private final TaskCommentRepository taskCommentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;
    private final GmailNotificationService gmailNotificationService;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> listComments(@PathVariable UUID taskId,
                                                          @AuthenticationPrincipal OidcUser principal) {
        // Verify task exists
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task not found with id: " + taskId);
        }

        List<CommentDTO> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable UUID taskId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal OidcUser principal) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        String authorEmail = principal.getEmail();
        String authorName = principal.getFullName() != null ? principal.getFullName() : authorEmail;

        // Extract @mention handles from the comment body
        List<String> mentionedEmails = extractMentions(request.getBody(), authorEmail);

        TaskComment comment = TaskComment.builder()
                .task(task)
                .authorEmail(authorEmail)
                .authorName(authorName)
                .body(request.getBody())
                .mentions(mentionedEmails)
                .build();

        TaskComment savedComment = taskCommentRepository.save(comment);

        // Log activity
        TaskActivity activity = TaskActivity.builder()
                .taskId(taskId)
                .actorEmail(authorEmail)
                .actorName(authorName)
                .eventType(ActivityEventType.COMMENT_ADDED)
                .detail("Comment added by " + authorName)
                .build();
        taskActivityRepository.save(activity);

        // Send mention notifications
        for (String mentionedEmail : mentionedEmails) {
            try {
                String mentionedName = appUserRepository.findByEmail(mentionedEmail)
                        .map(AppUser::getName)
                        .orElse(mentionedEmail);
                gmailNotificationService.sendMentionNotification(savedComment, mentionedEmail, mentionedName, task);
            } catch (Exception e) {
                log.warn("Failed to send mention notification to {}: {}", mentionedEmail, e.getMessage());
            }
        }

        return ResponseEntity.status(201).body(CommentDTO.fromEntity(savedComment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal OidcUser principal) {

        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        // Verify comment belongs to the specified task
        if (!comment.getTask().getId().equals(taskId)) {
            throw new EntityNotFoundException("Comment does not belong to task " + taskId);
        }

        String currentEmail = principal.getEmail();

        // Only author or OWNER can delete
        UserRole role = appUserRepository.findByEmail(currentEmail)
                .map(AppUser::getRole)
                .orElse(UserRole.MEMBER);

        boolean isAuthor = comment.getAuthorEmail().equalsIgnoreCase(currentEmail);
        boolean isOwner = role == UserRole.OWNER;

        if (!isAuthor && !isOwner) {
            throw new AccessDeniedException("You do not have permission to delete this comment");
        }

        taskCommentRepository.delete(comment);
        log.info("Comment {} deleted by {}", commentId, currentEmail);

        return ResponseEntity.noContent().build();
    }

    /**
     * Parses @mentions from comment body. Resolves handles against the app_users table.
     * If an exact email is mentioned (@user@domain.com style), it is used directly.
     * Otherwise partial username matches are attempted.
     */
    private List<String> extractMentions(String body, String authorEmail) {
        List<String> mentionedEmails = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return mentionedEmails;
        }

        Matcher matcher = MENTION_PATTERN.matcher(body);
        while (matcher.find()) {
            String handle = matcher.group(1);

            // Direct email mention: @user@domain.com captured as two tokens by the simple regex;
            // handle the case where the mention is a full email embedded differently.
            // Try to find user by partial email/name match
            String resolvedEmail = resolveHandle(handle, authorEmail);
            if (resolvedEmail != null && !mentionedEmails.contains(resolvedEmail)) {
                mentionedEmails.add(resolvedEmail);
            }
        }

        return mentionedEmails;
    }

    private String resolveHandle(String handle, String excludeEmail) {
        // First try exact email match (handle contains @)
        if (handle.contains("@")) {
            return handle.equalsIgnoreCase(excludeEmail) ? null : handle;
        }

        // Try to find user whose email starts with handle (e.g., @jsmith -> jsmith@domain.com)
        return appUserRepository.findAll().stream()
                .filter(u -> !u.getEmail().equalsIgnoreCase(excludeEmail))
                .filter(u -> {
                    String localPart = u.getEmail().split("@")[0];
                    String namePart = u.getName().replace(" ", "").toLowerCase();
                    return localPart.equalsIgnoreCase(handle)
                            || namePart.equalsIgnoreCase(handle)
                            || u.getName().toLowerCase().contains(handle.toLowerCase());
                })
                .map(AppUser::getEmail)
                .findFirst()
                .orElse(null);
    }

    // ---- Inner DTO ----
    record CommentCreateRequest(String body) {}
}
