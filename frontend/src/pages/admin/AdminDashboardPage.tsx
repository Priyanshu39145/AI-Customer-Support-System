import { useQuery } from '@tanstack/react-query';
import { Ticket, Clock, CheckCircle, AlertCircle, TrendingUp, Users, Activity } from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell,
} from 'recharts';
import dashboardService from '@/services/dashboardService';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { useToast } from '@/components/Toast/ToastProvider';
import clsx from 'clsx';
import { useEffect, useState } from "react";

const PIE_COLORS = ['#f59e0b', '#3b82f6', '#22c55e'];

interface StatCardProps {
  label: string;
  value: number | string;
  icon: React.ElementType;
  color: string;
  bgColor: string;
}

const StatCard = ({ label, value, icon: Icon, color, bgColor }: StatCardProps) => (
  <div className="card p-5 hover:shadow-card-hover transition-all duration-300">
    <div className="flex items-start justify-between">
      <div className="flex-1">
        <p className="text-sm font-medium text-[var(--color-text-secondary)]">
          {label}
        </p>
        <p className="text-3xl font-bold text-[var(--color-text)] mt-2">
          {value}
        </p>
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

export const AdminDashboardPage = () => {
  const { showToast } = useToast();

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

  const ticketData = [
    { name: 'Open', value: stats?.openTickets || 0 },
    { name: 'In Progress', value: stats?.inProgressTickets || 0 },
    { name: 'Closed', value: stats?.closedTickets || 0 },
  ];

  const statCards = [
    { label: 'Total Tickets', value: stats?.totalTickets || 0, icon: Ticket, color: 'text-blue-600', bgColor: 'bg-blue-50 dark:bg-blue-950/30' },
    { label: 'Open Tickets', value: stats?.openTickets || 0, icon: AlertCircle, color: 'text-amber-600', bgColor: 'bg-amber-50 dark:bg-amber-950/30' },
    { label: 'In Progress', value: stats?.inProgressTickets || 0, icon: Clock, color: 'text-blue-600', bgColor: 'bg-blue-50 dark:bg-blue-950/30' },
    { label: 'Closed Tickets', value: stats?.closedTickets || 0, icon: CheckCircle, color: 'text-green-600', bgColor: 'bg-green-50 dark:bg-green-950/30' },
  ];

  const resolutionRate = stats?.totalTickets
    ? Math.round((stats.closedTickets / stats.totalTickets) * 100)
    : 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="page-header">
        <h1 className="page-title">Admin Dashboard</h1>
        <p className="page-description">
          Monitor system performance and ticket analytics
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((stat, index) => (
          <div key={stat.label} className="animate-in" style={{ animationDelay: `${index * 50}ms` }}>
            <StatCard {...stat} />
          </div>
        ))}
      </div>

      {/* Charts */}
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
                  {ticketData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={PIE_COLORS[index]} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="flex justify-center gap-6 mt-4">
            {ticketData.map((item, index) => (
              <div key={item.name} className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: PIE_COLORS[index] }} />
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
            Ticket Status Overview
          </h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={ticketData} barCategoryGap="20%">
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                <XAxis dataKey="name" tick={{ fontSize: 12, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} />
                <YAxis tick={{ fontSize: 12, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="value" fill="#0ea5e9" radius={[6, 6, 0, 0]} maxBarSize={60} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Metrics */}
      <div className="card p-6">
        <h2 className="text-lg font-semibold text-[var(--color-text)] mb-4">
          Key Metrics
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-[var(--color-bg-tertiary)]">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp className="w-4 h-4 text-green-600" />
              <span className="text-sm text-[var(--color-text-secondary)]">Resolution Rate</span>
            </div>
            <p className="text-2xl font-bold text-[var(--color-text)]">{resolutionRate}%</p>
          </div>
          <div className="p-4 rounded-xl bg-[var(--color-bg-tertiary)]">
            <div className="flex items-center gap-2 mb-2">
              <Users className="w-4 h-4 text-blue-600" />
              <span className="text-sm text-[var(--color-text-secondary)]">Total Conversations</span>
            </div>
            <p className="text-2xl font-bold text-[var(--color-text)]">{stats?.totalConversations || 0}</p>
          </div>
          <div className="p-4 rounded-xl bg-[var(--color-bg-tertiary)]">
            <div className="flex items-center gap-2 mb-2">
              <Activity className="w-4 h-4 text-purple-600" />
              <span className="text-sm text-[var(--color-text-secondary)]">Active Chats</span>
            </div>
            <p className="text-2xl font-bold text-[var(--color-text)]">{stats?.activeConversations || 0}</p>
          </div>
        </div>
      </div>
    </div>
  );
};