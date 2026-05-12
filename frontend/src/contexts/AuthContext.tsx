import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import authService, { LoginRequest, RegisterRequest } from '@/services/authService';
import {
  clearTokens,
  getRefreshToken,
  setTokens,
  setUserRole,
  setUserData,
  getUserData as getStoredUserData,
  checkAuthFailure,
} from '@/services/api';

interface User {
  id: string;
  email: string;
  name: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isHydrated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Restore auth state from localStorage.
 * This is synchronous - no API calls needed.
 */
const restoreAuthState = async (): Promise<{ user: User | null; hasTokens: boolean }> => {
  const refreshToken = getRefreshToken();

  // Check for forced logout
  if (checkAuthFailure()) {
    return { user: null, hasTokens: false };
  }

  if (!refreshToken) {
    return { user: null, hasTokens: false };
  }

  // Restore user from localStorage
  const storedUserData = getStoredUserData() as User | null;

  if (storedUserData) {
    return { user: storedUserData, hasTokens: true };
  }

  // Have refresh token but no user - tokens might still be valid
  return { user: null, hasTokens: true };
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isHydrated, setIsHydrated] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  /**
   * Auth initialization - restore state from localStorage.
   *
   * PRODUCTION FIX #2: Non-blocking hydration
   * - NO api calls - restore from localStorage
   * - Tokens in localStorage = authenticated
   * - First API call handles refresh if needed
   * - User in localStorage = can render immediately
   */
  useEffect(() => {
    const initAuth = async () => {
      try {
        const { user: storedUser} = await restoreAuthState();

        if (storedUser) {
          setUser(storedUser);
        }
      } finally {
        setIsHydrated(true);
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

    const login = async (data: LoginRequest) => {
      const response = await authService.login(data);

      setTokens(response.accessToken, response.refreshToken);
      setUserRole(response.user.role);
      setUserData(response.user);

      setUser(response.user);

      navigateToDashboard(response.user.role);
    };

const register = async (data: RegisterRequest) => {
  await authService.register(data);

  navigate('/login', { replace: true });
};

  const logout = async () => {
    // Clear tokens first so interceptor doesn't try to refresh after logout
    clearTokens();
    setUser(null);

    try {
      await authService.logout();
    } catch {
      // Ignore logout errors - tokens cleared locally
    }

    navigate('/login', { state: { from: location }, replace: true });
  };

  const navigateToDashboard = (role: string) => {
    const from = location.state?.from?.pathname;
    if (from && from !== '/login' && from !== '/register') {
      navigate(from, { replace: true });
      return;
    }

    switch (role) {
      case 'ADMIN':
        navigate('/admin/dashboard', { replace: true });
        break;
      case 'AGENT':
        navigate('/agent/dashboard', { replace: true });
        break;
      default:
        navigate('/dashboard', { replace: true });
    }
  };

  /**
   * isAuthenticated:
   * - True if user is in context OR refresh token exists in localStorage
   * - This allows immediate rendering after hydration
   * - First API call will handle token refresh if needed
   */
  const isAuthenticated = !!user || !!getRefreshToken();

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isHydrated,
        login,
        register,
        logout,
        isAuthenticated,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};