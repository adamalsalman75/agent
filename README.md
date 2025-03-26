# AI Task Assistant Agent
A simple AI agent built with Spring Boot and Spring AI that helps manage tasks using natural language processing.

## Features
- Process natural language requests to create, complete, and list tasks
- Store tasks in a PostgreSQL database using Spring Data JDBC
- RESTful API for direct task management
- Utilizes GPT-4o model for advanced language understanding

## Requirements
- Java 21
- Maven
- Docker and Docker Compose (for PostgreSQL)
- OpenAI API key

## Getting Started

### 1. Start the PostgreSQL database
```bash
docker-compose up -d
```

### 2. Set your OpenAI API key as an environment variable
```bash
export OPENAI_API_KEY=your-api-key-here
```

### 3. Build and run the application
```bash
mvn spring-boot:run
```

## Architecture Highlights
- Uses Java Records for immutable data models
- Implements Spring Data JDBC with ListCrudRepository for data access
- Environment-based configuration for enhanced security

## API Endpoints

### Natural Language Processing
```
POST /api/query
```
Request body:
```json
{
  "query": "Add a new task to buy groceries"
}
```

### Direct Task Management
- `GET /api/tasks` - List all tasks
- `GET /api/tasks/active` - List active (incomplete) tasks
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}/complete` - Mark a task as complete

## Testing with curl

Here are some example curl commands to test the API:

### Natural Language Queries
```bash
# Process a natural language query to create a task
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "Create a task to learn Spring AI by next week"}' | jq

# Ask for all tasks
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "Show me all my tasks"}' | jq

# Complete a specific task
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "Complete task 1"}' | jq
```

### Direct API Endpoints
```bash
# List all tasks
curl http://localhost:8080/api/tasks | jq

# List only active (incomplete) tasks
curl http://localhost:8080/api/tasks/active | jq

# Create a new task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"description": "Learn Spring AI"}' | jq

# Complete a task (replace {id} with the actual task ID)
curl -X PUT http://localhost:8080/api/tasks/1/complete | jq
```

## Example Queries
- "Create a task to learn Spring AI by next week"
- "Show me all my tasks"
- "Complete task 1"
- "What tasks do I have pending?"

## AI Agent Patterns

This project implements several core patterns of AI agent architecture:

### 1. Tasks Pattern
Tasks are discrete units of work that the agent can perform. Each task is self-contained and has a specific responsibility.

In this project:
- `TaskAction` interface defines the contract for all tasks
- Each task (like `CreateTaskAction`, `CompleteTaskAction`) handles one specific operation
- Tasks are automatically discovered and executed based on the AI's intent recognition

### 2. Knowledge Pattern
Knowledge represents the agent's memory and information storage. It allows the agent to learn from experience and maintain state.

In this project:
- `KnowledgeBase` interface provides methods to store and retrieve information
- Can store user preferences, task patterns, and historical data
- Helps the agent make better decisions based on past experiences

### 3. Sensor Pattern
Sensors gather information from the environment, providing the agent with context about its world.

In this project:
- `Sensor` interface defines how to collect information
- Can include sensors for:
  - User input processing
  - System state monitoring
  - Time-based information
  - Environmental conditions

### 4. Decision Making Pattern
The decision-making pattern determines what actions to take based on sensor input and knowledge.

In this project:
- `DecisionMaker` interface evaluates situations and chooses actions
- Uses AI (GPT-4o) to understand intent
- Combines sensor data and knowledge to select appropriate tasks
- Returns structured decisions via `ActionDecision`

### 5. Chain of Thought Pattern
Chain of Thought represents the AI's internal reasoning process, breaking down complex problems into discrete steps.

In this project:
- `ChainOfThought` interface defines how to break down reasoning
- Uses step-by-step thinking to solve complex problems
- Records each step of the reasoning process
- Helps explain how the AI reached its conclusions
- Particularly useful for complex task decomposition

### 6. Refinement Pattern
Refinement represents the iterative improvement process through user feedback and validation.

In this project:
- `Refinement` interface defines the iterative improvement process
- Involves back-and-forth interaction with users
- Asks clarifying questions when needed
- Improves responses based on feedback
- Particularly useful for:
  - Complex task understanding
  - Ambiguity resolution
  - Requirement clarification
  - Input validation and enhancement

### How the Patterns Work Together

1. **Information Gathering**: Sensors collect data from the environment (user input, system state)
2. **Context Building**: Knowledge base provides historical context and learned patterns
3. **Decision Process**: Decision maker evaluates the situation using sensor data and knowledge
4. **Action Execution**: Selected tasks are executed based on the decision
5. **Learning**: Results are stored back in the knowledge base for future reference

This architecture allows the agent to:
- Adapt to different situations
- Learn from past experiences
- Make informed decisions
- Execute actions effectively

