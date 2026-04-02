package com.pcmtexas.actiontracker.repository;

import com.pcmtexas.actiontracker.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    @Query("""
            SELECT a FROM TaskActivity a
            WHERE a.actorEmail = :actorEmail OR a.taskId IN :taskIds
            ORDER BY a.createdAt DESC
            LIMIT 10
            """)
    List<TaskActivity> findTop10ByActorEmailOrTaskIdInOrderByCreatedAtDesc(
            @Param("actorEmail") String actorEmail,
            @Param("taskIds") Collection<UUID> taskIds
    );

    @Query("SELECT a FROM TaskActivity a ORDER BY a.createdAt DESC LIMIT 20")
    List<TaskActivity> findRecent20();
}
