import { GoogleLogin, CredentialResponse } from '@react-oauth/google';
import { useAuth } from '../context/AuthContext';
import { Box, Typography, Alert } from '@mui/material';
import { useState } from 'react';

export default function GoogleLoginButton() {
  const { login } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const handleSuccess = async (credentialResponse: CredentialResponse) => {
    if (credentialResponse.credential) {
      try {
        setError(null);
        await login(credentialResponse.credential);
      } catch (err) {
        console.error('Login failed:', err);
        setError('Login failed. Please try again.');
      }
    }
  };

  const handleError = () => {
    console.error('Google login failed');
    setError('Google login failed. Please try again.');
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 2,
        mt: 4,
      }}
    >
      <Typography variant="h4" component="h1" gutterBottom>
        Welcome to Fitness Tracker
      </Typography>
      <Typography variant="body1" color="text.secondary" gutterBottom>
        Track your food, exercise, and weight to meet your fitness goals.
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <GoogleLogin
        onSuccess={handleSuccess}
        onError={handleError}
        useOneTap
        theme="filled_blue"
        size="large"
        text="signin_with"
        shape="rectangular"
      />
    </Box>
  );
}
