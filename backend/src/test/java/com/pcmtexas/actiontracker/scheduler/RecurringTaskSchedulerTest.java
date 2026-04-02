package com.pcmtexas.actiontracker.scheduler;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.entity.TaskActivity;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import com.pcmtexas.actiontracker.repository.TaskActivityRepository;
import com.pcmtexas.actiontracker.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecurringTaskSchedulerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskActivityRepository taskActivityRepository;

    @InjectMocks
    private RecurringTaskScheduler recurringTaskScheduler;

    private Task buildRecurringTask(Recurrence recurrence, OffsetDateTime updatedAt) {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Recurring Task")
                .description("A recurring task")
                .assigneeEmail("member@digitalchalk.com")
                .assigneeName("Member")
                .assignedByEmail("owner@digitalchalk.com")
                .assignedByName("Owner")
                .dueDate(LocalDate.now())
                .priority(Priority.MEDIUM)
                .status(Status.COMPLETE)
                .isRecurring(true)
                .recurrence(recurrence)
                .createdAt(OffsetDateTime.now().minusDays(30))
                .updatedAt(updatedAt)
                .build();
        return task;
    }

    @Test
    void processRecurringTasks_weeklyCompleted8DaysAgo_resetsToNotStarted() {
        OffsetDateTime completedAt = OffsetDateTime.now().minusDays(8);
        Task task = buildRecurringTask(Recurrence.WEEKLY, completedAt);

        when(taskRepository.findCompletedRecurringTasks()).thenReturn(List.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskActivityRepository.save(any(TaskActivity.class))).thenReturn(null);

        recurringTaskScheduler.resetRecurringTasks();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(Status.NOT_STARTED);
        verify(taskActivityRepository, times(1)).save(any(TaskActivity.class));
    }

    @Test
    void processRecurringTasks_weeklyCompleted2DaysAgo_doesNotReset() {
        OffsetDateTime completedAt = OffsetDateTime.now().minusDays(2);
        Task task = buildRecurringTask(Recurrence.WEEKLY, completedAt);

        when(taskRepository.findCompletedRecurringTasks()).thenReturn(List.of(task));

        recurringTaskScheduler.resetRecurringTasks();

        verify(taskRepository, never()).save(any(Task.class));
        verify(taskActivityRepository, never()).save(any(TaskActivity.class));
    }

    @Test
    void processRecurringTasks_monthlyCompleted31DaysAgo_resetsToNotStarted() {
        OffsetDateTime completedAt = OffsetDateTime.now().minusDays(31);
        Task task = buildRecurringTask(Recurrence.MONTHLY, completedAt);

        when(taskRepository.findCompletedRecurringTasks()).thenReturn(List.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskActivityRepository.save(any(TaskActivity.class))).thenReturn(null);

        recurringTaskScheduler.resetRecurringTasks();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(Status.NOT_STARTED);
    }

    @Test
    void processRecurringTasks_monthlyCompleted15DaysAgo_doesNotReset() {
        OffsetDateTime completedAt = OffsetDateTime.now().minusDays(15);
        Task task = buildRecurringTask(Recurrence.MONTHLY, completedAt);

        when(taskRepository.findCompletedRecurringTasks()).thenReturn(List.of(task));

        recurringTaskScheduler.resetRecurringTasks();

        verify(taskRepository, never()).save(any(Task.class));
        verify(taskActivityRepository, never()).save(any(TaskActivity.class));
    }

    @Test
    void processRecurringTasks_taskWithNullRecurrence_skipsTask() {
        Task task = buildRecurringTask(null, OffsetDateTime.now().minusDays(30));

        when(taskRepository.findCompletedRecurringTasks()).thenReturn(List.of(task));

        recurringTaskScheduler.resetRecurringTasks();

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void processRecurringTasks_noCompletedRecurringTasks_noSaves() {
        when(taskRepository.findCompletedRecurringTasks()).thenReturn(List.of());

        recurringTaskScheduler.resetRecurringTasks();

        verify(taskRepository, never()).save(any(Task.class));
        verify(taskActivityRepository, never()).save(any(TaskActivity.class));
    }
}
