import apiClient from './client';
import {
  Food,
  CreateFoodDto,
  UpdateFoodDto,
  FoodEaten,
  CreateFoodEatenDto,
  UpdateFoodEatenDto,
} from '../types';

export const foodsApi = {
  // Foods
  getAll: async (): Promise<Food[]> => {
    const response = await apiClient.get<Food[]>('/foods');
    return response.data;
  },

  search: async (query: string): Promise<Food[]> => {
    const response = await apiClient.get<Food[]>('/foods/search', {
      params: { q: query },
    });
    return response.data;
  },

  create: async (data: CreateFoodDto): Promise<Food> => {
    const response = await apiClient.post<Food>('/foods', data);
    return response.data;
  },

  update: async (id: string, data: UpdateFoodDto): Promise<Food> => {
    const response = await apiClient.patch<Food>(`/foods/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/foods/${id}`);
  },

  // Foods Eaten
  getEaten: async (params: { date?: string; startDate?: string; endDate?: string }): Promise<FoodEaten[]> => {
    const response = await apiClient.get<FoodEaten[]>('/foods/eaten', { params });
    return response.data;
  },

  getRecentlyEaten: async (): Promise<Food[]> => {
    // Get foods eaten in the last 30 days and return unique foods
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    const response = await apiClient.get<FoodEaten[]>('/foods/eaten', {
      params: {
        startDate: thirtyDaysAgo.toISOString(),
        endDate: new Date().toISOString(),
      },
    });

    // Extract unique foods
    const foodMap = new Map<string, Food>();
    response.data.forEach((eaten) => {
      if (!foodMap.has(eaten.food.id)) {
        foodMap.set(eaten.food.id, eaten.food);
      }
    });

    return Array.from(foodMap.values());
  },

  addEaten: async (data: CreateFoodEatenDto): Promise<FoodEaten> => {
    const response = await apiClient.post<FoodEaten>('/foods/eaten', data);
    return response.data;
  },

  updateEaten: async (id: string, data: UpdateFoodEatenDto): Promise<FoodEaten> => {
    const response = await apiClient.patch<FoodEaten>(`/foods/eaten/${id}`, data);
    return response.data;
  },

  deleteEaten: async (id: string): Promise<void> => {
    await apiClient.delete(`/foods/eaten/${id}`);
  },
};
