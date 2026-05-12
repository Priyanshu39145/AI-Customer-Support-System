import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format, parseISO, isValid } from 'date-fns';
import { ArrowLeft, Send, Clock, User, Calendar, Mail, AlertCircle } from 'lucide-react';
import ticketService from '@/services/ticketService';
import agentService from '@/services/agentService';
import { TicketStatusBadge, PriorityBadge, CategoryBadge } from '@/components/UI/Badges';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';
import { useAuth } from '@/contexts/AuthContext';
import clsx from 'clsx';

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

const commentSchema = z.object({
  content: z.string().trim().min(1, 'Comment cannot be empty'),
});

type CommentForm = z.infer<typeof commentSchema>;

export const TicketDetailPage = () => {
  const { user } = useAuth();
  const { ticketId } = useParams<{ ticketId: string }>();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [showCommentForm, setShowCommentForm] = useState(false);

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

  const { data: agents } = useQuery({
    queryKey: ['agents'],
    queryFn: () => agentService.getAllAgents(),
    enabled: user?.role === 'ADMIN',
  });

  const addCommentMutation = useMutation({
    mutationFn: (content: string) => ticketService.addComment(ticketId!, content),
    onSuccess: () => {
      reset();
      queryClient.invalidateQueries({ queryKey: ['ticketComments', ticketId] });
      queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] });
      showToast('success', 'Comment added successfully');
      setShowCommentForm(false);
    },
    onError: () => showToast('error', 'Failed to add comment'),
  });

  const assignTicketMutation = useMutation({
    mutationFn: (agentId: string) => ticketService.assignTicket(ticketId!, agentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] });
      showToast('success', 'Ticket assigned successfully');
    },
    onError: () => showToast('error', 'Failed to assign ticket'),
  });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CommentForm>({
    resolver: zodResolver(commentSchema),
  });

  const onSubmit = (data: CommentForm) => addCommentMutation.mutate(data.content);

  if (isLoading) return <PageLoader />;
  if (error || !ticket) {
    return (
      <EmptyState
        title="Ticket not found"
        description="The ticket you're looking for doesn't exist or has been deleted"
        action={
          <Link to="/tickets" className="btn-primary">
            Back to Tickets
          </Link>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Back Link */}
      <Link
        to="/tickets"
        className="inline-flex items-center gap-2 text-[var(--color-text-secondary)] hover:text-[var(--color-text)] transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Tickets
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Ticket Details */}
          <div className="card p-6">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h1 className="text-xl font-semibold text-[var(--color-text)]">
                  {ticket.title}
                </h1>
                <p className="text-sm text-[var(--color-text-tertiary)] mt-1">
                  Created {safeFormatDate(ticket.createdAt, 'MMM d, yyyy h:mm a')}
                </p>
              </div>
              <TicketStatusBadge status={ticket.status} />
            </div>

            <p className="text-[var(--color-text-secondary)] whitespace-pre-wrap">
              {ticket.description || 'No description provided'}
            </p>

            <div className="flex items-center gap-2 mt-4 flex-wrap">
              <PriorityBadge priority={ticket.priority} />
              <CategoryBadge category={ticket.category} />
            </div>
          </div>

          {/* Comments */}
          <div className="card p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-[var(--color-text)]">
                Comments ({comments?.length || 0})
              </h2>
              <button
                onClick={() => setShowCommentForm(!showCommentForm)}
                className="btn-secondary text-sm"
              >
                Add Comment
              </button>
            </div>

            {showCommentForm && (
              <form onSubmit={handleSubmit(onSubmit)} className="mb-6">
                <div className="relative">
                  <textarea
                    {...register('content')}
                    placeholder="Write a comment..."
                    className={clsx('input min-h-[100px] resize-none', errors.content && 'input-error')}
                  />
                  <button
                    type="submit"
                    disabled={addCommentMutation.isPending}
                    className="absolute bottom-3 right-3 p-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 transition-colors"
                  >
                    <Send className="w-4 h-4" />
                  </button>
                </div>
                {errors.content && (
                  <p className="text-sm text-red-500 mt-1.5 flex items-center gap-1">
                    <AlertCircle className="w-4 h-4" />
                    {errors.content.message}
                  </p>
                )}
              </form>
            )}

            <div className="space-y-4">
              {!comments || comments.length === 0 ? (
                <p className="text-sm text-[var(--color-text-secondary)]">No comments yet</p>
              ) : (
                comments.map((comment) => (
                  <div key={comment.id} className="border-b border-[var(--color-border)] pb-4 last:border-0">
                    <div className="flex items-center gap-2 mb-2">
                      <div className="w-6 h-6 rounded-full bg-[var(--color-bg-tertiary)] flex items-center justify-center">
                        <User className="w-3.5 h-3.5 text-[var(--color-text-tertiary)]" />
                      </div>
                      <span className="font-medium text-sm text-[var(--color-text)]">
                        {comment.authorName || 'Unknown'}
                      </span>
                      <span className="text-sm text-[var(--color-text-tertiary)]">
                        {safeFormatDate(comment.createdAt, 'MMM d, yyyy h:mm a')}
                      </span>
                    </div>
                    <p className="text-sm text-[var(--color-text-secondary)] pl-8">
                      {comment.content}
                    </p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Details Card */}
          <div className="card p-5">
            <h2 className="text-base font-semibold text-[var(--color-text)] mb-4">
              Details
            </h2>
            <div className="space-y-4">
              <div>
                <p className="text-xs text-[var(--color-text-tertiary)] flex items-center gap-1.5 mb-1">
                  <User className="w-3 h-3" />
                  Created By
                </p>
                <p className="font-medium text-sm text-[var(--color-text)]">
                  {ticket.createdByName || 'N/A'}
                </p>
                <p className="text-xs text-[var(--color-text-tertiary)] flex items-center gap-1.5 mt-0.5">
                  <Mail className="w-3 h-3" />
                  {ticket.createdByEmail || 'N/A'}
                </p>
              </div>

              {ticket.assignedToId && (
                <div>
                  <p className="text-xs text-[var(--color-text-tertiary)] flex items-center gap-1.5 mb-1">
                    <User className="w-3 h-3" />
                    Assigned To
                  </p>
                  <p className="font-medium text-sm text-[var(--color-text)]">
                    {ticket.assignedToName || 'N/A'}
                  </p>
                  <p className="text-xs text-[var(--color-text-tertiary)] flex items-center gap-1.5 mt-0.5">
                    <Mail className="w-3 h-3" />
                    {ticket.assignedToEmail || 'N/A'}
                  </p>
                </div>
              )}

              <div>
                <p className="text-xs text-[var(--color-text-tertiary)] flex items-center gap-1.5 mb-1">
                  <Calendar className="w-3 h-3" />
                  Last Updated
                </p>
                <p className="font-medium text-sm text-[var(--color-text)]">
                  {safeFormatDate(ticket.updatedAt, 'MMM d, yyyy h:mm a')}
                </p>
              </div>
            </div>
          </div>

          {/* Assign Agent (Admin only) */}
          {user?.role === 'ADMIN' && (
            <div className="card p-5">
              <h2 className="text-base font-semibold text-[var(--color-text)] mb-4">
                Assign Ticket
              </h2>
              <select
                defaultValue=""
                onChange={(e) => {
                  if (!e.target.value) return;
                  assignTicketMutation.mutate(e.target.value);
                }}
                className="select"
              >
                <option value="">Select Agent</option>
                {agents?.map((agent) => (
                  <option key={agent.id} value={agent.id}>
                    {agent.name} ({agent.email})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Activity History */}
          <div className="card p-5">
            <h2 className="text-base font-semibold text-[var(--color-text)] mb-4">
              Activity
            </h2>
            <div className="space-y-3">
              {!history || history.length === 0 ? (
                <p className="text-sm text-[var(--color-text-secondary)]">No activity history</p>
              ) : (
                history.map((activity) => (
                  <div key={activity.id} className="flex items-start gap-2.5">
                    <div className="w-1.5 h-1.5 rounded-full bg-primary-500 mt-2" />
                    <div>
                      <p className="text-sm text-[var(--color-text)]">
                        {activity.actionType}
                      </p>
                      <p className="text-xs text-[var(--color-text-tertiary)]">
                        {activity.performedByName} - {safeFormatDate(activity.timestamp, 'MMM d, h:mm a')}
                      </p>
                    </div>
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