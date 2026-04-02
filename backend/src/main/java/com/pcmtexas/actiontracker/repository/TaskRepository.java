package com.pcmtexas.actiontracker.repository;

import com.pcmtexas.actiontracker.entity.Task;
import com.pcmtexas.actiontracker.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByAssigneeEmail(String email);

    List<Task> findByAssignedByEmail(String email);

    List<Task> findByStatus(Status status);

    List<Task> findByDueDateBefore(LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.assigneeEmail = :assigneeEmail AND t.dueDate < :today AND t.status <> 'COMPLETE'")
    List<Task> findOverdueByAssignee(@Param("assigneeEmail") String assigneeEmail,
                                     @Param("today") LocalDate today);

    @Query("""
            SELECT t FROM Task t
            WHERE (:assigneeEmail IS NULL OR t.assigneeEmail = :assigneeEmail)
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom)
              AND (:dueDateTo IS NULL OR t.dueDate <= :dueDateTo)
              AND (:projectTag IS NULL OR t.projectTag = :projectTag)
              AND (:recurringOnly = false OR t.isRecurring = true)
            ORDER BY t.dueDate ASC NULLS LAST, t.priority ASC, t.createdAt DESC
            """)
    List<Task> findByFilters(
            @Param("assigneeEmail") String assigneeEmail,
            @Param("status") com.pcmtexas.actiontracker.enums.Status status,
            @Param("priority") com.pcmtexas.actiontracker.enums.Priority priority,
            @Param("dueDateFrom") LocalDate dueDateFrom,
            @Param("dueDateTo") LocalDate dueDateTo,
            @Param("projectTag") String projectTag,
            @Param("recurringOnly") boolean recurringOnly
    );

    @Query("""
            SELECT t FROM Task t
            WHERE (t.assigneeEmail = :email OR t.assignedByEmail = :email)
              AND (:assigneeEmailFilter IS NULL OR t.assigneeEmail = :assigneeEmailFilter)
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom)
              AND (:dueDateTo IS NULL OR t.dueDate <= :dueDateTo)
              AND (:projectTag IS NULL OR t.projectTag = :projectTag)
              AND (:recurringOnly = false OR t.isRecurring = true)
            ORDER BY t.dueDate ASC NULLS LAST, t.priority ASC, t.createdAt DESC
            """)
    List<Task> findByFiltersForMember(
            @Param("email") String email,
            @Param("assigneeEmailFilter") String assigneeEmailFilter,
            @Param("status") com.pcmtexas.actiontracker.enums.Status status,
            @Param("priority") com.pcmtexas.actiontracker.enums.Priority priority,
            @Param("dueDateFrom") LocalDate dueDateFrom,
            @Param("dueDateTo") LocalDate dueDateTo,
            @Param("projectTag") String projectTag,
            @Param("recurringOnly") boolean recurringOnly
    );

    @Query("SELECT t FROM Task t WHERE t.isRecurring = true AND t.status = 'COMPLETE'")
    List<Task> findCompletedRecurringTasks();

    @Query("SELECT t FROM Task t WHERE t.dueDate = :tomorrow AND t.status <> 'COMPLETE'")
    List<Task> findTasksDueTomorrow(@Param("tomorrow") LocalDate tomorrow);
}
