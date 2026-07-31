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
import { UploadCompanyPolicyPage } from '@/pages/admin/UploadCompanyPolicyPage';
import { UsersPage } from '@/pages/admin/UsersPage';

const AgentDashboardPage = () => <DashboardPage />;

/**
 * App root component.
 *
 * The auth provider hydrates from the protected /api/auth/me endpoint.
 * - ProtectedRoute handles actual auth checks
 */
function App() {
  const { isLoading, isHydrated, user } = useAuth();

  if (!isHydrated || isLoading) {
    return <PageLoader />;
  }

  // Get default dashboard based on role
  const getDefaultDashboard = () => {
    if (!user) {
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
            <RoleGuard allowedRoles={['USER']}>
              <ConversationsPage />
            </RoleGuard>
          }
        />
        <Route
          path="chat"
          element={
            <RoleGuard allowedRoles={['USER']}>
              <ChatPage />
            </RoleGuard>
          }
        />
        <Route
          path="chat/:conversationId"
          element={
            <RoleGuard allowedRoles={['USER']}>
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
            <RoleGuard allowedRoles={['AGENT']}>
              <AgentDashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="agent/tickets"
          element={
            <RoleGuard allowedRoles={['AGENT']}>
              <AgentTicketsPage />
            </RoleGuard>
          }
        />
        <Route
          path="agent/tickets/:ticketId"
          element={
            <RoleGuard allowedRoles={['AGENT']}>
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
            element={
                <RoleGuard allowedRoles={['ADMIN']}>
                    <UploadCompanyPolicyPage />
                </RoleGuard>
            }
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
          path="admin/users"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <UsersPage />
            </RoleGuard>
          }
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
