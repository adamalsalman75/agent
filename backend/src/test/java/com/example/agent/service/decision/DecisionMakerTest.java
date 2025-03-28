package com.example.agent.service.decision;

import com.example.agent.service.task.TaskAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DecisionMakerTest {

    @Mock
    private ChatClient chatClient;
    
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private TaskAction createTaskAction;

    @Mock
    private TaskAction completeTaskAction;

    private DecisionMaker decisionMaker;

    @BeforeEach
    void setUp() {
        // Use lenient() for all stubs to avoid unnecessary stubbing errors
        lenient().when(createTaskAction.canHandle(anyString())).thenReturn(false);
        lenient().when(completeTaskAction.canHandle(anyString())).thenReturn(false);
        
        // Specific action handlers
        lenient().when(createTaskAction.canHandle("CREATE_TASK")).thenReturn(true);
        lenient().when(completeTaskAction.canHandle("COMPLETE_TASK")).thenReturn(true);
        
        // Setup ChatClient method chain
        lenient().when(chatClient.prompt()).thenReturn(requestSpec);
        lenient().when(requestSpec.system(anyString())).thenReturn(requestSpec);
        lenient().when(requestSpec.user(anyString())).thenReturn(requestSpec);
        lenient().when(requestSpec.call()).thenReturn(responseSpec);
        
        decisionMaker = new DecisionMaker(
            chatClient,
            List.of(createTaskAction, completeTaskAction)
        );
    }

    @Test
    void decide_CreateTaskAction_ShouldReturnValidDecision() {
        // Given
        Map<String, Object> context = Map.of(
            "query", "Create a task to buy groceries"
        );
        
        when(responseSpec.content()).thenReturn("CREATE_TASK|Buy groceries from the store");

        // When
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Then
        assertTrue(result.isPresent());
        ActionDecision decision = result.get();
        assertEquals(createTaskAction, decision.action());
        assertEquals("Buy groceries from the store", decision.parameters().get("description"));
    }

    @Test
    void decide_CompleteTaskAction_ShouldReturnValidDecision() {
        // Given
        Map<String, Object> context = Map.of(
            "query", "Complete task 123"
        );
        
        when(responseSpec.content()).thenReturn("COMPLETE_TASK|123");

        // When
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Then
        assertTrue(result.isPresent());
        ActionDecision decision = result.get();
        assertEquals(completeTaskAction, decision.action());
        assertEquals("123", decision.parameters().get("taskId"));
    }

    @Test
    void decide_WithInvalidAction_ShouldReturnEmpty() {
        // Given
        Map<String, Object> context = Map.of(
            "query", "Invalid action request"
        );
        
        when(responseSpec.content()).thenReturn("INVALID_ACTION|some parameters");

        // When
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void decide_WithMalformedResponse_ShouldReturnEmpty() {
        // Given
        Map<String, Object> context = Map.of(
            "query", "Create task"
        );
        
        when(responseSpec.content()).thenReturn("malformed response without separator");

        // When
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void decide_WithNullContext_ShouldReturnEmpty() {
        // Given
        Map<String, Object> context = Map.of(); // Empty map has no "query" key

        // When
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Then
        assertTrue(result.isEmpty());
        verify(chatClient, never()).prompt();
    }

    @Test
    void decide_WithNullQuery_ShouldReturnEmpty() {
        // Given
        Map<String, Object> context = new HashMap<>();
        context.put("query", null); // Use HashMap to allow null values

        // When
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Then
        assertTrue(result.isEmpty());
        verify(chatClient, never()).prompt();
    }
}