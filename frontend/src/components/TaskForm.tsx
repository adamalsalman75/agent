import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi } from '../api/client';

export const TaskForm = () => {
  const [description, setDescription] = useState('');
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: taskApi.createTask,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      setDescription('');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (description.trim()) {
      createMutation.mutate(description);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="task" className="block text-sm font-medium text-gray-700">
          New Task
        </label>
        <div className="mt-1 flex rounded-md shadow-sm">
          <input
            type="text"
            name="task"
            id="task"
            className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2"
            placeholder="Enter task description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <button
            type="submit"
            className="ml-3 inline-flex justify-center rounded-md border border-transparent bg-blue-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            disabled={createMutation.isPending}
          >
            Add Task
          </button>
        </div>
      </div>
    </form>
  );
};