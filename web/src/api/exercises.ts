import apiClient from './client';
import {
  Exercise,
  ExercisePerformed,
  CreateExercisePerformedDto,
  UpdateExercisePerformedDto,
} from '../types';

export const exercisesApi = {
  // Exercises
  getCategories: async (): Promise<string[]> => {
    const response = await apiClient.get<string[]>('/exercises/categories');
    return response.data;
  },

  getByCategory: async (category: string): Promise<Exercise[]> => {
    const response = await apiClient.get<Exercise[]>('/exercises/by-category', {
      params: { category },
    });
    return response.data;
  },

  search: async (query: string): Promise<Exercise[]> => {
    const response = await apiClient.get<Exercise[]>('/exercises/search', {
      params: { q: query },
    });
    return response.data;
  },

  // Exercises Performed
  getPerformed: async (params: { date?: string; startDate?: string; endDate?: string }): Promise<ExercisePerformed[]> => {
    const response = await apiClient.get<ExercisePerformed[]>('/exercises/performed', { params });
    return response.data;
  },

  getRecentlyPerformed: async (): Promise<Exercise[]> => {
    // Get exercises performed in the last 30 days and return unique exercises
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    const response = await apiClient.get<ExercisePerformed[]>('/exercises/performed', {
      params: {
        startDate: thirtyDaysAgo.toISOString(),
        endDate: new Date().toISOString(),
      },
    });

    // Extract unique exercises
    const exerciseMap = new Map<string, Exercise>();
    response.data.forEach((performed) => {
      if (!exerciseMap.has(performed.exercise.id)) {
        exerciseMap.set(performed.exercise.id, performed.exercise);
      }
    });

    return Array.from(exerciseMap.values());
  },

  addPerformed: async (data: CreateExercisePerformedDto): Promise<ExercisePerformed> => {
    const response = await apiClient.post<ExercisePerformed>('/exercises/performed', data);
    return response.data;
  },

  updatePerformed: async (id: string, data: UpdateExercisePerformedDto): Promise<ExercisePerformed> => {
    const response = await apiClient.patch<ExercisePerformed>(`/exercises/performed/${id}`, data);
    return response.data;
  },

  deletePerformed: async (id: string): Promise<void> => {
    await apiClient.delete(`/exercises/performed/${id}`);
  },
};
