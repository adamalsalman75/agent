import { useQuery } from '@tanstack/react-query';
import { Task, taskApi } from '../api/client';
import { 
  Paper, 
  List, 
  ListItem, 
  ListItemText, 
  Typography, 
  CircularProgress,
  Box,
  IconButton
} from '@mui/material';
import { 
  CheckCircle as CheckCircleIcon,
  RadioButtonUnchecked as UncheckedIcon
} from '@mui/icons-material';

export const TaskList = () => {
  const { data: tasks, isLoading } = useQuery<Task[]>({
    queryKey: ['tasks'],
    queryFn: taskApi.getTasks,
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!tasks?.length) {
    return (
      <Paper sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="text.secondary">
          No tasks yet. Add one to get started!
        </Typography>
      </Paper>
    );
  }

  return (
    <Paper elevation={2}>
      <List sx={{ p: 0 }}>
        {tasks.map((task, index) => (
          <ListItem
            key={task.id}
            divider={index < tasks.length - 1}
            secondaryAction={
              <Typography variant="caption" color="text.secondary">
                {new Date(task.createdAt).toLocaleDateString()}
              </Typography>
            }
          >
            <IconButton edge="start" sx={{ mr: 2, color: task.completed ? 'success.main' : 'action.disabled' }}>
              {task.completed ? <CheckCircleIcon /> : <UncheckedIcon />}
            </IconButton>
            <ListItemText
              primary={task.description}
              sx={{
                '& .MuiListItemText-primary': {
                  textDecoration: task.completed ? 'line-through' : 'none',
                  color: task.completed ? 'text.secondary' : 'text.primary',
                },
              }}
            />
          </ListItem>
        ))}
      </List>
    </Paper>
  );
};