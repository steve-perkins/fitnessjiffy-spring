import apiClient from './client';
import { Weight, CreateWeightDto, UpdateWeightDto } from '../types';

export const weightsApi = {
  getAll: async (): Promise<Weight[]> => {
    const response = await apiClient.get<Weight[]>('/weights');
    return response.data;
  },

  getOnDate: async (date: string): Promise<Weight | null> => {
    const response = await apiClient.get<Weight>('/weights/on-date', {
      params: { date },
    });
    return response.data;
  },

  create: async (data: CreateWeightDto): Promise<Weight> => {
    const response = await apiClient.post<Weight>('/weights', data);
    return response.data;
  },

  update: async (id: string, data: UpdateWeightDto): Promise<Weight> => {
    const response = await apiClient.patch<Weight>(`/weights/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/weights/${id}`);
  },
};
