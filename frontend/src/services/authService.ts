import api, { ensureCsrfToken } from './api';

export interface AuthenticatedUser {
  id: string;
  email: string;
  name: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  user: AuthenticatedUser;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface RegisterResponse {
  name: string;
  email: string;
  role: string;
  enabled: boolean;
}

export const authService = {
  async login(data: LoginRequest): Promise<AuthenticatedUser> {
    await ensureCsrfToken();
    const response = await api.post<LoginResponse>('/auth/login', data);
    return response.data.user;
  },

  async register(data: RegisterRequest): Promise<RegisterResponse> {
    await ensureCsrfToken();
    const response = await api.post<RegisterResponse>('/auth/register', data);
    return response.data;
  },

  async getCurrentUser(): Promise<AuthenticatedUser> {
    const response = await api.get<AuthenticatedUser>('/api/auth/me');
    return response.data;
  },

  async logout(): Promise<void> {
    await ensureCsrfToken();
    await api.post('/auth/logout');
  },
};

export default authService;
