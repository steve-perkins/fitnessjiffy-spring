import { useEffect, useRef } from 'react';
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import GoogleLoginButton from './components/GoogleLoginButton';
import ProfilePage from './pages/ProfilePage';
import FoodPage from './pages/FoodPage';
import ExercisePage from './pages/ExercisePage';
import ReportsPage from './pages/ReportsPage';

function App() {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const prevAuthRef = useRef(isAuthenticated);

  // Navigate to profile when user logs in
  useEffect(() => {
    if (!prevAuthRef.current && isAuthenticated) {
      navigate('/profile', { replace: true });
    }
    prevAuthRef.current = isAuthenticated;
  }, [isAuthenticated, navigate]);

  // Show loading spinner while checking auth
  if (isLoading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  // Not authenticated - show login
  if (!isAuthenticated) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
        sx={{ backgroundColor: 'background.default' }}
      >
        <GoogleLoginButton />
      </Box>
    );
  }

  // Authenticated - show main app
  return (
    <Layout>
      <Routes>
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/food" element={<FoodPage />} />
        <Route path="/exercise" element={<ExercisePage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="*" element={<Navigate to="/profile" replace />} />
      </Routes>
    </Layout>
  );
}

export default App;
