package com.example.agent.service.reasoning;

import com.example.agent.model.ConversationContext;
import com.example.agent.service.CompletionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefinementTest {

    @Mock
    private CompletionService completionService;

    private ObjectMapper objectMapper;
    private Refinement refinement;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        refinement = new Refinement(completionService, objectMapper);
    }

    @Test
    void refineTask_WithNewTask_ShouldCreateNewContext() {
        // Given
        String query = "Create a task to buy groceries";
        when(completionService.getCompletion(anyString()))
                .thenReturn("Buy groceries from the store");

        // When
        ConversationContext result = refinement.refineTask(query, null);

        // Then
        assertNotNull(result);
        assertEquals("CREATE_TASK", result.currentIntent());
        assertFalse(result.requiresFollowUp());
        assertNull(result.nextPrompt());
        assertNotNull(result.collectedData());
        assertTrue(result.collectedData().toString().contains("Buy groceries from the store"));
    }

    @Test
    void refineTask_WhenNeedingMoreInfo_ShouldRequestFollowUp() {
        // Given
        String query = "Create a task for the conference";
        when(completionService.getCompletion(anyString()))
                .thenReturn("NEEDS_MORE_INFO:Is this for organizing or attending a conference?");

        // When
        ConversationContext result = refinement.refineTask(query, null);

        // Then
        assertNotNull(result);
        assertTrue(result.requiresFollowUp());
        assertEquals("Is this for organizing or attending a conference?", result.nextPrompt());
    }

    @Test
    void refineTask_WithExistingContext_ShouldContinueConversation() {
        // Given
        String query = "It's for organizing a tech conference";
        ObjectNode contextData = objectMapper.createObjectNode();
        contextData.put("description", "Conference task");
        
        JsonNode context = objectMapper.valueToTree(new ConversationContext(
            "CREATE_TASK",
            contextData,
            true,
            "Is this for organizing or attending a conference?",
            null
        ));

        when(completionService.getCompletion(anyString()))
                .thenReturn("Organize tech conference with venue selection and speaker invitations");

        // When
        ConversationContext result = refinement.refineTask(query, context);

        // Then
        assertNotNull(result);
        assertEquals("CREATE_TASK", result.currentIntent());
        assertFalse(result.requiresFollowUp());
        assertNull(result.nextPrompt());
        assertTrue(result.collectedData().toString().contains("tech conference"));
    }

    @Test
    void refineTask_WithInvalidContext_ShouldHandleGracefully() {
        // Given
        String query = "Create a task";
        ObjectNode invalidContext = objectMapper.createObjectNode();
        invalidContext.put("invalid", "data");

        when(completionService.getCompletion(anyString()))
                .thenReturn("Simple task created");

        // When
        ConversationContext result = refinement.refineTask(query, invalidContext);

        // Then
        assertNotNull(result);
        assertEquals("CREATE_TASK", result.currentIntent());
        assertFalse(result.requiresFollowUp());
        assertNotNull(result.collectedData());
    }

    @Test
    void refineTask_WithNullResponse_ShouldHandleGracefully() {
        // Given
        String query = "Create a task";
        when(completionService.getCompletion(anyString()))
                .thenReturn(null);

        // When
        ConversationContext result = refinement.refineTask(query, null);

        // Then
        assertNotNull(result);
        assertEquals("CREATE_TASK", result.currentIntent());
        assertFalse(result.requiresFollowUp());
        assertNotNull(result.collectedData());
    }
}