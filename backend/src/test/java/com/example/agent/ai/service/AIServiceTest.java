package com.example.agent.ai.service;

import com.example.agent.ai.model.ConversationContext;
import com.example.agent.ai.model.QueryRequest;
import com.example.agent.ai.model.QueryResponse;
import com.example.agent.ai.service.decision.ActionDecision;
import com.example.agent.ai.service.decision.DecisionMaker;
import com.example.agent.common.model.Task;
import com.example.agent.common.model.TaskData;
import com.example.agent.task.repository.TaskRepository;
import com.example.agent.task.service.action.RequireInfoAction;
import com.example.agent.task.service.action.TaskAction;
import com.example.agent.task.service.action.TaskParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DecisionMaker decisionMaker;

    @Mock
    private TaskAction taskAction;

    @Mock
    private RequireInfoAction requireInfoAction;

    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIService(taskRepository, decisionMaker);
    }

    @Test
    void processQuery_WithValidQuery_ReturnsSuccessResponse() {
        // Setup
        String query = "Create a task to buy groceries";
        QueryRequest request = new QueryRequest(query, null);

        String futureDate = LocalDateTime.now().plusDays(30).toString();

        TaskParameters taskParameters = TaskParameters.forCreateTask(
            null, 
            "Buy groceries", 
            futureDate, 
            "HIGH", 
            null
        );

        Task createdTask = Task.createNewWithDetails(
            "Buy groceries",
            LocalDateTime.now().plusDays(30),
            "HIGH",
            null,
            null,
            null
        );

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("query", query);
        contextMap.put("previousContext", ConversationContext.createNew());

        when(decisionMaker.decide(any())).thenReturn(Optional.of(new ActionDecision(taskAction, taskParameters)));
        when(taskAction.execute(any())).thenReturn(taskParameters);
        when(taskRepository.save(any())).thenReturn(createdTask);

        // Execute
        QueryResponse response = aiService.processQuery(request);

        // Verify
        assertNotNull(response);
        assertEquals("Task processed successfully", response.response());
        assertNotNull(response.resultTask());
        assertEquals("Buy groceries", response.resultTask().description());
        assertFalse(response.requiresFollowUp());
        assertNull(response.context());
    }

    @Test
    void processQuery_WithNullQuery_ThrowsIllegalArgumentException() {
        // Setup
        QueryRequest request = new QueryRequest(null, null);

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> aiService.processQuery(request));
    }

    @Test
    void processQuery_WithEmptyQuery_ThrowsIllegalArgumentException() {
        // Setup
        QueryRequest request = new QueryRequest("", null);

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> aiService.processQuery(request));
    }

    @Test
    void processQuery_WithNoRecognizedIntent_ReturnsErrorResponse() {
        // Setup
        String query = "Unknown command";
        QueryRequest request = new QueryRequest(query, null);

        when(decisionMaker.decide(any())).thenReturn(Optional.empty());

        // Execute
        QueryResponse response = aiService.processQuery(request);

        // Verify
        assertNotNull(response);
        assertTrue(response.response().contains("No intent recognized"));
        assertNull(response.resultTask());
        assertFalse(response.requiresFollowUp());
        assertNull(response.context());
    }

    @Test
    void processQuery_RequiringMoreInfo_ReturnsPromptResponse() {
        // Setup
        String query = "Create a task";
        QueryRequest request = new QueryRequest(query, null);

        ConversationContext context = new ConversationContext(
            "CREATE_TASK",
            TaskData.createEmpty(),
            true,
            "What would you like to create a task for?",
            null
        );

        when(decisionMaker.decide(any())).thenReturn(Optional.of(
            ActionDecision.requireMoreInfo(requireInfoAction, "What would you like to create a task for?", context)
        ));

        // Execute
        QueryResponse response = aiService.processQuery(request);

        // Verify
        assertNotNull(response);
        assertEquals("What would you like to create a task for?", response.response());
        assertNull(response.resultTask());
        assertTrue(response.requiresFollowUp());
        assertEquals(context, response.context());
    }

    @Test
    void processQuery_WithExistingTask_ReturnsTaskResponse() {
        // Setup
        String query = "Complete task 1";
        QueryRequest request = new QueryRequest(query, null);

        TaskParameters taskParameters = TaskParameters.forCompleteTask("1");

        Task existingTask = Task.createNewWithDetails(
            "Existing task",
            null,
            "MEDIUM",
            null,
            null,
            null
        );

        when(decisionMaker.decide(any())).thenReturn(Optional.of(new ActionDecision(taskAction, taskParameters)));
        when(taskAction.execute(any())).thenReturn(taskParameters);
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(existingTask));

        // Execute
        QueryResponse response = aiService.processQuery(request);

        // Verify
        assertNotNull(response);
        assertEquals("Task processed successfully", response.response());
        assertNotNull(response.resultTask());
        assertEquals("Existing task", response.resultTask().description());
        assertFalse(response.requiresFollowUp());
        assertNull(response.context());
    }

    @Test
    void processQuery_WithTaskNotFound_ThrowsIllegalArgumentException() {
        // Setup
        String query = "Complete task 999";
        QueryRequest request = new QueryRequest(query, null);

        TaskParameters taskParameters = TaskParameters.forCompleteTask("999");

        when(decisionMaker.decide(any())).thenReturn(Optional.of(new ActionDecision(taskAction, taskParameters)));
        when(taskAction.execute(any())).thenReturn(taskParameters);
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> aiService.processQuery(request));
    }

    @Test
    void processQuery_WithGenericException_ReturnsErrorResponse() {
        // Setup
        String query = "Create a task";
        QueryRequest request = new QueryRequest(query, null);

        when(decisionMaker.decide(any())).thenThrow(new RuntimeException("Test exception"));

        // Execute
        QueryResponse response = aiService.processQuery(request);

        // Verify
        assertNotNull(response);
        assertTrue(response.response().contains("Error processing query"));
        assertNull(response.resultTask());
        assertFalse(response.requiresFollowUp());
        assertNull(response.context());
    }
}
