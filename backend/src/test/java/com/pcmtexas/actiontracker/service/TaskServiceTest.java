package com.pcmtexas.actiontracker.service;

import com.pcmtexas.actiontracker.dto.BulkCreateRequest.ExtractedTaskItem;
import com.pcmtexas.actiontracker.dto.TaskCreateRequest;
import com.pcmtexas.actiontracker.dto.TaskDTO;
import com.pcmtexas.actiontracker.dto.TaskFilterRequest;
import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.enums.UserRole;
import com.pcmtexas.actiontracker.repository.AppUserRepository;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.repository.TaskCommentRepository;
import com.pcmtexas.actiontracker.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskActivityRepository taskActivityRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ClaudeService claudeService;

    @Mock
    private GmailNotificationService gmailNotificationService;

    @InjectMocks
    private TaskService taskService;

    // ---- Helpers ----

    private Task buildTask() {
        return Task.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .description("Test Description")
                .assigneeEmail("assignee@digitalchalk.com")
                .assigneeName("Assignee Name")
                .assignedByEmail("owner@digitalchalk.com")
                .assignedByName("Owner Name")
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .status(Status.NOT_STARTED)
                .source("Test Source")
                .sourceUrl("https://example.com")
                .projectTag("PROJECT-A")
                .isRecurring(false)
                .recurrence(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private TaskCreateRequest buildCreateRequest() {
        return TaskCreateRequest.builder()
                .title("New Task")
                .description("New Description")
                .assigneeEmail("assignee@digitalchalk.com")
                .assigneeName("Assignee Name")
                .dueDate(LocalDate.now().plusDays(5))
                .priority(Priority.HIGH)
                .source("Email")
                .sourceUrl("https://example.com/email")
                .projectTag("PROJECT-B")
                .isRecurring(false)
                .recurrence(null)
                .build();
    }

    // ---- Tests ----

    @Test
    void getTasks_asOwner_returnsAllMatchingTasks() {
        Task task1 = buildTask();
        Task task2 = buildTask();
        task2.setTitle("Second Task");

        when(taskRepository.findByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(false)
        )).thenReturn(List.of(task1, task2));

        TaskFilterRequest filter = TaskFilterRequest.builder().build();
        List<TaskDTO> result = taskService.getTasks(filter, "owner@digitalchalk.com", UserRole.OWNER);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        assertThat(result.get(1).getTitle()).isEqualTo("Second Task");
    }

    @Test
    void getTasks_asMember_returnsOnlyMemberTasks() {
        Task task = buildTask();

        when(taskRepository.findByFiltersForMember(
                eq("member@digitalchalk.com"),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(false)
        )).thenReturn(List.of(task));

        TaskFilterRequest filter = TaskFilterRequest.builder().build();
        List<TaskDTO> result = taskService.getTasks(filter, "member@digitalchalk.com", UserRole.MEMBER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssigneeEmail()).isEqualTo("assignee@digitalchalk.com");
    }

    @Test
    void getTaskById_existingId_returnsTaskDTO() {
        Task task = buildTask();
        UUID id = task.getId();

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskDTO result = taskService.getTaskById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getAssigneeEmail()).isEqualTo("assignee@digitalchalk.com");
        assertThat(result.getStatus()).isEqualTo(Status.NOT_STARTED);
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void getTaskById_nonExistingId_throwsEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void createTask_validRequest_savesAndReturnsDTO() throws Exception {
        TaskCreateRequest req = buildCreateRequest();
        Task savedTask = Task.builder()
                .id(UUID.randomUUID())
                .title(req.getTitle())
                .description(req.getDescription())
                .assigneeEmail(req.getAssigneeEmail())
                .assigneeName(req.getAssigneeName())
                .assignedByEmail("owner@digitalchalk.com")
                .assignedByName("Owner Name")
                .dueDate(req.getDueDate())
                .priority(req.getPriority())
                .status(Status.NOT_STARTED)
                .isRecurring(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskActivityRepository.save(any())).thenReturn(null);
        doNothing().when(gmailNotificationService).sendTaskAssignedEmail(any(), anyString());

        TaskDTO result = taskService.createTask(req, "owner@digitalchalk.com", "Owner Name");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Task");
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskActivityRepository, times(1)).save(any());
    }

    @Test
    void deleteTask_existingId_deletesTask() {
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(true);

        taskService.deleteTask(id);

        verify(taskRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteTask_nonExistingId_throwsEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(id))
                .isInstanceOf(EntityNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void extractTasksFromNotes_callsClaudeService() {
        ExtractedTaskItem item = new ExtractedTaskItem();
        item.setTitle("Extracted Task");
        item.setDescription("Extracted Description");
        item.setAssignee("John Smith");

        when(claudeService.extractTasks("Some meeting notes")).thenReturn(List.of(item));

        List<ExtractedTaskItem> result = taskService.extractTasksFromNotes("Some meeting notes");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Extracted Task");
        verify(claudeService, times(1)).extractTasks("Some meeting notes");
    }
}
