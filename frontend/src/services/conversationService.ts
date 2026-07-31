import api from './api';

export interface Conversation {
  conversationId: string;
  conversationTitle: string;
}

// export interface CreateConversationRequest {
//   title?: string;
// }

export const conversationService = {
  async getConversations(): Promise<Conversation[]> {
    const response = await api.get('/conversations');
    return response.data;
  },


  async searchConversations(keyword: string): Promise<Conversation[]> {
    const response = await api.get('/conversations/search', { params: { keyword } });
    return response.data;
  },

  async renameConversation(conversationId: string, title: string): Promise<Conversation> {
    const response = await api.put(`/conversations/${conversationId}/title`, { title });
    return response.data;
  },

  async closeConversation(conversationId: string): Promise<Conversation> {
    const response = await api.put(`/conversations/${conversationId}/close`);
    return response.data;
  },

  async deleteConversation(conversationId: string, permanent: boolean = false): Promise<void> {
    await api.delete(`/conversations/${conversationId}`, { params: { permanent } });
  },

  async getMessages(conversationId: string): Promise<Message[]> {
    const response = await api.get(`/messages/${conversationId}`);
    return response.data;
  },

  async sendMessage(message: string, conversationId?: string): Promise<AIResponse> {
    const url = conversationId
      ? `/api/chat/${conversationId}`
      : '/api/chat';

    const response = await api.post(url, message, {
      headers: {
        'Content-Type': 'text/plain',
      },
    });

    return response.data;
  }


};

export interface Message {
  id: string;
  content: string;
  senderType: 'USER' | 'AGENT' | 'AI';
  userId: string;
  createdAt: string;
}

export interface AIResponse {
  userMessage: string;
  aiResponse: string;
  conversationId: string;
  timestamp: string;
}

export default conversationService;