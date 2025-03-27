import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { TaskList } from './components/TaskList';
import { TaskForm } from './components/TaskForm';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-3xl mx-auto p-6">
          <h1 className="text-3xl font-bold text-gray-900 mb-8">Task Manager</h1>
          <TaskForm />
          <TaskList />
        </div>
      </div>
    </QueryClientProvider>
  );
}

export default App;
