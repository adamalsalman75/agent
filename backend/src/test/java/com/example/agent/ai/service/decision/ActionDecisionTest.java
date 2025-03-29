package com.example.agent.ai.service.decision;

import com.example.agent.task.service.action.TaskAction;
import com.example.agent.task.service.action.TaskParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ActionDecisionTest {

    @Mock
    private TaskAction mockAction;

    @Test
    void constructor_WithActionAndParameters_CreatesDecision() {
        // Setup
        TaskParameters params = TaskParameters.forCreateTask(
            "task-123",
            "Test task",
            null,
            null,
            null
        );

        // Execute
        ActionDecision decision = new ActionDecision(mockAction, params);

        // Verify
        assertEquals(mockAction, decision.action());
        assertEquals(params, decision.parameters());
        assertNull(decision.nextPrompt());
        assertNull(decision.context());
    }

    @Test
    void requireMoreInfo_CreatesDecisionWithPromptAndContext() {
        // Setup
        String nextPrompt = "What is the deadline?";
        Object context = "Previous context";

        // Execute
        ActionDecision decision = ActionDecision.requireMoreInfo(mockAction, nextPrompt, context);

        // Verify
        assertEquals(mockAction, decision.action());
        assertNull(decision.parameters());
        assertEquals(nextPrompt, decision.nextPrompt());
        assertEquals(context, decision.context());
    }

    @Test
    void requireMoreInfo_WithNullContext_CreatesDecisionWithPrompt() {
        // Setup
        String nextPrompt = "What is the priority?";

        // Execute
        ActionDecision decision = ActionDecision.requireMoreInfo(mockAction, nextPrompt, null);

        // Verify
        assertEquals(mockAction, decision.action());
        assertNull(decision.parameters());
        assertEquals(nextPrompt, decision.nextPrompt());
        assertNull(decision.context());
    }
}