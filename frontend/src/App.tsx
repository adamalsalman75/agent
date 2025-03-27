import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { TaskList } from './components/TaskList';
import { TaskForm } from './components/TaskForm';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { Container, Typography, Box } from '@mui/material';

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
            <TaskList />
          </Container>
        </Box>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
