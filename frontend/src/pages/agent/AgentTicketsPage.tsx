import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import ticketService, { StatusType, PriorityType } from '@/services/ticketService';
import { TicketCard } from '@/components/UI/TicketCard';
import { SearchBar } from '@/components/UI/SearchBar';
import { Pagination } from '@/components/UI/Pagination';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';
import { useEffect, useState } from 'react';

export const AgentTicketsPage = () => {
  const { showToast } = useToast();
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<StatusType | ''>('');
  const [priority, setPriority] = useState<PriorityType | ''>('');

  const { data, isLoading, error } = useQuery({
    queryKey: ['agentTickets', page, size, keyword, status, priority],
    queryFn: () =>
      ticketService.getAssignedTickets({
        page,
        size,
        keyword: keyword || undefined,
        status: status || undefined,
        priority: priority || undefined,
      }),
    retry: 1,
  });

  useEffect(() => {
    if (error) {
      showToast('error', 'Failed to load tickets');
    }
  }, [error, showToast]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">Assigned Tickets</h1>
          <p className="text-gray-500">Manage tickets assigned to you</p>
        </div>
      </div>

      <SearchBar value={keyword} onChange={setKeyword} placeholder="Search tickets..." className="max-w-md" />

      <div className="flex gap-2">
        <select value={status} onChange={(e) => setStatus(e.target.value as StatusType)} className="input w-40">
          <option value="">All Status</option>
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="CLOSED">Closed</option>
        </select>
        <select value={priority} onChange={(e) => setPriority(e.target.value as PriorityType)} className="input w-40">
          <option value="">All Priority</option>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>

        </select>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-gray-100 dark:bg-gray-800 rounded-xl animate-pulse" />
          ))}
        </div>
      ) : data?.empty ? (
        <EmptyState title="No assigned tickets" description="You don't have any tickets assigned yet" />
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {data?.content.map((ticket) => (
                <TicketCard
                    key={ticket.id}
                    ticket={ticket}
                />
            ))}
          </div>
          {data && <Pagination currentPage={page} totalPages={data.totalPages} onPageChange={setPage} />}
        </>
      )}
    </div>
  );
};