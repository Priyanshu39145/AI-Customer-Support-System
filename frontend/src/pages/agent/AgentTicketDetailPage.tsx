import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { format, parseISO, isValid } from 'date-fns';
import { ArrowLeft, Send } from 'lucide-react';
import ticketService, { StatusType, PriorityType, CategoryType } from '@/services/ticketService';
import { TicketStatusBadge, PriorityBadge, CategoryBadge } from '@/components/UI/Badges';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';

/**
 * Safe date formatter - handles null, undefined, or invalid dates gracefully.
 */
const safeFormatDate = (date: string | null | undefined, formatStr: string): string => {
  if (!date) {
    return 'N/A';
  }
  try {
    const parsed = parseISO(date);
    if (!isValid(parsed)) {
      return 'N/A';
    }
    return format(parsed, formatStr);
  } catch {
    return 'N/A';
  }
};

export const AgentTicketDetailPage = () => {
  const { ticketId } = useParams<{ ticketId: string }>();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [showCommentForm, setShowCommentForm] = useState(false);
  const [comment, setComment] = useState('');

  const { data: ticket, isLoading, error } = useQuery({
    queryKey: ['ticket', ticketId],
    queryFn: () => ticketService.getTicketById(ticketId!),
    enabled: !!ticketId,
    retry: 1,
  });

  const { data: comments } = useQuery({
    queryKey: ['ticketComments', ticketId],
    queryFn: () => ticketService.getComments(ticketId!),
    enabled: !!ticketId,
  });

  const { data: history } = useQuery({
    queryKey: ['ticketHistory', ticketId],
    queryFn: () => ticketService.getTicketHistory(ticketId!),
    enabled: !!ticketId,
  });

  const statusMutation = useMutation({
    mutationFn: (status: StatusType) => ticketService.changeStatus(ticketId!, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] });
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      queryClient.invalidateQueries({ queryKey: ['ticketHistory', ticketId] });

      showToast('success', 'Status updated');
    },
    onError: () => showToast('error', 'Failed to update status'),
  });

  const priorityMutation = useMutation({
    mutationFn: (priority: PriorityType) => ticketService.changePriority(ticketId!, priority),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] });
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      queryClient.invalidateQueries({ queryKey: ['ticketHistory', ticketId] });

      showToast('success', 'Status updated');
    },
    onError: () => showToast('error', 'Failed to update priority'),
  });

  const categoryMutation = useMutation({
    mutationFn: (category: CategoryType) => ticketService.changeCategory(ticketId!, category),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] });
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      showToast('success', 'Category updated');
    },
    onError: () => showToast('error', 'Failed to update category'),
  });

  const commentMutation = useMutation({
    mutationFn: (content: string) =>
      ticketService.addComment(ticketId!, content.trim()),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ticketComments', ticketId] });
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      showToast('success', 'Comment added');
      setShowCommentForm(false);
      setComment('');
    },
    onError: () => showToast('error', 'Failed to add comment'),
  });

  if (isLoading) {
    return <PageLoader />;
  }

  if (error || !ticket) {
    return (
      <EmptyState
        title="Ticket not found"
        description="The ticket you're looking for doesn't exist"
      />
    );
  }

  return (
    <div className="space-y-6">
      <Link to="/agent/tickets" className="inline-flex items-center gap-2 text-gray-500 hover:text-gray-700">
        <ArrowLeft className="w-4 h-4" />
        Back to Tickets
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h1 className="text-2xl font-bold">{ticket.title}</h1>
                <p className="text-sm text-gray-500">
                  Created {safeFormatDate(ticket.createdAt, 'MMM d, yyyy h:mm a')}
                </p>
              </div>
              <TicketStatusBadge status={ticket.status} />
            </div>
            <p className="whitespace-pre-wrap">
              {ticket.description || 'No description provided'}
            </p>
            <div className="flex items-center gap-2 mt-4">
              <PriorityBadge priority={ticket.priority} />
              <CategoryBadge category={ticket.category} />
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
            <div className="flex justify-between mb-4">
              <h2 className="font-semibold">Comments ({comments?.length || 0})</h2>
              <button onClick={() => setShowCommentForm(!showCommentForm)} className="btn-secondary text-sm">
                Add
              </button>
            </div>
            {showCommentForm && (
              <div className="flex gap-2 mb-4">
                <input
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Write a comment..."
                  className="input flex-1"
                />
                <button
                  onClick={() => commentMutation.mutate(comment)}
                  disabled={!comment.trim()}
                  className="btn-primary"
                >
                  <Send className="w-4 h-4" />
                </button>
              </div>
            )}
            <div className="space-y-3">
              {!comments || comments.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No comments yet
                </p>
              ) : (
                comments.map((c) => (
                <div key={c.id} className="border-b pb-3">
                  <p className="font-medium">{c.authorName || 'Unknown'}</p>
                  <p>{c.content}</p>
                </div>
              ))
              )}
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
            <h2 className="font-semibold mb-4">Actions</h2>
            <div className="space-y-3">
              <div>
                <label className="text-sm text-gray-500">Status</label>

                {ticket.status === "OPEN" && (
                  <button
                    onClick={() => statusMutation.mutate("IN_PROGRESS")}
                    disabled={statusMutation.isPending}
                    className="btn-primary mt-1 w-full"
                  >
                    Move to In Progress
                  </button>
                )}

                {ticket.status === "IN_PROGRESS" && (
                  <button
                    onClick={() => statusMutation.mutate("CLOSED")}
                    disabled={statusMutation.isPending}
                    className="btn-primary mt-1 w-full"
                  >
                    Move to Closed
                  </button>
                )}

                {ticket.status === "CLOSED" && (
                  <button
                    disabled
                    className="btn-secondary mt-1 w-full"
                  >
                    Ticket Closed
                  </button>
                )}
              </div>
              <div>
                <label className="text-sm text-gray-500">Priority</label>
                <select
                  value={ticket.priority}
                  onChange={(e) => priorityMutation.mutate(e.target.value as PriorityType)}
                  disabled={priorityMutation.isPending}
                  className="input mt-1"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>

                </select>
              </div>
              <div>
                <label className="text-sm text-gray-500">Category</label>
                <select
                  value={ticket.category}
                  onChange={(e) => categoryMutation.mutate(e.target.value as CategoryType)}
                  disabled={categoryMutation.isPending}
                  className="input mt-1"
                >
                  <option value="GENERAL">General</option>
                  <option value="TECHNICAL">Technical</option>
                  <option value="BILLING">Billing</option>
                  <option value="DELIVERY">Delivery</option>
                  <option value="ACCOUNT">Account</option>
                  <option value="REFUND">Refund</option>
                  <option value="PAYMENT">Payment</option>
                  <option value="PRODUCT">Product</option>
                </select>
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
            <h2 className="font-semibold mb-4">Details</h2>
            <div className="space-y-2 text-sm">
              <p><span className="text-gray-500">Created by:</span> {ticket.createdByName || 'N/A'}</p>
              {ticket.assignedToId && (
                <p><span className="text-gray-500">Assigned to:</span> {ticket.assignedToName || 'N/A'}</p>
              )}
              <p><span className="text-gray-500">Updated:</span> {safeFormatDate(ticket.updatedAt, 'MMM d, h:mm a')}</p>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
            <h2 className="font-semibold mb-4">Activity</h2>

            <div className="space-y-3">
              {!history || history.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No activity history available
                </p>
              ) : (
                history.map((activity) => (
                  <div
                    key={`${activity.action}-${activity.timestamp}`}
                    className="border-b border-[var(--color-border)] pb-2"
                  >
                    <p className="text-sm font-medium">
                      {activity.action}
                    </p>

                    <p className="text-xs text-gray-500">
                      {activity.performedByName} •{' '}
                      {safeFormatDate(activity.timestamp, 'MMM d, yyyy h:mm a')}
                    </p>


                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};