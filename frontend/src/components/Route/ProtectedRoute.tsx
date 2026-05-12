import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { PageLoader } from '@/components/UI/LoadingSpinner';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

/**
 * Protected Route component.
 *
 * Handles route protection with proper auth hydration.
 */
export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isAuthenticated, isLoading, isHydrated } = useAuth();
  const location = useLocation();

  // Phase 1: Still hydrating from localStorage
  if (!isHydrated) {
    return <PageLoader />;
  }

  // Phase 2: Still doing auth operations (login, logout)
  if (isLoading) {
    return <PageLoader />;
  }

  // Phase 3: Not authenticated - redirect to login
  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        state={{ from: location, reason: 'session_expired' }}
        replace
      />
    );
  }

  // Authenticated - render protected content
  return <>{children}</>;
};

interface RoleGuardProps {
  children: React.ReactNode;
  allowedRoles: string[];
}

/**
 * Role Guard component.
 *
 * Restricts access to specific roles.
 */
export const RoleGuard = ({ children, allowedRoles }: RoleGuardProps) => {
  const { user, isHydrated } = useAuth();

  // Still restoring auth state
  if (!isHydrated) {
    return <PageLoader />;
  }

  // User not loaded yet
  if (!user) {
    return <PageLoader />;
  }

  // Wrong role
  if (!allowedRoles.includes(user.role)) {
    switch (user.role) {
      case 'ADMIN':
        return <Navigate to="/admin/dashboard" replace />;
      case 'AGENT':
        return <Navigate to="/agent/dashboard" replace />;
      default:
        return <Navigate to="/dashboard" replace />;
    }
  }

  return <>{children}</>;
};