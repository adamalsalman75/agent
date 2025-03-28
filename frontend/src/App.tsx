import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { TaskList } from './components/TaskList';
import { TaskForm } from './components/TaskForm';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { Container, Typography, Box, Dialog, DialogTitle, DialogContent, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useState } from 'react';
import { Task } from './types';

const queryClient = new QueryClient();

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    background: {
      default: '#f5f5f5',
    },
  },
});

function App() {
  const [selectedTask, setSelectedTask] = useState<Task | undefined>();
  const [isUpdateDialogOpen, setIsUpdateDialogOpen] = useState(false);

  const handleTaskSelect = (task: Task) => {
    setSelectedTask(task);
    setIsUpdateDialogOpen(true);
  };

  const handleDialogClose = () => {
    setSelectedTask(undefined);
    setIsUpdateDialogOpen(false);
  };

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Box sx={{ minHeight: '100vh', py: 4 }}>
          <Container maxWidth="md">
            <Typography 
              variant="h3" 
              component="h1" 
              sx={{ mb: 4, textAlign: 'center', color: 'primary.main' }}
            >
              Task Manager
            </Typography>
            <TaskForm />
            <TaskList onTaskSelect={handleTaskSelect} />
          </Container>
        </Box>

        <Dialog 
          open={isUpdateDialogOpen} 
          onClose={handleDialogClose}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>
            Update Task
            <IconButton
              aria-label="close"
              onClick={handleDialogClose}
              sx={{ position: 'absolute', right: 8, top: 8 }}
            >
              <CloseIcon />
            </IconButton>
          </DialogTitle>
          <DialogContent>
            <TaskForm 
              activeTask={selectedTask} 
              onClose={handleDialogClose}
            />
          </DialogContent>
        </Dialog>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
