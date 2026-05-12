/**
 * Centralized refresh token management with shared promise queue.
 *
 * PRODUCTION-GRADE Architecture:
 * - JWT expiration validation: Decode JWT payload and check exp claim
 * - Shared refreshPromise: Single in-flight refresh, all requestors wait on same promise
 * - Auth endpoint exclusion: Never retry auth/login/register/refresh endpoints
 * - React navigation: Use history API instead of hard reload
 * - Proper hydration: Restore user from localStorage without blocking API calls
 */

import axios, { InternalAxiosRequestConfig } from 'axios';


const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * Storage abstraction - supports both localStorage and sessionStorage
 */
const authStorage = (): Storage | null => {
  if (typeof window === 'undefined') return null;
  return window.localStorage;
};

// ============================================================================
// JWT Utilities
// ============================================================================

interface JWTPayload {
  exp: number;
  iat: number;
  sub?: string;
  email?: string;
  role?: string;
  [key: string]: unknown;
}

interface RetryAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

/**
 * Decode JWT payload without verification.
 * JWTs are URL-safe Base64 encoded, need proper decoding.
 */
const decodeJWT = (token: string): JWTPayload | null => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;

    // Decode the payload (middle part)
    const payload = parts[1];
    // Replace URL-safe chars and add padding
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const paddedBase64 = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const json = atob(paddedBase64);
    return JSON.parse(json);
  } catch {
    return null;
  }
};

/**
 * Check if JWT is expired or will expire soon.
 * @param token JWT to check
 * @param bufferSeconds Extra seconds to buffer (default 30s)
 */
const isTokenExpired = (token: string, bufferSeconds: number = 30): boolean => {
  const payload = decodeJWT(token);
  if (!payload || !payload.exp) return true;

  const now = Math.floor(Date.now() / 1000);
  // Consider expired if it expires within bufferSeconds
  return payload.exp <= (now + bufferSeconds);
};

// ============================================================================
// Token Storage Functions
// ============================================================================

export const getAccessToken = (): string | null => {
  return authStorage()?.getItem('accessToken') ?? null;
};

export const getRefreshToken = (): string | null => {
  return authStorage()?.getItem('refreshToken') ?? null;
};

export const setTokens = (accessToken: string, refreshToken: string) => {
  authStorage()?.setItem('accessToken', accessToken);
  authStorage()?.setItem('refreshToken', refreshToken);
};

export const clearTokens = () => {
  const storage = authStorage();
  storage?.removeItem('accessToken');
  storage?.removeItem('refreshToken');
  storage?.removeItem('userRole');
  storage?.removeItem('userData');
};

export const getUserRole = (): string | null => {
  return authStorage()?.getItem('userRole') ?? null;
};

export const setUserRole = (role: string) => {
  authStorage()?.setItem('userRole', role);
};

export const setUserData = (data: object) => {
  authStorage()?.setItem('userData', JSON.stringify(data));
};

export const getUserData = (): object | null => {
  const data = authStorage()?.getItem('userData');
  try {
     return data ? JSON.parse(data) : null;
  } catch {
     return null;
  }
};

// ============================================================================
// SHARED REFRESH TOKEN SYSTEM - Single Point of Truth
// ============================================================================

/**
 * The single in-flight refresh request promise.
 * ALL refresh operations must use this.
 */
let refreshPromise: Promise<string> | null = null;

/**
 * Queue of pending requests waiting for refresh.
 */
// interface PendingRequest {
//   resolve: (token: string) => void;
//   reject: (error: Error) => void;
// }
// let pendingRequests: PendingRequest[] = [];
//
// /**
//  * Process all pending requests after refresh completes.
//  */
// const processPendingRequests = (error: Error | null, newToken: string | null) => {
//   pendingRequests.forEach((req) => {
//     if (error) {
//       req.reject(error);
//     } else {
//       req.resolve(newToken!);
//     }
//   });
//   pendingRequests = [];
// };

/**
 * Execute refresh request to backend.
 * Uses raw axios with explicit baseURL to avoid interceptor loops.
 */
const executeRefresh = async (refreshToken: string): Promise<string> => {
  const response = await axios.post(
    `${API_BASE_URL}/auth/refresh`,
    { refreshToken },
    {
      baseURL: API_BASE_URL,
      timeout: 200000,
    }
  );

  const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data;
  setTokens(newAccessToken, newRefreshToken);
  return newAccessToken;
};

/**
 * Get a valid access token, refreshing if necessary.
 *
 * Flow:
 * 1. If valid token exists: return immediately
 * 2. If refresh in progress: wait for existing promise
 * 3. Otherwise: start new refresh (single source of truth)
 */
export const getValidAccessToken = async (): Promise<string> => {
  const accessToken = getAccessToken();

  // Return immediately if valid token exists
  if (accessToken && !isTokenExpired(accessToken, 30)) {
    return accessToken;
  }

  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  // If refresh already in progress, wait for it
  if (refreshPromise) {
    return refreshPromise;
  }

  // Start new refresh - single point of truth
    refreshPromise = (async () => {
      try {
        const newToken = await executeRefresh(refreshToken);
        return newToken;
      } catch (error) {
        clearTokens();
        throw error;
      } finally {
        refreshPromise = null;
      }
    })();

  return refreshPromise;
};

// ============================================================================
// Axios Instance with Interceptor
// ============================================================================

/**
 * Auth endpoints that should NOT trigger token refresh on 401.
 * These endpoints either don't need auth or use refresh tokens directly.
 */
const AUTH_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/logout',
  '/oauth2/authorization',
];

const shouldSkipRefresh = (url: string): boolean => {
  return AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint));
};

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 200000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  async (config) => {
    const url = config.url || '';
    // Skip auth logic for public/auth endpoints
    if (shouldSkipRefresh(url)) {
      return config;
    }
    try {
      const token = await getValidAccessToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch {
      // Allow request to continue without token
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response interceptor: Handle 401 and token refresh.
 * Uses getValidAccessToken() which handles: expired token -> refresh -> retry.
 */
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest =
      (error.config || {}) as RetryAxiosRequestConfig;
    const url = originalRequest.url || '';

    // Skip refresh for auth endpoints (they use refresh tokens)
    if (shouldSkipRefresh(url)) {
      return Promise.reject(error);
    }

    // Network/backend failure
    if (!error.response) {
      return Promise.reject(
        new Error('Backend server unreachable')
      );
    }

    // Handle 401: try token refresh then retry original request
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const newToken = await getValidAccessToken();
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // Store auth failure for AuthContext to detect
        if (typeof window !== 'undefined') {
          sessionStorage.setItem('auth_failed', 'true');
        }
        clearTokens();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

/**
 * Helper to get auth failure state and clear it.
 * Used by AuthContext/logout flow to detect forced logout.
 */
export const checkAuthFailure = (): boolean => {
  if (typeof window === 'undefined') {
    return false;
  }

  const failed = sessionStorage.getItem('auth_failed') === 'true';
  sessionStorage.removeItem('auth_failed');

  return failed;
};

export default api;