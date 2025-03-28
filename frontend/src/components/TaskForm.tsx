import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi, AIResponse, Task } from '../api/client';
import { 
  Paper, 
  TextField, 
  Button, 
  Box, 
  Typography, 
  Alert, 
  CircularProgress,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Chip,
} from '@mui/material';
import { 
  Send as SendIcon,
  Chat as ChatIcon,
  CheckCircle as CheckCircleIcon,
  Schedule as ScheduleIcon,
  Flag as FlagIcon,
  Info as InfoIcon
} from '@mui/icons-material';

interface ConversationState {
  messages: Array<{
    type: 'user' | 'assistant';
    content: string;
  }>;
  context?: any;
  requiresFollowUp?: boolean;
  resultTask?: Task;
}

interface TaskFormProps {
  activeTask?: Task;
  onClose?: () => void;
}

export const TaskForm = ({ activeTask, onClose }: TaskFormProps) => {
  const [query, setQuery] = useState('');
  const [conversation, setConversation] = useState<ConversationState>({
    messages: [],
    context: activeTask ? { inProgressTask: activeTask } : undefined
  });
  const queryClient = useQueryClient();

  const processQueryMutation = useMutation({
    mutationFn: ({ query, context }: { query: string; context?: any }) => 
      taskApi.processQuery(query, context),
    onSuccess: (data: AIResponse) => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      
      setConversation(prev => ({
        messages: [
          ...prev.messages,
          { type: 'user', content: query },
          { type: 'assistant', content: data.response }
        ],
        context: data.context,
        requiresFollowUp: data.requiresFollowUp,
        resultTask: data.resultTask
      }));

      if (!data.requiresFollowUp) {
        setQuery('');
        if (onClose && data.resultTask) {
          onClose();
        }
      }
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      processQueryMutation.mutate({ 
        query, 
        context: conversation.context 
      });
    }
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return null;
    return new Date(dateString).toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    });
  };

  return (
    <>
      <Paper 
        component="form" 
        onSubmit={handleSubmit}
        elevation={2}
        sx={{ p: 3, mb: 3 }}
      >
        {activeTask && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Updating task: {activeTask.description}
          </Alert>
        )}

        {conversation.messages.length > 0 && (
          <List sx={{ mb: 3 }}>
            {conversation.messages.map((msg, index) => (
              <ListItem
                key={index}
                sx={{
                  backgroundColor: msg.type === 'assistant' ? 'action.hover' : 'transparent',
                  borderRadius: 1,
                  mt: 1
                }}
              >
                <ListItemIcon>
                  {msg.type === 'assistant' ? <ChatIcon color="primary" /> : null}
                </ListItemIcon>
                <ListItemText
                  primary={msg.content}
                  sx={{
                    '& .MuiTypography-root': {
                      whiteSpace: 'pre-wrap',
                    }
                  }}
                />
              </ListItem>
            ))}
          </List>
        )}

        {conversation.resultTask && (
          <Alert 
            icon={<CheckCircleIcon />}
            severity="success"
            sx={{ mb: 2 }}
          >
            <Typography variant="subtitle1" gutterBottom>
              Task Created Successfully
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              {conversation.resultTask.deadline && (
                <Chip
                  size="small"
                  icon={<ScheduleIcon />}
                  label={formatDate(conversation.resultTask.deadline)}
                />
              )}
              {conversation.resultTask.priority && (
                <Chip
                  size="small"
                  icon={<FlagIcon />}
                  label={conversation.resultTask.priority}
                />
              )}
              {conversation.resultTask.constraints && (
                <Chip
                  size="small"
                  icon={<InfoIcon />}
                  label="Has Constraints"
                />
              )}
            </Box>
          </Alert>
        )}
        
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField
            fullWidth
            data-testid="task-input"
            label={conversation.requiresFollowUp 
              ? "Please provide more details" 
              : "Tell me what you need"}
            variant="outlined"
            size="medium"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={conversation.requiresFollowUp 
              ? "Provide additional information..." 
              : "e.g., Create a task to organize the conference"}
          />
          <Button
            type="submit"
            data-testid="submit-button"
            variant="contained"
            disabled={processQueryMutation.isPending}
            startIcon={processQueryMutation.isPending 
              ? <CircularProgress size={20} color="inherit" /> 
              : <SendIcon />}
            sx={{ px: 4 }}
          >
            {processQueryMutation.isPending ? 'Processing...' : 'Send'}
          </Button>
        </Box>
        
        {processQueryMutation.isError && (
          <Typography color="error" sx={{ mt: 2 }}>
            An error occurred while processing your request. Please try again.
          </Typography>
        )}
      </Paper>

      {conversation.requiresFollowUp && (
        <Alert severity="info" sx={{ mb: 3 }}>
          I'm helping you create a well-defined task. Please provide additional details as requested above.
        </Alert>
      )}
    </>
  );
};