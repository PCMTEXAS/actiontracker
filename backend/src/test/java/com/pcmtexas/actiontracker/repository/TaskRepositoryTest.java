package com.pcmtexas.actiontracker.repository;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.enums.Priority;
import com.pcmtexas.actiontracker.enums.Recurrence;
import com.pcmtexas.actiontracker.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema-h2.sql"
})
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task buildTask(String title, String assigneeEmail, String assignedByEmail,
                            Status status, Priority priority, LocalDate dueDate,
                            boolean recurring, Recurrence recurrence) {
        return Task.builder()
                .title(title)
                .description("Description for " + title)
                .assigneeEmail(assigneeEmail)
                .assigneeName("Assignee")
                .assignedByEmail(assignedByEmail)
                .assignedByName("Owner")
                .dueDate(dueDate)
                .priority(priority)
                .status(status)
                .isRecurring(recurring)
                .recurrence(recurrence)
                .build();
    }

    @Test
    void findByFilters_withNoFilters_returnsAllTasks() {
        taskRepository.save(buildTask("Task A", "a@example.com", "owner@example.com",
                Status.NOT_STARTED, Priority.MEDIUM, null, false, null));
        taskRepository.save(buildTask("Task B", "b@example.com", "owner@example.com",
                Status.IN_PROGRESS, Priority.HIGH, null, false, null));

        List<Task> result = taskRepository.findByFilters(
                null, null, null, null, null, null, false);

        assertThat(result).hasSize(2);
    }

    @Test
    void findByFilters_withStatusFilter_returnsMatchingTasks() {
        taskRepository.save(buildTask("Not Started", "a@example.com", "owner@example.com",
                Status.NOT_STARTED, Priority.MEDIUM, null, false, null));
        taskRepository.save(buildTask("In Progress", "b@example.com", "owner@example.com",
                Status.IN_PROGRESS, Priority.HIGH, null, false, null));
        taskRepository.save(buildTask("Complete", "c@example.com", "owner@example.com",
                Status.COMPLETE, Priority.LOW, null, false, null));

        List<Task> result = taskRepository.findByFilters(
                null, Status.IN_PROGRESS, null, null, null, null, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("In Progress");
    }

    @Test
    void findByFiltersForMember_returnsOnlyMemberTasks() {
        taskRepository.save(buildTask("Alice Task", "alice@example.com", "owner@example.com",
                Status.NOT_STARTED, Priority.MEDIUM, null, false, null));
        taskRepository.save(buildTask("Bob Task", "bob@example.com", "owner@example.com",
                Status.NOT_STARTED, Priority.MEDIUM, null, false, null));

        List<Task> result = taskRepository.findByFiltersForMember(
                "alice@example.com", null, null, null, null, null, null, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Alice Task");
    }

    @Test
    void findByDueDateBefore_returnsOverdueTasks() {
        LocalDate pastDate = LocalDate.now().minusDays(3);
        LocalDate futureDate = LocalDate.now().plusDays(3);

        taskRepository.save(buildTask("Overdue Task", "a@example.com", "owner@example.com",
                Status.NOT_STARTED, Priority.HIGH, pastDate, false, null));
        taskRepository.save(buildTask("Future Task", "b@example.com", "owner@example.com",
                Status.NOT_STARTED, Priority.MEDIUM, futureDate, false, null));

        List<Task> result = taskRepository.findByDueDateBefore(LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Overdue Task");
    }

    @Test
    void findCompletedRecurringTasks_returnsOnlyCompleteRecurring() {
        // Complete + recurring — should be returned
        taskRepository.save(buildTask("Complete Recurring", "a@example.com", "owner@example.com",
                Status.COMPLETE, Priority.MEDIUM, null, true, Recurrence.WEEKLY));

        // In-progress + recurring — should NOT be returned
        taskRepository.save(buildTask("InProgress Recurring", "b@example.com", "owner@example.com",
                Status.IN_PROGRESS, Priority.MEDIUM, null, true, Recurrence.MONTHLY));

        // Complete + non-recurring — should NOT be returned
        taskRepository.save(buildTask("Complete NonRecurring", "c@example.com", "owner@example.com",
                Status.COMPLETE, Priority.MEDIUM, null, false, null));

        List<Task> result = taskRepository.findCompletedRecurringTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Complete Recurring");
    }
}
