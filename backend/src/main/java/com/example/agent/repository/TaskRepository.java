package com.example.agent.repository;

import com.example.agent.model.Task;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends ListCrudRepository<Task, Long> {
    
    List<Task> findByCompletedOrderByCreatedAtDesc(boolean completed);
    
}