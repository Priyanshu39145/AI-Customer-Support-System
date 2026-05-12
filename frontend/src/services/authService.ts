import api, {
  getAccessToken,
  getRefreshToken,

  clearTokens,


} from './api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    email: string;
    name: string;
    role: string;
  };
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

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  refreshToken: string;
}

/**
 * Auth service - handles all authentication operations.
 *
 * IMPORTANT: Token refresh is now centralized in api.ts via getValidAccessToken().
 * The authService.refresh() method is kept for backwards compatibility but should not
 * be used for normal token refresh - use getValidAccessToken() directly instead.
 *
 * This ensures only ONE refresh request ever runs, preventing MariaDB optimistic
 * locking errors that occurred when multiple concurrent requests tried to revoke
 * the same refresh token.
 */
export const authService = {
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await api.post('/auth/login', data);


    return response.data;
  },

  async register(data: RegisterRequest): Promise<LoginResponse> {
    // First register the user
    const registerResponse = await api.post('/auth/register', {
      name: data.name,
      email: data.email,
      password: data.password,
    });

    // If registration returns user directly without tokens, return the user info
    // Otherwise, login to get tokens
    if (registerResponse.data.accessToken && registerResponse.data.refreshToken) {


      return registerResponse.data;
    }

    // Backend returns user without tokens on registration, so we need to login
    const loginResponse = await api.post('/auth/login', {
      email: data.email,
      password: data.password,
    });


    return loginResponse.data;
  },

  /**
   * Refresh tokens using refresh token.
   *
   * NOTE: This method is kept for backwards compatibility.
   * For most use cases, you should use getValidAccessToken() from api.ts
   * which handles refresh automatically.
   *
   * @deprecated Use getValidAccessToken() from api.ts for token refresh
   */
//   async refresh(refreshToken: string): Promise<LoginResponse> {
//     const response = await api.post('/auth/refresh', { refreshToken });
//     const { accessToken, refreshToken: newRefreshToken, user } = response.data;
//
//     return response.data;
//   },

  async logout() {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      try {
        await api.post('/auth/logout', { refreshToken });
      } catch {
        // Ignore logout errors
      }
    }
    clearTokens();
  },

  isAuthenticated: (): boolean => {
    return !!getAccessToken() || !!getRefreshToken();
  },
};

export default authService;