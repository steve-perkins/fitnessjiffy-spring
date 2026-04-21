import apiClient from './client';
import { AuthResponse } from '../types';

export const authApi = {
  /**
   * Exchange Google ID token for backend JWT
   */
  googleLogin: async (idToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/google', { idToken });
    return response.data;
  },

  /**
   * Get current user profile (also verifies token validity)
   */
  getCurrentUser: async () => {
    const response = await apiClient.get('/users/me');
    return response.data;
  },
};
