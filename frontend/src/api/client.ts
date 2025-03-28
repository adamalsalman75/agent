import axios from 'axios';

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface Task {
  id: number;
  description: string;
  completed: boolean;
  createdAt: string;
  completedAt: string | null;
  deadline: string | null;
  priority: string | null;
  constraints: string | null;
  parentId: number | null;
  metadata: any;
}

export interface AIResponse {
  response: string;
  requiresFollowUp: boolean;
  context?: any;
  resultTask?: Task;
}

export const taskApi = {
  getTasks: () => api.get<Task[]>('/tasks').then(res => res.data),
  getActiveTasks: () => api.get<Task[]>('/tasks/active').then(res => res.data),
  getRootTasks: () => api.get<Task[]>('/tasks/root').then(res => res.data),
  getSubtasks: (id: number) => api.get<Task[]>(`/tasks/${id}/subtasks`).then(res => res.data),
  getTasksByPriority: (priority: string) => api.get<Task[]>(`/tasks/priority/${priority}`).then(res => res.data),
  getOverdueTasks: () => api.get<Task[]>('/tasks/overdue').then(res => res.data),
  createTask: (task: Partial<Task>) => api.post<Task>('/tasks', task).then(res => res.data),
  completeTask: (id: number) => api.put<Task>(`/tasks/${id}/complete`).then(res => res.data),
  updateTask: (id: number, task: Partial<Task>) => api.put<Task>(`/tasks/${id}`, task).then(res => res.data),
  processQuery: (query: string, context?: any) =>
    api.post<AIResponse>('/query', { query, context }).then(res => res.data),
};