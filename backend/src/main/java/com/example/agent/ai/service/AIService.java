package com.example.agent.ai.service;

import com.example.agent.ai.model.ConversationContext;
import com.example.agent.ai.model.QueryRequest;
import com.example.agent.ai.model.QueryResponse;
import com.example.agent.common.model.Task;
import com.example.agent.task.repository.TaskRepository;
import com.example.agent.ai.service.decision.DecisionMaker;
import com.example.agent.ai.service.decision.ActionDecision;
import com.example.agent.task.service.action.RequireInfoAction;
import com.example.agent.task.service.action.TaskParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final TaskRepository taskRepository;
    private final DecisionMaker decisionMaker;
    
    public AIService(TaskRepository taskRepository, DecisionMaker decisionMaker) {
        this.taskRepository = taskRepository;
        this.decisionMaker = decisionMaker;
    }
    
    public QueryResponse processQuery(QueryRequest request) {
        logger.info("Entering processQuery with request: {}", request);
        try {
            if (request == null || request.query() == null || request.query().trim().isEmpty()) {
                logger.warn("Invalid query request: {}", request);
                throw new IllegalArgumentException("Query cannot be null or empty");
            }

            logger.debug("Creating context map for decision maker");
            Map<String, Object> contextMap = new HashMap<>();
            contextMap.put("query", request.query());

            ConversationContext previousContext = request.context() != null ? 
                request.context() : ConversationContext.createNew();
            contextMap.put("previousContext", previousContext);

            logger.info("Processing query through decision maker");
            Optional<ActionDecision> decision = decisionMaker.decide(contextMap);
            if (decision.isEmpty()) {
                logger.warn("No intent recognized for query: {}", request.query());
                return new QueryResponse("Failed to process query: No intent recognized", null, false, null);
            }

            ActionDecision actionDecision = decision.get();
            logger.debug("Action decision made: {}", actionDecision);

            if (actionDecision.action() instanceof RequireInfoAction) {
                logger.info("More information required: {}", actionDecision.nextPrompt());
                return new QueryResponse(actionDecision.nextPrompt(), null, true, (ConversationContext) actionDecision.context());
            }

            logger.info("Executing action: {}", actionDecision.action());
            TaskParameters result = actionDecision.action().execute(actionDecision.parameters());
            Task resultTask = null;

            if (result.taskId() != null && !result.taskId().isEmpty()) {
                logger.debug("Fetching task by ID: {}", result.taskId());
                resultTask = taskRepository.findById(Long.parseLong(result.taskId()))
                    .orElseThrow(() -> new IllegalArgumentException("Task not found"));
            } else if (result.description() != null && !result.description().isEmpty()) {
                logger.debug("Creating new task from parameters");
                resultTask = createTaskFromParameters(result);
            }

            logger.info("Task processed successfully");
            return new QueryResponse("Task processed successfully", resultTask, false, null);
        } catch (IllegalArgumentException e) {
            logger.error("Illegal argument exception: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            return new QueryResponse("Error processing query: " + e.getMessage(), null, false, null);
        } finally {
            logger.info("Exiting processQuery");
        }
    }
    
    private Task createTaskFromParameters(TaskParameters params) {
        Task task = Task.createNewWithDetails(
            params.description(),
            params.deadline() != null ? LocalDateTime.parse(params.deadline()) : null,
            params.priority(),
            params.constraints(),
            null,
            null
        );
        return taskRepository.save(task);
    }
}