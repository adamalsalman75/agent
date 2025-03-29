package com.example.agent.ai.service.decision;

import com.example.agent.ai.model.ConversationContext;
import com.example.agent.common.model.TaskData;
import com.example.agent.task.service.action.CreateTaskAction;
import com.example.agent.task.service.action.TaskAction;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionMakerTest {
    
    @Mock
    private ChatClient chatClient;
    
    @Mock
    private ChatClient.ChatClientRequestSpec reqSpec;
    
    @Mock
    private ChatClient.CallResponseSpec respSpec;
    
    @Mock
    private CreateTaskAction createTaskAction;
    
    @Mock
    private Refinement refinement;

    private DecisionMaker decisionMaker;

    @BeforeEach
    void setUp() {
        decisionMaker = new DecisionMaker(chatClient, List.of(createTaskAction), refinement);
    }

    @Test
    void decide_WithValidCreateTaskIntent_ReturnsActionDecision() {
        // Setup
        String query = "Create a task to buy groceries";
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);
        
        TaskData taskData = new TaskData("Buy groceries", null, "HIGH", null, null);
        ConversationContext refinementContext = new ConversationContext(
            "CREATE_TASK",
            taskData,
            false,
            null,
            null
        );
        
        setupChatClientMocks();
        when(createTaskAction.canHandle("CREATE_TASK")).thenReturn(true);
        // Use Class<T> overload to avoid ambiguity
        when(respSpec.entity(DecisionMaker.IntentClassification.class))
            .thenReturn(new DecisionMaker.IntentClassification("CREATE_TASK"));
        when(refinement.refineTask(query, null)).thenReturn(refinementContext);

        // Execute
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(createTaskAction, result.get().action());
        assertNotNull(result.get().parameters());
    }

    @Test
    void decide_WithUnrecognizedIntent_ReturnsEmpty() {
        // Setup
        String query = "Unknown command";
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);
        
        setupChatClientMocks();
        // Use Class<T> overload to avoid ambiguity
        when(respSpec.entity(DecisionMaker.IntentClassification.class))
            .thenReturn(new DecisionMaker.IntentClassification(""));

        // Execute
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void decide_RequiringFollowUp_ReturnsActionDecisionWithPrompt() {
        // Setup
        String query = "Create a task";
        Map<String, Object> context = new HashMap<>();
        context.put("query", query);
        
        ConversationContext refinementContext = new ConversationContext(
            "CREATE_TASK",
            TaskData.createEmpty(),
            true,
            "What would you like to create a task for?",
            null
        );
        
        setupChatClientMocks();
        // Use Class<T> overload to avoid ambiguity
        when(respSpec.entity(DecisionMaker.IntentClassification.class))
            .thenReturn(new DecisionMaker.IntentClassification("CREATE_TASK"));
        when(refinement.refineTask(query, null)).thenReturn(refinementContext);

        // Execute
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Verify
        assertTrue(result.isPresent());
        ActionDecision decision = result.get();
        assertTrue(decision.parameters() == null || decision.parameters().description() == null);
        assertEquals("What would you like to create a task for?", refinementContext.nextPrompt());
    }

    @Test
    void decide_WithNullQuery_ReturnsEmpty() {
        // Setup
        Map<String, Object> context = new HashMap<>();

        // Execute
        Optional<ActionDecision> result = decisionMaker.decide(context);

        // Verify
        assertTrue(result.isEmpty());
    }

    private void setupChatClientMocks() {
        when(chatClient.prompt()).thenReturn(reqSpec);
        when(reqSpec.system(anyString())).thenReturn(reqSpec);
        when(reqSpec.user(anyString())).thenReturn(reqSpec);
        when(reqSpec.call()).thenReturn(respSpec);
    }
}