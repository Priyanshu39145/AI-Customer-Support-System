import axios, { InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN';

interface RetryAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const readCookie = (name: string): string | null => {
  const prefix = `${name}=`;
  const cookie = document.cookie.split('; ').find((value) => value.startsWith(prefix));
  return cookie ? decodeURIComponent(cookie.substring(prefix.length)) : null;
};

const isUnsafeMethod = (method?: string) =>
  ['post', 'put', 'patch', 'delete'].includes((method || 'get').toLowerCase());

const isRefreshExcluded = (url: string) => [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/logout',
  '/auth/csrf',

  // Session probe - do not try refresh
  '/api/auth/me',
].some((endpoint) => url.includes(endpoint));

let csrfPromise: Promise<void> | null = null;

/** Obtains the non-HttpOnly CSRF token required for cookie-authenticated writes. */
// export const ensureCsrfToken = async (): Promise<void> => {
//   if (readCookie(CSRF_COOKIE_NAME)) {
//     return;
//   }
//
//   if (!csrfPromise) {
//     csrfPromise = axios.get(`${API_BASE_URL}/auth/csrf`, {
//       withCredentials: true,
//       timeout: 200000,
//     }).then(() => undefined).finally(() => {
//       csrfPromise = null;
//     });
//   }
//
//   return csrfPromise;
// };

export const ensureCsrfToken = async (): Promise<void> => {
  return;
};

// const csrfHeaders = (): Record<string, string> => {
//   const token = readCookie(CSRF_COOKIE_NAME);
//   return token ? { [CSRF_HEADER_NAME]: token } : {};
// };

const csrfHeaders = (): Record<string, string> => ({});

let refreshPromise: Promise<void> | null = null;

const refreshSession = async (): Promise<void> => {
  if (!refreshPromise) {
    refreshPromise = ensureCsrfToken()
      .then(() => axios.post(`${API_BASE_URL}/auth/refresh`, undefined, {
        withCredentials: true,
        headers: csrfHeaders(),
        timeout: 200000,
      }))
      .then(() => undefined)
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
};

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 200000,
  withCredentials: true,
});

// api.interceptors.request.use((config) => {
//   if (isUnsafeMethod(config.method)) {
//     const token = readCookie(CSRF_COOKIE_NAME);
//     if (token) {
//       config.headers.set(CSRF_HEADER_NAME, token);
//     }
//   }
//   return config;
// });

api.interceptors.request.use((config) => config);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = (error.config || {}) as RetryAxiosRequestConfig;
    const url = originalRequest.url || '';

    if (!error.response || isRefreshExcluded(url) || error.response.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;
    try {
      await refreshSession();
      return api(originalRequest);
    } catch (refreshError) {

      // Only notify for protected requests.
      if (!originalRequest.url?.includes('/api/auth/me')) {
        window.dispatchEvent(new Event('auth:expired'));
      }

      return Promise.reject(refreshError);
    }
  },
);

export default api;
