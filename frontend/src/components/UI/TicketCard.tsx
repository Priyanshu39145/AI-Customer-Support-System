import { Link } from 'react-router-dom';
import { format, parseISO, isValid } from 'date-fns';
import { Clock, User } from 'lucide-react';
import { TicketStatusBadge, PriorityBadge, CategoryBadge } from './Badges';
import { TicketResponse } from '@/services/ticketService';
import { useAuth } from '@/contexts/AuthContext';
import clsx from 'clsx';

interface TicketCardProps {
  ticket: TicketResponse;
}

const safeFormatDate = (date: string | null | undefined, formatStr: string): string => {
  if (!date) return 'N/A';
  try {
    const parsed = parseISO(date);
    if (!isValid(parsed)) return 'N/A';
    return format(parsed, formatStr);
  } catch {
    return 'N/A';
  }
};

export const TicketCard = ({ ticket }: TicketCardProps) => {
  const { user } = useAuth();

  const getTicketPath = () => {
    if (user?.role === 'ADMIN') return `/admin/tickets/${ticket.id}`;
    if (user?.role === 'AGENT') return `/agent/tickets/${ticket.id}`;
    return `/tickets/${ticket.id}`;
  };

  return (
    <Link
      to={getTicketPath()}
      className="group block card-hover p-5"
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-3 mb-3">
        <h3 className="font-semibold text-[var(--color-text)] line-clamp-1 group-hover:text-primary-600 transition-colors">
          {ticket.title}
        </h3>
        <TicketStatusBadge status={ticket.status} />
      </div>

      {/* Description */}
      <p className="text-sm text-[var(--color-text-secondary)] line-clamp-2 mb-4">
        {ticket.description}
      </p>

      {/* Tags */}
      <div className="flex items-center gap-2 flex-wrap mb-4">
        <PriorityBadge priority={ticket.priority} />
        <CategoryBadge category={ticket.category} />
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between pt-3 border-t border-[var(--color-border)]">
        <div className="flex items-center gap-1.5 text-xs text-[var(--color-text-tertiary)]">
          <Clock className="w-3.5 h-3.5" />
          <span>{safeFormatDate(ticket.createdAt, 'MMM d, yyyy')}</span>
        </div>

        {ticket.assignedToName && (
          <div className="flex items-center gap-1.5 text-xs text-[var(--color-text-tertiary)]">
            <User className="w-3.5 h-3.5" />
            <span className="truncate max-w-[100px]">{ticket.assignedToName}</span>
          </div>
        )}
      </div>
    </Link>
  );
};

export const TicketCardSkeleton = () => (
  <div className="card p-5 animate-pulse">
    <div className="flex items-start justify-between gap-3 mb-3">
      <div className="h-5 bg-[var(--color-bg-tertiary)] rounded w-3/4" />
      <div className="h-6 w-16 bg-[var(--color-bg-tertiary)] rounded-full" />
    </div>
    <div className="h-4 bg-[var(--color-bg-tertiary)] rounded w-full mb-2" />
    <div className="h-4 bg-[var(--color-bg-tertiary)] rounded w-2/3 mb-4" />
    <div className="flex gap-2 mb-4">
      <div className="h-6 w-14 bg-[var(--color-bg-tertiary)] rounded-full" />
      <div className="h-6 w-20 bg-[var(--color-bg-tertiary)] rounded-full" />
    </div>
    <div className="flex items-center justify-between pt-3 border-t border-[var(--color-border)]">
      <div className="h-3 w-20 bg-[var(--color-bg-tertiary)] rounded" />
      <div className="h-3 w-24 bg-[var(--color-bg-tertiary)] rounded" />
    </div>
  </div>
);