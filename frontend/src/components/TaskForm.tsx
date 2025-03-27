import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi } from '../api/client';
import { Paper, TextField, Button, Box } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';

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
    <Paper 
      component="form" 
      onSubmit={handleSubmit}
      elevation={2}
      sx={{ p: 3, mb: 3 }}
    >
      <Box sx={{ display: 'flex', gap: 2 }}>
        <TextField
          fullWidth
          label="New Task"
          variant="outlined"
          size="medium"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="What needs to be done?"
        />
        <Button
          type="submit"
          variant="contained"
          disabled={createMutation.isPending}
          startIcon={<AddIcon />}
          sx={{ px: 4 }}
        >
          {createMutation.isPending ? 'Adding...' : 'Add'}
        </Button>
      </Box>
    </Paper>
  );
};