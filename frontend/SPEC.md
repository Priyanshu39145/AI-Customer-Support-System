# Frontend Specification - AI Customer Support System

## Tech Stack
- React 18 + Vite
- Tailwind CSS
- React Router 6
- Axios + React Query (TanStack Query)
- React Hook Form + Zod
- Recharts for charts
- React Markdown for markdown rendering

## Backend API Base URL
`http://localhost:8080`

## Authentication
- JWT tokens stored in localStorage
- Access token in memory + localStorage
- Refresh token flow via /auth/refresh
- Auto-refresh on 401, logout if refresh fails

## Roles
- USER
- AGENT  
- ADMIN

## Pages & Routes

### Public
- `/login` - Login page
- `/register` - Registration page

### Protected (USER, AGENT, ADMIN)
- `/` - Role-based dashboard redirect
- `/dashboard` - Dashboard stats
- `/conversations` - AI chat conversations list
- `/chat/:conversationId` - Chat interface

### USER Only
- `/tickets` - View/create tickets
- `/tickets/:ticketId` - Ticket details
- `/tickets/:ticketId/history` - Ticket history

### AGENT Only
- `/agent/tickets` - Assigned tickets management
- `/agent/tickets/:ticketId` - Ticket details with status control

### ADMIN Only
- `/admin/agents` - Manage agents
- `/admin/agents/:agentId/categories` - Assign categories
- `/admin/tickets` - All tickets management
- `/admin/tickets/:ticketId` - Ticket details with full control
- `/admin/tickets/:ticketId/assign` - Assign ticket to agent

### Shared
- All roles can access their dashboards andchat

## UI Components
- Sidebar navigation
- Top navbar with user menu
- TicketCard, TicketStatusBadge
- ChatMessage, ConversationCard
- Pagination, SearchBar, FilterDropdown
- LoadingSpinner, SkeletonLoader
- EmptyState, ConfirmDialog
- ToastProvider
- ProtectedRoute, RoleGuard

## Features
- Dark mode toggle
- Responsive design (mobile sidebar)
- Form validation with React Hook Form + Zod
- Optimistic updates with React Query
- Error boundaries
- Token refresh interceptor