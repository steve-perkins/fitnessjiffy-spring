import apiClient from './client';
import { User, UpdateProfileDto } from '../types';

export const usersApi = {
  getProfile: async (): Promise<User> => {
    const response = await apiClient.get<User>('/users/profile');
    return response.data;
  },

  updateProfile: async (data: UpdateProfileDto): Promise<User> => {
    const response = await apiClient.patch<User>('/users/profile', data);
    return response.data;
  },
};
