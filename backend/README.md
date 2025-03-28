# AI Task Assistant Backend

Spring Boot backend implementing an AI agent for natural language task management.

## Features
- Process natural language requests to create, complete, and list tasks
- Store tasks in a PostgreSQL database using Spring Data JDBC
- RESTful API for direct task management
- Utilizes GPT-4o model for advanced language understanding
- Implements various AI agent patterns for sophisticated task handling

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

## API Documentation

### Natural Language Processing
```
POST /api/query
Content-Type: application/json

{
  "query": "Add a new task to buy groceries"
}
```

### Direct Task Management
- `GET /api/tasks` - List all tasks
- `GET /api/tasks/active` - List active (incomplete) tasks
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}/complete` - Mark a task as complete

## Implementation Details

### Key Components
- Task Actions: CreateTaskAction, CompleteTaskAction, ListTasksAction
- Decision Making: OpenAIDecisionMaker for intent classification
- Data Model: Immutable Task records with Spring Data JDBC
- Configuration: Environment-based settings and CORS setup

### Code Organization
- `config/` - Spring configuration including AI and database setup
- `controller/` - REST API endpoints
- `model/` - Data models and domain objects
- `repository/` - Data access layer
- `service/` - Business logic and AI integration
  - `decision/` - Intent classification and decision making
  - `knowledge/` - State and context management
  - `reasoning/` - Chain of thought implementation
  - `sensor/` - Environment data collection
  - `task/` - Task action implementations

## System Architecture
The following diagram illustrates the flow of data and control through the system's components:

```mermaid
graph TD
    Client[Client Request] -->|HTTP| Controller[AgentController]
    Controller -->|QueryRequest| AIService[AIService]
    
    subgraph "AI Processing"
        AIService -->|Input| DecisionMaker[Decision Making Layer]
        DecisionMaker -->|Intent| ReasoningEngine[Reasoning Engine]
        ReasoningEngine -->|Context| KnowledgeBase[Knowledge Base]
        
        SensorSystem[Sensor System] -->|Environment Data| KnowledgeBase
        KnowledgeBase -->|State| ReasoningEngine
        
        ReasoningEngine -->|Action Selection| TaskProcessor[Task Processor]
    end
    
    subgraph "Task Management"
        TaskProcessor -->|Execute| TaskService[TaskService]
        TaskService -->|CRUD Operations| Repository[(TaskRepository)]
        Repository -->|Data| Database[(PostgreSQL)]
    end
    
    TaskService -->|Result| CompletionService[CompletionService]
    CompletionService -->|Response| Controller
    Controller -->|QueryResponse| Client
```

### Component Descriptions
- **Controller Layer**: Handles HTTP requests and response formatting
- **AI Processing**:
  - Decision Making: Analyzes user intent using OpenAI
  - Reasoning Engine: Implements chain-of-thought processing
  - Knowledge Base: Maintains system state and context
  - Sensor System: Gathers environmental data
- **Task Management**:
  - Task Processor: Executes determined actions
  - Task Service: Handles business logic