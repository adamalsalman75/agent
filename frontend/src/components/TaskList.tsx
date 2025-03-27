import { useQuery } from '@tanstack/react-query';
import { Task, taskApi } from '../api/client';
import { CheckIcon, XMarkIcon } from '@heroicons/react/20/solid';

export const TaskList = () => {
  const { data: tasks, isLoading } = useQuery<Task[]>({
    queryKey: ['tasks'],
    queryFn: taskApi.getTasks,
  });

  if (isLoading) {
    return <div className="text-center">Loading tasks...</div>;
  }

  if (!tasks?.length) {
    return <div className="text-center text-gray-500">No tasks found</div>;
  }

  return (
    <div className="mt-6">
      <h2 className="text-lg font-medium mb-4">Your Tasks</h2>
      <div className="space-y-2">
        {tasks.map((task) => (
          <div
            key={task.id}
            className="flex items-center justify-between p-4 bg-white rounded-lg shadow"
          >
            <div className="flex items-center">
              <span className="flex-shrink-0 w-5 h-5">
                {task.completed ? (
                  <CheckIcon className="w-5 h-5 text-green-500" aria-hidden="true" />
                ) : (
                  <XMarkIcon className="w-5 h-5 text-gray-400" aria-hidden="true" />
                )}
              </span>
              <span className={`ml-3 ${task.completed ? 'line-through text-gray-500' : 'text-gray-900'}`}>
                {task.description}
              </span>
            </div>
            <div className="text-sm text-gray-500">
              {new Date(task.createdAt).toLocaleDateString()}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};