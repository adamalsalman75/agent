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
}

export const taskApi = {
  getTasks: () => api.get<Task[]>('/tasks').then(res => res.data),
  getActiveTasks: () => api.get<Task[]>('/tasks/active').then(res => res.data),
  createTask: (description: string) => 
    api.post<Task>('/tasks', { description }).then(res => res.data),
  completeTask: (id: number) => 
    api.put<Task>(`/tasks/${id}/complete`).then(res => res.data),
  processQuery: (query: string) =>
    api.post('/query', { query }).then(res => res.data),
};