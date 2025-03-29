package com.example.agent.task.repository;

import com.example.agent.common.model.Task;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {
    List<Task> findByCompletedFalse();
    
    @Query("SELECT * FROM tasks WHERE parent_id = :parentId")
    List<Task> findSubtasks(@Param("parentId") Long parentId);
    
    @Query("SELECT * FROM tasks WHERE parent_id IS NULL")
    List<Task> findRootTasks();
    
    @Query("SELECT * FROM tasks WHERE priority = :priority")
    List<Task> findByPriority(@Param("priority") String priority);
    
    @Query("SELECT * FROM tasks WHERE deadline < CURRENT_TIMESTAMP AND completed = false")
    List<Task> findOverdueTasks();
}