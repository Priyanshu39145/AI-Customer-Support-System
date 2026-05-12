import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  MessageSquare,
  Ticket,
  Users,
  Shield,
  Menu,
  X,
  Settings,
  FileText,
  HeadphonesIcon,
} from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import clsx from 'clsx';

const userLinks = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/conversations', icon: MessageSquare, label: 'AI Chat' },
  { to: '/tickets', icon: Ticket, label: 'My Tickets' },
];

const agentLinks = [
  { to: '/agent/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/agent/tickets', icon: Ticket, label: 'Assigned Tickets' },
];

const adminLinks = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/admin/agents', icon: Users, label: 'Agents' },
  { to: '/admin/tickets', icon: Ticket, label: 'All Tickets' },
  { to: '/admin/company-policy', icon: FileText, label: 'Company Policy' },
  { to: '/admin/users', icon: Shield, label: 'Users' },
];

const roleLabels = {
  ADMIN: 'Admin Panel',
  AGENT: 'Agent Portal',
  USER: 'My Account',
};

export const Sidebar = () => {
  const { user } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();

  const role = user?.role || 'USER';
  const links = role === 'ADMIN' ? adminLinks : role === 'AGENT' ? agentLinks : userLinks;

  const SidebarContent = ({ mobile = false }: { mobile?: boolean }) => (
    <div className="flex flex-col h-full">
      {/* Logo / Brand */}
      <div className="p-4 border-b border-[var(--color-border)]">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center shadow-md">
            <HeadphonesIcon className="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-[var(--color-text)] leading-tight">
              Support Hub
            </h2>
            <p className="text-xs text-[var(--color-text-tertiary)]">
              {roleLabels[role as keyof typeof roleLabels]}
            </p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
        <div className="px-3 py-2">
          <span className="text-xs font-medium text-[var(--color-text-tertiary)] uppercase tracking-wider">
            Menu
          </span>
        </div>
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            onClick={() => setMobileOpen(false)}
            className={({ isActive }) =>
              clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 mx-2',
                isActive
                  ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400 font-medium'
                  : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-bg-tertiary)] hover:text-[var(--color-text)]'
              )
            }
          >
            <link.icon className="w-5 h-5 flex-shrink-0" />
            <span className="text-sm">{link.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Settings */}
      <div className="p-3 border-t border-[var(--color-border)]">
        <button
          onClick={() => navigate('/settings')}
          className="flex items-center gap-3 px-3 py-2.5 w-full rounded-lg text-[var(--color-text-secondary)] hover:bg-[var(--color-bg-tertiary)] hover:text-[var(--color-text)] transition-all duration-200 mx-2"
        >
          <Settings className="w-5 h-5" />
          <span className="text-sm">Settings</span>
        </button>
      </div>

      {/* User Info */}
      {user && (
        <div className="p-3 border-t border-[var(--color-border)]">
          <div className="flex items-center gap-3 px-3 py-2">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white text-xs font-medium">
              {user.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-[var(--color-text)] truncate">
                {user.name}
              </p>
              <p className="text-xs text-[var(--color-text-tertiary)] truncate">
                {user.email}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <>
      {/* Mobile Toggle Button */}
      <button
        onClick={() => setMobileOpen(true)}
        className="lg:hidden fixed top-4 left-4 z-50 p-2 rounded-lg bg-[var(--color-bg)] border border-[var(--color-border)] shadow-md hover:shadow-lg transition-shadow"
      >
        <Menu className="w-5 h-5 text-[var(--color-text)]" />
      </button>

      {/* Desktop Sidebar */}
      <aside className="hidden lg:flex w-64 flex-col fixed inset-y-0 left-0 bg-[var(--color-bg)] border-r border-[var(--color-border)]">
        <SidebarContent />
      </aside>

      {/* Mobile Sidebar */}
      {mobileOpen && (
        <div className="lg:hidden fixed inset-0 z-50">
          <div
            className="absolute inset-0 bg-black/50 backdrop-blur-sm"
            onClick={() => setMobileOpen(false)}
          />
          <div className="absolute inset-y-0 left-0 w-72 bg-[var(--color-bg)] animate-slide-in-right shadow-xl">
            <button
              onClick={() => setMobileOpen(false)}
              className="absolute top-4 right-4 p-2 rounded-lg hover:bg-[var(--color-bg-tertiary)] transition-colors"
            >
              <X className="w-5 h-5 text-[var(--color-text-secondary)]" />
            </button>
            <SidebarContent mobile />
          </div>
        </div>
      )}
    </>
  );
};