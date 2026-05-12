import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Plus, Filter, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import ticketService, { StatusType, PriorityType, CategoryType } from '@/services/ticketService';
import { TicketCard, TicketCardSkeleton } from '@/components/UI/TicketCard';
import { SearchBar } from '@/components/UI/SearchBar';
import { Pagination } from '@/components/UI/Pagination';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';
import { useAuth } from '@/contexts/AuthContext';
import clsx from 'clsx';

const statusOptions = [
  { value: '', label: 'All Status' },
  { value: 'OPEN', label: 'Open' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'CLOSED', label: 'Closed' },
];

const priorityOptions = [
  { value: '', label: 'All Priority' },
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
];

const categoryOptions = [
  { value: '', label: 'All Categories' },
  { value: 'GENERAL', label: 'General' },
  { value: 'TECHNICAL', label: 'Technical' },
  { value: 'BILLING', label: 'Billing' },
  { value: 'DELIVERY', label: 'Delivery' },
  { value: 'ACCOUNT', label: 'Account' },
  { value: 'REFUND', label: 'Refund' },
  { value: 'PAYMENT', label: 'Payment' },
  { value: 'PRODUCT', label: 'Product' },
];

export const TicketsPage = () => {
  const { user, isHydrated } = useAuth();
  const { showToast } = useToast();

  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<StatusType | ''>('');
  const [priority, setPriority] = useState<PriorityType | ''>('');
  const [category, setCategory] = useState<CategoryType | ''>('');
  const [showFilters, setShowFilters] = useState(false);

  const { data, isLoading, error } = useQuery({
    queryKey: ['tickets', user?.role, page, size, keyword, status, priority, category],
    queryFn: () => {
      const params = {
        page,
        size,
        keyword: keyword || undefined,
        status: status || undefined,
        priority: priority || undefined,
        category: category || undefined,
      };
      if (user?.role === 'ADMIN') {
        return ticketService.getAllTickets(params);
      }
      return ticketService.getMyTickets(params);
    },
    retry: 1,
    enabled: isHydrated && !!user,
  });

  useEffect(() => {
    if (error) showToast('error', 'Failed to load tickets');
  }, [error, showToast]);

  const hasActiveFilters = status || priority || category;

  const clearFilters = () => {
    setStatus('');
    setPriority('');
    setCategory('');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-[var(--color-text)]">
            {user?.role === 'ADMIN' ? 'All Tickets' : 'My Tickets'}
          </h1>
          <p className="text-[var(--color-text-secondary)] mt-1">
            {user?.role === 'ADMIN'
              ? 'Manage and monitor all support tickets'
              : 'View and track your support tickets'}
          </p>
        </div>

        {user?.role === 'USER' && (
          <Link to="/tickets/new" className="btn-primary">
            <Plus className="w-4 h-4" />
            Create Ticket
          </Link>
        )}
      </div>

      {/* Filters Card */}
      <div className="card p-4">
        <div className="flex flex-col md:flex-row gap-4">
          {/* Search */}
          <div className="flex-1">
            <SearchBar
              value={keyword}
              onChange={setKeyword}
              placeholder="Search tickets by title or description..."
              className="w-full"
            />
          </div>

          {/* Filter Toggle (Mobile) */}
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={clsx(
              'md:hidden btn-secondary justify-center',
              showFilters && 'bg-primary-50 dark:bg-primary-900/20 border-primary-300'
            )}
          >
            <Filter className="w-4 h-4" />
            Filters
            {hasActiveFilters && (
              <span className="ml-1 px-1.5 py-0.5 text-xs rounded-full bg-primary-600 text-white">
                {[status, priority, category].filter(Boolean).length}
              </span>
            )}
          </button>

          {/* Filter Selects - Desktop */}
          <div className="hidden md:flex gap-3">
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as StatusType)}
              className="select w-36"
            >
              {statusOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>

            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value as PriorityType)}
              className="select w-36"
            >
              {priorityOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>

            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as CategoryType)}
              className="select w-36"
            >
              {categoryOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Filter Selects - Mobile */}
        {showFilters && (
          <div className="md:hidden mt-4 pt-4 border-t border-[var(--color-border)] space-y-3">
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as StatusType)}
              className="select"
            >
              {statusOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>

            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value as PriorityType)}
              className="select"
            >
              {priorityOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>

            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as CategoryType)}
              className="select"
            >
              {categoryOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>

            {hasActiveFilters && (
              <button
                onClick={clearFilters}
                className="btn-ghost w-full justify-center text-red-600"
              >
                <X className="w-4 h-4" />
                Clear Filters
              </button>
            )}
          </div>
        )}
      </div>

      {/* Tickets Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <TicketCardSkeleton key={i} />
          ))}
        </div>
      ) : data?.empty ? (
        <EmptyState
          title="No tickets found"
          description={
            hasActiveFilters
              ? 'Try adjusting your filters to find what you\'re looking for.'
              : 'Create your first support ticket to get started.'
          }
          action={
            !hasActiveFilters && user?.role === 'USER' ? (
              <Link to="/tickets/new" className="btn-primary">
                Create Ticket
              </Link>
            ) : hasActiveFilters ? (
              <button onClick={clearFilters} className="btn-secondary">
                Clear Filters
              </button>
            ) : undefined
          }
        />
      ) : (
        <>
          {/* Results Count */}
          <div className="text-sm text-[var(--color-text-secondary)]">
            Showing {data?.content.length || 0} of {data?.totalElements || 0} tickets
          </div>

          {/* Tickets Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {data?.content.map((ticket, index) => (
              <div
                key={ticket.id}
                className="animate-in"
                style={{ animationDelay: `${index * 30}ms` }}
              >
                <TicketCard ticket={ticket} />
              </div>
            ))}
          </div>

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={data.totalPages}
              onPageChange={setPage}
            />
          )}
        </>
      )}
    </div>
  );
};