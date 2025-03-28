package com.example.agent.service;

import com.example.agent.model.ConversationContext;
import com.example.agent.model.QueryRequest;
import com.example.agent.model.QueryResponse;
import com.example.agent.model.Task;
import com.example.agent.repository.TaskRepository;
import com.example.agent.service.reasoning.Refinement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Refinement refinement;

    @Mock
    private CompletionService completionService;

    private AIService aiService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aiService = new AIService(taskRepository, refinement, objectMapper, completionService);
    }

    @Test
    void processQuery_WhenNeedsMoreInfo_ShouldReturnFollowUpResponse() {
        String query = "Create a task";
        QueryRequest request = new QueryRequest(query, null);
        
        ConversationContext context = new ConversationContext(
            "CREATE_TASK",
            null,
            true,
            "What is the deadline for this task?",
            null
        );
        
        when(refinement.refineTask(query, null)).thenReturn(context);

        QueryResponse response = aiService.processQuery(request);

        assertTrue(response.requiresFollowUp());
        assertEquals("What is the deadline for this task?", response.response());
        assertNotNull(response.context());
        verify(refinement).refineTask(query, null);
    }

    @Test
    void processQuery_WhenCreatingNewTask_ShouldSaveAndReturnTask() {
        String query = "Create a task to buy groceries tomorrow";
        QueryRequest request = new QueryRequest(query, null);
        
        ObjectNode taskData = objectMapper.createObjectNode()
            .put("description", "Buy groceries")
            .put("deadline", LocalDateTime.now().plusDays(1).toString());
        
        ConversationContext context = new ConversationContext(
            "CREATE_TASK",
            taskData,
            false,
            null,
            null
        );
        
        when(refinement.refineTask(query, null)).thenReturn(context);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        QueryResponse response = aiService.processQuery(request);

        assertFalse(response.requiresFollowUp());
        assertNotNull(response.resultTask());
        assertEquals("Buy groceries", response.resultTask().description());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void processQuery_WhenUpdatingTask_ShouldUpdateAndReturnTask() {
        String query = "Change the deadline to next week";
        Task existingTask = Task.createNew("Original task");
        LocalDateTime newDeadline = LocalDateTime.now().plusWeeks(1);
        
        ObjectNode context = objectMapper.createObjectNode();
        context.put("taskId", 1L);
        context.put("action", "UPDATE_TASK");
        
        QueryRequest request = new QueryRequest(query, context);
        
        ObjectNode updateData = objectMapper.createObjectNode()
            .put("description", "Original task")
            .put("deadline", newDeadline.toString());
        
        ConversationContext conversationContext = new ConversationContext(
            "UPDATE_TASK",
            updateData,
            false,
            null,
            existingTask
        );
        
        when(refinement.refineTask(query, context)).thenReturn(conversationContext);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        QueryResponse response = aiService.processQuery(request);

        assertFalse(response.requiresFollowUp());
        assertNotNull(response.resultTask());
        assertEquals(newDeadline, response.resultTask().deadline());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void processQuery_WhenInvalidInput_ShouldReturnErrorResponse() {
        String query = "Invalid query";
        QueryRequest request = new QueryRequest(query, null);
        
        when(refinement.refineTask(query, null))
            .thenReturn(new ConversationContext(null, null, false, null, null));

        QueryResponse response = aiService.processQuery(request);

        assertFalse(response.requiresFollowUp());
        assertNull(response.resultTask());
        assertTrue(response.response().contains("Failed to process"));
    }

    @Test
    void processQuery_WhenCreatingTaskWithSubtasks_ShouldSaveAllTasks() {
        String query = "Create a project plan";
        QueryRequest request = new QueryRequest(query, null);
        
        ObjectNode taskData = objectMapper.createObjectNode();
        taskData.put("description", "Project Plan");
        taskData.put("priority", "HIGH");
        
        ArrayNode subtasks = taskData.putArray("subtasks");
        ObjectNode subtask1 = subtasks.addObject()
            .put("description", "Research phase")
            .put("priority", "HIGH");
        ObjectNode subtask2 = subtasks.addObject()
            .put("description", "Implementation phase")
            .put("priority", "MEDIUM");
        
        ConversationContext context = new ConversationContext(
            "CREATE_TASK",
            taskData,
            false,
            null,
            null
        );

        when(refinement.refineTask(query, null)).thenReturn(context);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task task = (Task) i.getArgument(0);
            return task;
        });

        QueryResponse response = aiService.processQuery(request);

        assertFalse(response.requiresFollowUp());
        assertNotNull(response.resultTask());
        assertEquals("Project Plan", response.resultTask().description());
        assertEquals("HIGH", response.resultTask().priority());
        
        // Verify save was called 3 times with correct tasks
        verify(taskRepository, times(3)).save(any(Task.class));
        verify(taskRepository).save(argThat(task -> 
            task.description().equals("Project Plan") && 
            task.priority().equals("HIGH") &&
            task.parentId() == null
        ));
        verify(taskRepository).save(argThat(task -> 
            task.description().equals("Research phase") && 
            task.priority().equals("HIGH")
        ));
        verify(taskRepository).save(argThat(task -> 
            task.description().equals("Implementation phase") && 
            task.priority().equals("MEDIUM")
        ));
    }

    @Test
    void processQuery_WhenRequestIsNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aiService.processQuery(null));
        verify(refinement, never()).refineTask(any(), any());
    }

    @Test
    void processQuery_WhenQueryIsBlank_ShouldThrowException() {
        QueryRequest request = new QueryRequest("", null);
        assertThrows(IllegalArgumentException.class, () -> aiService.processQuery(request));
        verify(refinement, never()).refineTask(any(), any());
    }

    @Test
    void processQuery_WhenRefinementFails_ShouldReturnErrorResponse() {
        String query = "Create a task";
        QueryRequest request = new QueryRequest(query, null);
        
        when(refinement.refineTask(query, null))
            .thenThrow(new RuntimeException("Refinement failed"));

        QueryResponse response = aiService.processQuery(request);

        assertFalse(response.requiresFollowUp());
        assertNull(response.resultTask());
        assertTrue(response.response().contains("Error"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void processQuery_WhenTaskSaveFails_ShouldReturnErrorResponse() {
        String query = "Create a task";
        QueryRequest request = new QueryRequest(query, null);
        
        ObjectNode taskData = objectMapper.createObjectNode()
            .put("description", "New task");
        
        ConversationContext context = new ConversationContext(
            "CREATE_TASK",
            taskData,
            false,
            null,
            null
        );
        
        when(refinement.refineTask(query, null)).thenReturn(context);
        when(taskRepository.save(any(Task.class)))
            .thenThrow(new RuntimeException("Save failed"));

        QueryResponse response = aiService.processQuery(request);

        assertFalse(response.requiresFollowUp());
        assertNull(response.resultTask());
        assertTrue(response.response().contains("Error"));
    }
}