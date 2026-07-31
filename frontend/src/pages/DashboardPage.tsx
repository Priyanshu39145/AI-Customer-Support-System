import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from "react";
import { Ticket, MessageSquare, Clock, CheckCircle, AlertCircle, TrendingUp, ArrowRight, Plus } from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { useAuth } from '@/contexts/AuthContext';
import dashboardService from '@/services/dashboardService';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { useToast } from '@/components/Toast/ToastProvider';
import { Link } from 'react-router-dom';
import clsx from 'clsx';

const COLORS = ['#f59e0b', '#3b82f6', '#22c55e'];
const PIE_COLORS = ['#f59e0b', '#3b82f6', '#22c55e'];

interface StatCardProps {
  label: string;
  value: number | string;
  icon: React.ElementType;
  color: string;
  bgColor: string;
  trend?: string;
}

const StatCard = ({ label, value, icon: Icon, color, bgColor, trend }: StatCardProps) => (
  <div className="card p-5 hover:shadow-card-hover transition-all duration-300">
    <div className="flex items-start justify-between">
      <div className="flex-1">
        <p className="text-sm font-medium text-[var(--color-text-secondary)]">
          {label}
        </p>
        <p className="text-3xl font-bold text-[var(--color-text)] mt-2">
          {value}
        </p>
        {trend && (
          <p className="text-xs font-medium text-green-600 mt-2 flex items-center gap-1">
            <TrendingUp className="w-3 h-3" />
            {trend}
          </p>
        )}
      </div>
      <div className={clsx('p-3 rounded-xl', bgColor)}>
        <Icon className={clsx('w-6 h-6', color)} />
      </div>
    </div>
  </div>
);

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-[var(--color-bg)] border border-[var(--color-border)] rounded-lg shadow-lg p-3">
        <p className="text-sm font-medium text-[var(--color-text)]">{label}</p>
        <p className="text-sm text-[var(--color-text-secondary)]">
          {payload[0].name}: <span className="font-semibold">{payload[0].value}</span>
        </p>
      </div>
    );
  }
  return null;
};

