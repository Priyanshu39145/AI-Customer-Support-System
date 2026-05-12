import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { ProtectedRoute, RoleGuard } from '@/components/Route/ProtectedRoute';
import { MainLayout } from '@/layouts/MainLayout';
import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { OAuth2CallbackPage } from '@/pages/auth/OAuth2CallbackPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage';
import { AdminAgentsPage } from '@/pages/admin/AdminAgentsPage';
import { AgentCategoriesPage } from '@/pages/admin/AgentCategoriesPage';
import { TicketsPage } from '@/pages/TicketsPage';
import { TicketDetailPage } from '@/pages/TicketDetailPage';
import { CreateTicketPage } from '@/pages/CreateTicketPage';
import { ConversationsPage } from '@/pages/ConversationsPage';
import { ChatPage } from '@/pages/ChatPage';
import { AgentTicketsPage } from '@/pages/agent/AgentTicketsPage';
import { AgentTicketDetailPage } from '@/pages/agent/AgentTicketDetailPage';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { getRefreshToken } from '@/services/api';
import UploadCompanyPolicyPage from '@/pages/Admin/UploadCompanyPolicyPage';
import { UsersPage } from '@/pages/Admin/UsersPage';

const AgentDashboardPage = () => <DashboardPage />;

/**
 * App root component.
 *
 * PRODUCTION FIX #2: Proper Hydration Flow
 * - Show loading ONLY while isHydrated is false (restoring from localStorage)
 * - Once hydrated: show app immediately
 * - If user is null but refreshToken exists: allow render
 *   First API call will fetch user data
 * - ProtectedRoute handles actual auth checks
 */
function App() {
  const { isLoading, isHydrated, user } = useAuth();

  // Phase 1: Still hydrating from localStorage
  // This is very fast - just reading from storage
  if (!isHydrated || isLoading) {
    return <PageLoader />;
  }

  // Phase 2: Hydrated but still fetching user data
  // Only show loading if we have tokens but no user data yet
  // This handles edge case where user data wasn't stored but tokens exist
  // First API call will populate user data


  // Only block rendering if we have tokens but no user AND we haven't tried to load user yet
  // Otherwise render the app - interceptor will handle auth on API calls
  // For now, render user as null but authenticated (so protected routes work)

  // Get default dashboard based on role
  const getDefaultDashboard = () => {
    if (!user) {
      // No user yet but might have tokens - go to user dashboard
      // First API call will fetch user details
      return '/dashboard';
    }
    switch (user.role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'AGENT':
        return '/agent/dashboard';
      default:
        return '/dashboard';
    }
  };

  return (
    <Routes>
      {/* Public routes - accessible without auth */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

      {/* Protected routes wrapped in layout */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Root redirects to role-appropriate dashboard */}
        <Route path="/" element={<Navigate to={getDefaultDashboard()} replace />} />

        {/* USER routes */}
        <Route
          path="dashboard"
          element={
            <RoleGuard allowedRoles={['USER']}>
              <DashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="conversations"
          element={
            <RoleGuard allowedRoles={['USER', 'AGENT', 'ADMIN']}>
              <ConversationsPage />
            </RoleGuard>
          }
        />
        <Route
          path="chat"
          element={
            <RoleGuard allowedRoles={['USER', 'AGENT', 'ADMIN']}>
              <ChatPage />
            </RoleGuard>
          }
        />
        <Route
          path="chat/:conversationId"
          element={
            <RoleGuard allowedRoles={['USER', 'AGENT', 'ADMIN']}>
              <ChatPage />
            </RoleGuard>
          }
        />
        <Route
          path="tickets"
          element={
            <RoleGuard allowedRoles={['USER']}>
              <TicketsPage />
            </RoleGuard>
          }
        />
        <Route
          path="tickets/new"
          element={
            <RoleGuard allowedRoles={['USER']}>
              <CreateTicketPage />
            </RoleGuard>
          }
        />
        <Route
          path="tickets/:ticketId"
          element={
            <RoleGuard allowedRoles={['USER']}>
              <TicketDetailPage />
            </RoleGuard>
          }
        />

        {/* AGENT routes */}
        <Route
          path="agent/dashboard"
          element={
            <RoleGuard allowedRoles={['AGENT', 'ADMIN']}>
              <AgentDashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="agent/tickets"
          element={
            <RoleGuard allowedRoles={['AGENT', 'ADMIN']}>
              <AgentTicketsPage />
            </RoleGuard>
          }
        />
        <Route
          path="agent/tickets/:ticketId"
          element={
            <RoleGuard allowedRoles={['AGENT', 'ADMIN']}>
              <AgentTicketDetailPage />
            </RoleGuard>
          }
        />

        {/* ADMIN routes */}
        <Route
          path="admin/dashboard"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <AdminDashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="admin/agents"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <AdminAgentsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/admin/company-policy"
          element={<UploadCompanyPolicyPage />}
        />
        <Route
          path="admin/agents/:agentId/categories"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <AgentCategoriesPage />
            </RoleGuard>
          }
        />
        <Route
          path="admin/tickets"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <TicketsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/admin/users"
          element={<UsersPage />}
        />
        <Route
          path="admin/tickets/:ticketId"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <TicketDetailPage />
            </RoleGuard>
          }
        />
      </Route>

      {/* Catch all - redirect to dashboard */}
      <Route
        path="*"
        element={
          <Navigate
            to={user ? getDefaultDashboard() : '/login'}
            replace
          />
        }
      />
    </Routes>
  );
}

export default App;