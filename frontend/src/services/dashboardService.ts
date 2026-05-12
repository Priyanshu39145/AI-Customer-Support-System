import api from './api';

export interface DashboardStats {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  closedTickets: number;
  totalConversations: number;
  activeConversations: number;
}

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    const response = await api.get('/dashboard/stats');

    return response.data;
  },
};

export default dashboardService;