import apiClient from './client';
import { ReportEntry } from '../types';

export const reportsApi = {
  getEntries: async (params?: { startDate?: string; endDate?: string }): Promise<ReportEntry[]> => {
    const response = await apiClient.get<ReportEntry[]>('/report-entries', { params });
    return response.data;
  },
};
