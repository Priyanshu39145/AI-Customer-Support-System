import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, MessageSquare, Sparkles } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import conversationService from '@/services/conversationService';
import { ConversationCard } from '@/components/Chat/ConversationCard';
import { SearchBar } from '@/components/UI/SearchBar';
import { EmptyState } from '@/components/UI/EmptyState';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { useToast } from '@/components/Toast/ToastProvider';

export const ConversationsPage = () => {
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');

  const { data, isLoading, error } = useQuery({
    queryKey: ['conversations', search],
    queryFn: () =>
      search.trim()
        ? conversationService.searchConversations(search)
        : conversationService.getConversations(),
    retry: 1,
  });

  useEffect(() => {
    if (error) showToast('error', 'Failed to load conversations');
  }, [error, showToast]);

  const renameMutation = useMutation({
    mutationFn: ({ id, title }: { id: string; title: string }) =>
      conversationService.renameConversation(id, title),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['conversations'] });
      showToast('success', 'Conversation renamed');
    },
    onError: () => showToast('error', 'Failed to rename conversation'),
  });

  const deleteMutation = useMutation({
    mutationFn: ({ id, permanent }: { id: string; permanent?: boolean }) =>
      conversationService.deleteConversation(id, permanent),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['conversations'] });
      showToast('success', 'Conversation deleted');
    },
    onError: () => showToast('error', 'Failed to delete conversation'),
  });

  const closeMutation = useMutation({
    mutationFn: (id: string) => conversationService.closeConversation(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['conversations'] });
      showToast('success', 'Conversation closed');
    },
    onError: () => showToast('error', 'Failed to close conversation'),
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-semibold text-[var(--color-text)]">
              AI Chat
            </h1>
            <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-gradient-to-r from-primary-500/20 to-purple-500/20 text-primary-600 dark:text-primary-400 border border-primary-500/20">
              <Sparkles className="w-3 h-3 inline mr-1" />
              AI Powered
            </span>
          </div>
          <p className="text-[var(--color-text-secondary)] mt-1">
            Chat with your AI support assistant
          </p>
        </div>

        <Link to="/chat" className="btn-primary">
          <Plus className="w-4 h-4" />
          New Chat
        </Link>
      </div>

      {/* Search */}
      <div className="card p-4">
        <SearchBar
          value={search}
          onChange={setSearch}
          placeholder="Search conversations by title..."
          className="w-full"
        />
      </div>

      {/* Content */}
      {isLoading ? (
        <PageLoader />
      ) : data?.length === 0 ? (
        <EmptyState
          icon={<MessageSquare className="w-10 h-10 text-[var(--color-text-tertiary)]" />}
          title="No conversations yet"
          description="Start a new chat with AI to get instant support"
          action={
            <Link to="/chat" className="btn-primary">
              Start Chat
            </Link>
          }
        />
      ) : (
        <>
          <div className="text-sm text-[var(--color-text-secondary)]">
            {data?.length} conversation{data?.length !== 1 ? 's' : ''} found
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {data?.map((conversation, index) => (
              <div
                key={conversation.conversationId}
                onClick={() => navigate(`/chat/${conversation.conversationId}`)}
                className="animate-in"
                style={{ animationDelay: `${index * 30}ms` }}
              >
                <ConversationCard
                  conversation={conversation}
                  onRename={(conversationId, title) =>
                    renameMutation.mutate({ id: conversationId, title })
                  }
                  onDelete={(conversationId, permanent) =>
                    deleteMutation.mutate({ id: conversationId, permanent })
                  }
                  onClose={(conversationId) =>
                    closeMutation.mutate(conversationId)
                  }
                />
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};