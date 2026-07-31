import api from './api';

export type StatusType = 'OPEN' | 'IN_PROGRESS' | 'CLOSED';
export type PriorityType = 'LOW' | 'MEDIUM' | 'HIGH';
export type CategoryType =
  | 'GENERAL'
  | 'TECHNICAL'
  | 'BILLING'
  | 'DELIVERY'
  | 'ACCOUNT'
  | 'REFUND'
  | 'PAYMENT'
  | 'PRODUCT';

export interface TicketResponse {
  id: string;
  title: string;
  status: StatusType;
  priority: PriorityType;
  category: CategoryType;
  createdAt: string | null;
  updatedAt: string | null;
  createdById?: string;
  createdByName?: string;
  createdByEmail?: string;
  assignedToId?: string;
  assignedToName?: string;
  assignedToEmail?: string;
}

export interface TicketDetailedResponse extends TicketResponse {
  conversationId?: string;
}

export interface TicketActivityResponse {
    id: string;
  action: string;
  performedById: string;
  performedByName: string;
  oldValue?: string;
  newValue?: string;
  timestamp: string | null;
}

export interface TicketCommentResponse {
  id: string;
  content: string;
  ticketId?: string;
  authorId?: string;
  authorName?: string;
  authorRole?: string;
  createdAt: string | null;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  category?: CategoryType;
}

export interface PagedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

interface TicketParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: StatusType;
  priority?: PriorityType;
  category?: CategoryType;

  assignedToId?: string;
  createdFrom?: string;
  createdTo?: string;
}

export interface CreateTicketResponse {
    id: string;
    title: string;
    status: StatusType;
    priority: PriorityType;
    category: CategoryType;
}

export const ticketService = {
  // User endpoints
  async getMyTickets(params: TicketParams = {}): Promise<PagedResponse<TicketResponse>> {
    const response = await api.get('/users/me/tickets', { params });
    return response.data;
  },

  async getTicketById(ticketId: string): Promise<TicketDetailedResponse> {
    const response = await api.get(`/tickets/${ticketId}`);
    return response.data;
  },


  async getTicketHistory(ticketId: string): Promise<TicketActivityResponse[]> {
    const response = await api.get(`/tickets/${ticketId}/history`);
    return response.data;
  },

  async createTicket(data: CreateTicketRequest): Promise<CreateTicketResponse> {
    const response = await api.post('/tickets', data);
    return response.data;
  },

  async addComment(ticketId: string, content: string): Promise<TicketCommentResponse> {
    const response = await api.post(`/tickets/${ticketId}/comments`, { content });
    return response.data;
  },

  async getComments(ticketId: string): Promise<TicketCommentResponse[]> {
    const response = await api.get(`/tickets/${ticketId}/comments`);
    return response.data;
  },

  // Agent endpoints
  async getAssignedTickets(params: TicketParams = {}): Promise<PagedResponse<TicketResponse>> {
    const response = await api.get('/agents/me/tickets', { params });
    return response.data;
  },

  async changeStatus(ticketId: string, status: StatusType): Promise<TicketResponse> {
    const response = await api.put(`/tickets/${ticketId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  async changePriority(ticketId: string, priority: PriorityType): Promise<TicketResponse> {
    const response = await api.put(`/tickets/${ticketId}/priority`, null, {
      params: { priority },
    });
    return response.data;
  },

  async changeCategory(ticketId: string, category: CategoryType): Promise<TicketResponse> {
    const response = await api.put(`/tickets/${ticketId}/category`, null, {
      params: { category },
    });
    return response.data;
  },

  // Admin endpoints
  async getAllTickets(params: TicketParams = {}): Promise<PagedResponse<TicketResponse>> {
    const response = await api.get('/tickets', { params });
    return response.data;
  },

  async assignTicket(ticketId: string, agentId: string): Promise<TicketResponse> {
    const response = await api.put(`/tickets/${ticketId}/assign`, null, {
      params: { agentId },
    });
    return response.data;
  },
};

export default ticketService;