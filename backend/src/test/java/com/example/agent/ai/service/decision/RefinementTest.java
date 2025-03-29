package com.example.agent.ai.service.decision;

import com.example.agent.ai.model.ConversationContext;
import com.example.agent.common.model.TaskData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefinementTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec reqSpec;

    @Mock
    private ChatClient.CallResponseSpec respSpec;

    private Refinement refinement;

    @BeforeEach
    void setUp() {
        refinement = new Refinement(chatClient);
        when(chatClient.prompt()).thenReturn(reqSpec);
        when(reqSpec.system(anyString())).thenReturn(reqSpec);
        when(reqSpec.user(anyString())).thenReturn(reqSpec);
        when(reqSpec.call()).thenReturn(respSpec);
    }

    @Test
    void refineTask_WithNewTask_ReturnsContext() {
        // Setup
        String query = "Create a task to buy groceries with high priority";
        Class<?> responseClass = refinement.getClass().getDeclaredClasses()[0];
        doReturn(createTaskRefinementResponse(
            "Buy groceries",
            null,
            "HIGH",
            null,
            false,
            null
        )).when(respSpec).entity(responseClass);

        // Execute
        ConversationContext result = refinement.refineTask(query, null);

        // Verify
        assertNotNull(result);
        assertEquals("Buy groceries", result.collectedData().description());
        assertEquals("HIGH", result.collectedData().priority());
        assertFalse(result.requiresFollowUp());
        assertNull(result.nextPrompt());
    }

    @Test
    void refineTask_WithIncompleteInfo_ReturnsContextWithFollowUp() {
        // Setup
        String query = "Create a task";
        Class<?> responseClass = refinement.getClass().getDeclaredClasses()[0];
        doReturn(createTaskRefinementResponse(
            null,
            null,
            null,
            null,
            true,
            "What would you like to create a task for?"
        )).when(respSpec).entity(responseClass);

        // Execute
        ConversationContext result = refinement.refineTask(query, null);

        // Verify
        assertNotNull(result);
        assertTrue(result.requiresFollowUp());
        assertEquals("What would you like to create a task for?", result.nextPrompt());
    }

    @Test
    void refineTask_WithPreviousData_UpdatesExistingInfo() {
        // Setup
        String query = "Set the deadline to tomorrow";
        TaskData previousData = new TaskData("Buy groceries", null, "HIGH", null, null);
        Class<?> responseClass = refinement.getClass().getDeclaredClasses()[0];
        
        doReturn(createTaskRefinementResponse(
            "Buy groceries",
            "2025-03-30",
            "HIGH",
            null,
            false,
            null
        )).when(respSpec).entity(responseClass);

        // Execute
        ConversationContext result = refinement.refineTask(query, previousData);

        // Verify
        assertNotNull(result);
        assertEquals("Buy groceries", result.collectedData().description());
        assertEquals("2025-03-30", result.collectedData().deadline());
        assertEquals("HIGH", result.collectedData().priority());
        assertFalse(result.requiresFollowUp());
    }

    private Object createTaskRefinementResponse(
            String description,
            String deadline,
            String priority,
            String constraints,
            boolean needsMoreInfo,
            String followUpQuestion) {
        try {
            Class<?> responseClass = refinement.getClass().getDeclaredClasses()[0];
            return responseClass.getDeclaredConstructors()[0].newInstance(
                description, deadline, priority, constraints, needsMoreInfo, followUpQuestion);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TaskRefinementResponse", e);
        }
    }
}