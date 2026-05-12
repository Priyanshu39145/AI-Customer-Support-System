import api from './api';

export interface Agent {
  id: string;
  name: string;
  email: string;
  categories: string[];
}

export interface AssignCategoriesRequest {
  categories: string[];
}

export const agentService = {

  async getAgents(): Promise<Agent[]> {
    const response = await api.get('/agents');
    return response.data;
  },

  async assignCategories(
    agentId: string,
    categories: string[]
  ): Promise<Agent> {

    const response = await api.put(
      `/agents/${agentId}/categories`,
      { categories }
    );

    return response.data;
  },

  async getAllAgents(): Promise<Agent[]> {
    const response = await api.get('/admin/agents');
    return response.data;
  },
};

export default agentService;