export const DashboardPage = () => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const isAgent = user?.role === 'AGENT';
  const isAdmin = user?.role === 'ADMIN';

  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: () => dashboardService.getStats(),
    retry: 1,
  });

  useEffect(() => {
    if (error) {
      showToast('error', 'Failed to load dashboard stats');
    }
  }, [error, showToast]);

  if (isLoading) {
      return <PageLoader />;
  }

  const statCards = [
    {
      label: 'Total Tickets',
      value: stats?.totalTickets || 0,
      icon: Ticket,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50 dark:bg-blue-950/30',
    },
    {
      label: 'Open Tickets',
      value: stats?.openTickets || 0,
      icon: AlertCircle,
      color: 'text-amber-600',
      bgColor: 'bg-amber-50 dark:bg-amber-950/30',
    },
    {
      label: 'In Progress',
      value: stats?.inProgressTickets || 0,
      icon: Clock,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50 dark:bg-blue-950/30',
    },
    {
      label: 'Closed Tickets',
      value: stats?.closedTickets || 0,
      icon: CheckCircle,
      color: 'text-green-600',
      bgColor: 'bg-green-50 dark:bg-green-950/30',
    },
  ];

  const ticketData = [
    { name: 'Open', value: stats?.openTickets || 0 },
    { name: 'In Progress', value: stats?.inProgressTickets || 0 },
    { name: 'Closed', value: stats?.closedTickets || 0 },
  ];

  const resolutionRate = stats?.totalTickets
    ? Math.round((stats.closedTickets / stats.totalTickets) * 100)
    : 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <h1 className="page-title">
          Welcome back, {user?.name?.split(' ')[0]}!
        </h1>
        <p className="page-description">
          Here's an overview of your {isAdmin ? 'support operations' : isAgent ? 'assigned tickets' : 'support activities'}.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((stat, index) => (
          <div
            key={stat.label}
            className="animate-in"
            style={{ animationDelay: `${index * 50}ms` }}
          >
            <StatCard {...stat} />
          </div>
        ))}
      </div>

      {/* Charts Section */}
      {isAgent ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Pie Chart */}
          <div className="card p-6">
            <h2 className="text-lg font-semibold text-[var(--color-text)] mb-4">
              Ticket Distribution
            </h2>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={ticketData}
                    cx="50%"
                    cy="50%"
                    innerRadius={70}
                    outerRadius={100}
                    paddingAngle={6}
                    dataKey="value"
                  >
                    {ticketData.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={PIE_COLORS[index % PIE_COLORS.length]}
                      />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex justify-center gap-6 mt-4">
              {ticketData.map((item, index) => (
                <div key={item.name} className="flex items-center gap-2">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: PIE_COLORS[index] }}
                  />
                  <span className="text-sm text-[var(--color-text-secondary)]">
                    {item.name} ({item.value})
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Bar Chart */}
          <div className="card p-6">
            <h2 className="text-lg font-semibold text-[var(--color-text)] mb-4">
              Ticket Overview
            </h2>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={ticketData} barCategoryGap="20%">
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 12, fill: 'var(--color-text-secondary)' }}
                    axisLine={{ stroke: 'var(--color-border)' }}
                  />
                  <YAxis
                    tick={{ fontSize: 12, fill: 'var(--color-text-secondary)' }}
                    axisLine={{ stroke: 'var(--color-border)' }}
                  />
                  <Tooltip content={<CustomTooltip />} />
                  <Bar
                    dataKey="value"
                    fill="#0ea5e9"
                    radius={[6, 6, 0, 0]}
                    maxBarSize={60}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Resolution Rate Card */}
          <div className="card p-6 lg:col-span-2">
            <h2 className="text-lg font-semibold text-[var(--color-text)] mb-4">
              Performance Metrics
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-xl bg-[var(--color-bg-tertiary)]">
                <p className="text-sm text-[var(--color-text-secondary)]">Resolution Rate</p>
                <p className="text-2xl font-bold text-[var(--color-text)] mt-1">{resolutionRate}%</p>
              </div>
              <div className="p-4 rounded-xl bg-[var(--color-bg-tertiary)]">
                <p className="text-sm text-[var(--color-text-secondary)]">Total Conversations</p>
                <p className="text-2xl font-bold text-[var(--color-text)] mt-1">{stats?.totalConversations || 0}</p>
              </div>
              <div className="p-4 rounded-xl bg-[var(--color-bg-tertiary)]">
                <p className="text-sm text-[var(--color-text-secondary)]">Active Chats</p>
                <p className="text-2xl font-bold text-[var(--color-text)] mt-1">{stats?.activeConversations || 0}</p>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Conversations Card */}
          <div className="card p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-[var(--color-text)]">
                AI Conversations
              </h2>
              <Link
                to="/conversations"
                className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1"
              >
                View all <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 rounded-lg bg-[var(--color-bg-tertiary)]">
                <span className="text-sm text-[var(--color-text-secondary)]">Total Conversations</span>
                <span className="font-semibold text-[var(--color-text)]">{stats?.totalConversations || 0}</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg bg-[var(--color-bg-tertiary)]">
                <span className="text-sm text-[var(--color-text-secondary)]">Active</span>
                <span className="font-semibold text-[var(--color-text)]">{stats?.activeConversations || 0}</span>
              </div>
            </div>
            <Link
              to="/conversations"
              className="mt-4 btn-secondary w-full justify-center"
            >
              Start New Conversation
            </Link>
          </div>

          {/* Quick Actions */}
          <div className="card p-6">
            <h2 className="text-lg font-semibold text-[var(--color-text)] mb-4">
              Quick Actions
            </h2>
            <div className="space-y-3">
              <Link
                to="/conversations"
                className="flex items-center gap-3 p-3 rounded-lg bg-blue-50 dark:bg-blue-950/30 hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors group"
              >
                <div className="p-2 rounded-lg bg-blue-100 dark:bg-blue-900/50">
                  <MessageSquare className="w-5 h-5 text-blue-600 dark:text-blue-400" />
                </div>
                <span className="text-sm font-medium text-[var(--color-text)]">Start New Chat</span>
                <ArrowRight className="w-4 h-4 text-[var(--color-text-tertiary)] ml-auto group-hover:translate-x-1 transition-transform" />
              </Link>

              <Link
                to="/tickets/new"
                className="flex items-center gap-3 p-3 rounded-lg bg-green-50 dark:bg-green-950/30 hover:bg-green-100 dark:hover:bg-green-900/50 transition-colors group"
              >
                <div className="p-2 rounded-lg bg-green-100 dark:bg-green-900/50">
                  <Plus className="w-5 h-5 text-green-600 dark:text-green-400" />
                </div>
                <span className="text-sm font-medium text-[var(--color-text)]">Create Ticket</span>
                <ArrowRight className="w-4 h-4 text-[var(--color-text-tertiary)] ml-auto group-hover:translate-x-1 transition-transform" />
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};