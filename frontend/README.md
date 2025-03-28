# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default tseslint.config({
  extends: [
    // Remove ...tseslint.configs.recommended and replace with this
    ...tseslint.configs.recommendedTypeChecked,
    // Alternatively, use this for stricter rules
    ...tseslint.configs.strictTypeChecked,
    // Optionally, add this for stylistic rules
    ...tseslint.configs.stylisticTypeChecked,
  ],
  languageOptions: {
    // other options...
    parserOptions: {
      project: ['./tsconfig.node.json', './tsconfig.app.json'],
      tsconfigRootDir: import.meta.dirname,
    },
  },
})
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default tseslint.config({
  plugins: {
    // Add the react-x and react-dom plugins
    'react-x': reactX,
    'react-dom': reactDom,
  },
  rules: {
    // other rules...
    // Enable its recommended typescript rules
    ...reactX.configs['recommended-typescript'].rules,
    ...reactDom.configs.recommended.rules,
  },
})
```

## Frontend Architecture

The following diagram illustrates the component hierarchy and data flow in the frontend application:

```mermaid
graph TD
    subgraph "Component Hierarchy"
        App[App.tsx] -->|Renders| TaskForm[TaskForm]
        App -->|Renders| TaskList[TaskList]
        App -->|Renders| Dialog[Update Dialog]
        Dialog -->|Contains| UpdateForm[TaskForm in Update Mode]
        TaskList -->|Renders Multiple| TaskItem[TaskItem]
        TaskItem -->|May Render| Subtasks[Nested TaskItems]
    end

    subgraph "State Management"
        RQ[React Query] -->|Cache & Sync| Tasks[(Tasks)]
        Tasks -->|Read| TaskList
        Tasks -->|Update| TaskForm
        Tasks -->|Update| UpdateForm
    end

    subgraph "API Integration"
        TaskForm -->|POST| ProcessQuery[/api/query]
        UpdateForm -->|POST| ProcessQuery
        TaskList -->|GET| RootTasks[/api/tasks/root]
        TaskItem -->|GET| Subtasks[/api/tasks/{id}/subtasks]
        TaskItem -->|PUT| CompleteTask[/api/tasks/{id}/complete]
    end

    subgraph "User Interactions"
        User -->|Create Task| TaskForm
        User -->|Complete Task| TaskItem
        User -->|Update Task| Dialog
        User -->|Expand/Collapse| TaskItem
        ProcessQuery -->|Refinement Loop| User
    end

    ProcessQuery -->|Success| RQ
    RootTasks -->|Cache| RQ
    Subtasks -->|Cache| RQ
    CompleteTask -->|Invalidate| RQ
```

### Component Descriptions

- **App**: Root component managing the application layout and dialog state
- **TaskForm**: Handles both task creation and updates through natural language
- **TaskList**: Displays the hierarchy of tasks with expanding/collapsing capability
- **TaskItem**: Individual task display with completion and update actions

### State Management
- Uses React Query for server state management
- Caches task data and handles background updates
- Manages loading and error states automatically

### Key Features
- Natural language task creation
- Hierarchical task display
- Task completion toggling
- Task updates through conversation
- Automatic state synchronization

## Testing

This project uses Cypress for end-to-end testing. Before running the tests, make sure you have:

1. The backend Spring Boot application running
2. PostgreSQL database running
3. The frontend development server running (`npm run dev`)

### Running Tests

You can run the tests in two ways:

1. Interactive Mode (Recommended for development):
```bash
npm run cypress:open
```
This will open the Cypress Test Runner where you can:
- Select "E2E Testing"
- Choose your preferred browser
- Click on "task-management.cy.ts" to run the tests

2. Headless Mode (Good for CI/CD):
```bash
npm run test:e2e
```
This will run all tests in headless mode and show the results in the terminal.

### Test Files

- `cypress/e2e/task-management.cy.ts`: Contains tests for:
  - Task creation and updates
  - Task completion
  - Task refinement conversation

## Development Setup
