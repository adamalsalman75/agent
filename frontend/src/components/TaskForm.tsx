import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi, AIResponse } from '../api/client';
import { Paper, TextField, Button, Box, Typography, Alert } from '@mui/material';
import { Send as SendIcon } from '@mui/icons-material';

export const TaskForm = () => {
  const [query, setQuery] = useState('');
  const [conversation, setConversation] = useState<{
    response?: string;
    context?: any;
    requiresFollowUp?: boolean;
  }>({});
  const queryClient = useQueryClient();

  const processQueryMutation = useMutation({
    mutationFn: ({ query, context }: { query: string; context?: any }) => 
      taskApi.processQuery(query, context),
    onSuccess: (data: AIResponse) => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      setConversation({
        response: data.response,
        context: data.context,
        requiresFollowUp: data.requiresFollowUp
      });
      if (!data.requiresFollowUp) {
        setQuery('');
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

  return (
    <Paper 
      component="form" 
      onSubmit={handleSubmit}
      elevation={2}
      sx={{ p: 3, mb: 3 }}
    >
      {conversation.response && (
        <Alert 
          severity={conversation.requiresFollowUp ? "info" : "success"} 
          sx={{ mb: 2 }}
        >
          {conversation.response}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', gap: 2 }}>
        <TextField
          fullWidth
          label={conversation.requiresFollowUp ? "Please provide more details" : "Tell me what you need"}
          variant="outlined"
          size="medium"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={conversation.requiresFollowUp 
            ? "Provide additional information..." 
            : "e.g., Create a task to buy groceries"}
        />
        <Button
          type="submit"
          variant="contained"
          disabled={processQueryMutation.isPending}
          startIcon={<SendIcon />}
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
  );
};