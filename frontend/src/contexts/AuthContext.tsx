import { createContext, ReactNode, useCallback, useContext, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import authService, { AuthenticatedUser, LoginRequest, RegisterRequest } from '@/services/authService';
import { ensureCsrfToken } from '@/services/api';

interface AuthContextType {
  user: AuthenticatedUser | null;
  isLoading: boolean;
  isHydrated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  completeOAuthLogin: () => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isHydrated, setIsHydrated] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const navigateToDashboard = useCallback((role: string) => {
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
  }, [location.state, navigate]);

  useEffect(() => {
    const initialize = async () => {
      try {
        await ensureCsrfToken();
        setUser(await authService.getCurrentUser());
      } catch {
        setUser(null);
      } finally {
        setIsHydrated(true);
        setIsLoading(false);
      }
    };

    const PUBLIC_ROUTES = ['/login', '/register', '/oauth2/callback'];

    const expireSession = () => {
      setUser(null);

      // Already on a public page?
      if (PUBLIC_ROUTES.includes(location.pathname)) {
        return;
      }

      navigate('/login', {
        replace: true,
        state: {
          reason: 'session_expired',
          from: location,
        },
      });
    };

    window.addEventListener('auth:expired', expireSession);
    void initialize();
    return () => window.removeEventListener('auth:expired', expireSession);
  }, [navigate, location]);

  const login = useCallback(async (data: LoginRequest) => {
    await authService.login(data);
    const currentUser = await authService.getCurrentUser();
    setUser(currentUser);
    navigateToDashboard(currentUser.role);
  }, [navigateToDashboard]);

  const register = useCallback(async (data: RegisterRequest) => {
    await authService.register(data);
    navigate('/login', { replace: true });
  }, [navigate]);

  const completeOAuthLogin = useCallback(async () => {
    const currentUser = await authService.getCurrentUser();
    setUser(currentUser);
    navigateToDashboard(currentUser.role);
  }, [navigateToDashboard]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
      navigate('/login', { state: { from: location }, replace: true });
    }
  }, [location, navigate]);

  return (
    <AuthContext.Provider value={{
      user,
      isLoading,
      isHydrated,
      login,
      register,
      completeOAuthLogin,
      logout,
      isAuthenticated: user !== null,
    }}>
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
