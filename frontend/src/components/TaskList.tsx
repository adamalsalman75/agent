import { useState } from 'react';
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
  IconButton,
  Chip,
  Collapse,
  Alert
} from '@mui/material';
import { 
  CheckCircle as CheckCircleIcon,
  RadioButtonUnchecked as UncheckedIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Schedule as ScheduleIcon,
  Flag as FlagIcon,
  Info as InfoIcon
} from '@mui/icons-material';

export const TaskList = () => {
  const [expandedTasks, setExpandedTasks] = useState<number[]>([]);
  
  const { data: rootTasks, isLoading: isLoadingRoot } = useQuery<Task[]>({
    queryKey: ['tasks', 'root'],
    queryFn: taskApi.getRootTasks,
  });

  const toggleExpand = (taskId: number) => {
    setExpandedTasks(prev => 
      prev.includes(taskId) 
        ? prev.filter(id => id !== taskId)
        : [...prev, taskId]
    );
  };

  const TaskItem = ({ task, depth = 0 }: { task: Task; depth?: number }) => {
    const isExpanded = expandedTasks.includes(task.id);
    
    const { data: subtasks } = useQuery<Task[]>({
      queryKey: ['tasks', task.id, 'subtasks'],
      queryFn: () => taskApi.getSubtasks(task.id),
      enabled: isExpanded,
    });

    const formatDate = (dateString: string | null) => {
      if (!dateString) return null;
      return new Date(dateString).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric'
      });
    };

    const isOverdue = task.deadline && new Date(task.deadline) < new Date() && !task.completed;

    return (
      <>
        <ListItem
          sx={{ pl: 2 + depth * 3 }}
          secondaryAction={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {task.deadline && (
                <Chip
                  size="small"
                  icon={<ScheduleIcon />}
                  label={formatDate(task.deadline)}
                  color={isOverdue ? "error" : "default"}
                />
              )}
              {task.priority && (
                <Chip
                  size="small"
                  icon={<FlagIcon />}
                  label={task.priority}
                  color={task.priority === 'HIGH' ? "error" : 
                         task.priority === 'MEDIUM' ? "warning" : "info"}
                />
              )}
            </Box>
          }
        >
          <IconButton 
            edge="start" 
            sx={{ mr: 2, color: task.completed ? 'success.main' : 'action.disabled' }}
            onClick={() => taskApi.completeTask(task.id)}
          >
            {task.completed ? <CheckCircleIcon /> : <UncheckedIcon />}
          </IconButton>
          
          <ListItemText
            primary={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography
                  sx={{
                    textDecoration: task.completed ? 'line-through' : 'none',
                    color: task.completed ? 'text.secondary' : 'text.primary',
                  }}
                >
                  {task.description}
                </Typography>
              </Box>
            }
            secondary={task.constraints}
          />
          
          {subtasks && subtasks.length > 0 && (
            <IconButton onClick={() => toggleExpand(task.id)}>
              {isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </IconButton>
          )}
        </ListItem>

        {task.constraints && (
          <Collapse in={isExpanded}>
            <Alert 
              severity="info" 
              icon={<InfoIcon />}
              sx={{ mx: 2, mb: 1, mt: -1 }}
            >
              {task.constraints}
            </Alert>
          </Collapse>
        )}

        {isExpanded && subtasks && (
          <Collapse in={isExpanded}>
            <List disablePadding>
              {subtasks.map(subtask => (
                <TaskItem 
                  key={subtask.id} 
                  task={subtask} 
                  depth={depth + 1} 
                />
              ))}
            </List>
          </Collapse>
        )}
      </>
    );
  };

  if (isLoadingRoot) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!rootTasks?.length) {
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
        {rootTasks.map(task => (
          <TaskItem key={task.id} task={task} />
        ))}
      </List>
    </Paper>
  );
};