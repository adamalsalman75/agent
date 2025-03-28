# AI Task Assistant

A sophisticated task management system powered by AI that understands natural language and helps users manage tasks intelligently. The system implements advanced AI agent patterns to provide an intuitive and powerful task management experience.

## ✨ Key Features

- 🤖 **Natural Language Understanding**: Create and update tasks using everyday language
- 📋 **Smart Task Management**: Automatically organize tasks with priorities, deadlines, and dependencies
- 🌳 **Hierarchical Organization**: Support for nested tasks and subtasks
- 🔄 **Interactive Refinement**: AI-driven conversation to refine task details
- 🎯 **Intelligent Assistance**: Contextual suggestions and task breakdown
- 🔍 **Advanced Filtering**: Find tasks by priority, status, or deadline

## 🏗️ Project Structure

This project is split into two main components:

### [📡 Backend](/backend)
Spring Boot backend implementing sophisticated AI agent patterns:
- Natural language processing with OpenAI integration
- Advanced task management with PostgreSQL
- RESTful API with comprehensive endpoints
- [View Backend Documentation](/backend/README.md)

### [🎨 Frontend](/frontend)
Modern React frontend providing an intuitive interface:
- Clean, responsive Material-UI design
- Real-time updates with React Query
- End-to-end testing with Cypress
- [View Frontend Documentation](/frontend/README.md)

## 🧠 AI Architecture

The system implements several sophisticated AI agent patterns:

1. **📝 Tasks Pattern**: Discrete units of work with specific responsibilities
2. **🗄️ Knowledge Pattern**: System memory and information storage
3. **🤔 Decision Making**: Intelligent action selection based on context
4. **🔄 Refinement Pattern**: Iterative improvement through conversation

## 🚀 Getting Started

1. Clone the repository
2. Set up the backend:
   ```bash
   cd backend
   docker-compose up -d    # Start PostgreSQL
   export OPENAI_API_KEY=your-key-here
   mvn spring-boot:run
   ```
3. Set up the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
4. Visit http://localhost:5173 to start using the application

## 🛠️ Technical Stack

- **Backend**:
  - Spring Boot 3.4
  - Spring AI for OpenAI integration
  - PostgreSQL with Spring Data JDBC
  - Docker for database containerization

- **Frontend**:
  - React 19 with TypeScript
  - Material-UI for component library
  - React Query for state management
  - Cypress for E2E testing

## 📚 Documentation

- [Backend API Documentation](/backend/README.md#api-documentation)
- [Frontend Architecture](/frontend/README.md#frontend-architecture)
- [AI Pattern Implementation Details](/backend/features/completed/initial-patterns.md)
- [Upcoming Features](/backend/features/inprogress/refinement-pattern.md)

## 🤝 Contributing

We welcome contributions! Please check out our contributing guidelines (coming soon).

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

