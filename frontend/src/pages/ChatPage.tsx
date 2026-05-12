import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Send, ArrowLeft, Loader2, Sparkles } from 'lucide-react';
import conversationService from '@/services/conversationService';
import { ChatMessage } from '@/components/Chat/ChatMessage';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';
import clsx from 'clsx';

export const ChatPage = () => {
  const { conversationId } = useParams<{ conversationId?: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [input, setInput] = useState('');

  const { data: messages, isLoading: messagesLoading, error } = useQuery({
    queryKey: ['messages', conversationId],
    queryFn: () => conversationService.getMessages(conversationId!),
    enabled: !!conversationId,
    refetchInterval: conversationId ? 5000 : false,
    refetchIntervalInBackground: false,
  });

  useEffect(() => {
    if (error) showToast('error', 'Failed to load messages');
  }, [error, showToast]);

  const sendMutation = useMutation({
    mutationFn: async (message: string) => {
      const conversationIdToUse = conversationId || undefined;
      return conversationService.sendMessage(message, conversationIdToUse);
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['messages', data.conversationId || conversationId] });
      queryClient.invalidateQueries({ queryKey: ['conversations'] });
      if (!conversationId && data.conversationId) {
        navigate(`/chat/${data.conversationId}`, { replace: true });
      }
      setInput('');
    },
    onError: () => showToast('error', 'Failed to send message'),
  });

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || sendMutation.isPending) return;
    sendMutation.mutate(input.trim());
  };

  if (!messagesLoading && conversationId && !messages && !sendMutation.isPending) {
    return (
      <EmptyState
        title="Failed to load conversation"
        description="Please refresh and try again"
      />
    );
  }

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)]">
      {/* Header */}
      <div className="flex items-center gap-4 mb-4">
        <Link
          to="/conversations"
          className="p-2 rounded-lg hover:bg-[var(--color-bg-tertiary)] transition-colors"
        >
          <ArrowLeft className="w-5 h-5 text-[var(--color-text-secondary)]" />
        </Link>

        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-semibold text-[var(--color-text)]">
              AI Chat
            </h1>
            <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-gradient-to-r from-primary-500/20 to-purple-500/20 text-primary-600 dark:text-primary-400 border border-primary-500/20">
              <Sparkles className="w-3 h-3 inline mr-0.5" />
              AI
            </span>
          </div>
          <p className="text-sm text-[var(--color-text-secondary)]">
            Chat with your AI support assistant
          </p>
        </div>
      </div>

      {/* Chat Container */}
      <div className="flex-1 card overflow-hidden flex flex-col">
        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4 scrollbar-thin">
          {!conversationId ? (
            <div className="flex flex-col items-center justify-center h-full">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary-500 to-purple-600 flex items-center justify-center mb-4 shadow-lg">
                <Sparkles className="w-8 h-8 text-white" />
              </div>
              <h2 className="text-lg font-semibold text-[var(--color-text)] mb-2">
                Start a conversation
              </h2>
              <p className="text-sm text-[var(--color-text-secondary)] text-center max-w-md mb-6">
                Ask me anything about your tickets, support issues, or company policies. I'm here to help!
              </p>
              <button
                onClick={() => navigate('/conversations')}
                className="btn-secondary"
              >
                View Past Conversations
              </button>
            </div>
          ) : messagesLoading ? (
            <div className="flex items-center justify-center h-full">
              <div className="flex flex-col items-center gap-3">
                <Loader2 className="w-8 h-8 animate-spin text-primary-600" />
                <p className="text-sm text-[var(--color-text-secondary)]">Loading messages...</p>
              </div>
            </div>
          ) : messages?.length === 0 ? (
            <EmptyState
              title="No messages yet"
              description="Send a message to start the conversation"
            />
          ) : (
            messages?.map((message) => (
              <ChatMessage
                key={message.id}
                content={message.content}
                role={message.senderType === 'AI' ? 'AI' : 'USER'}
                createdAt={message.createdAt}
              />
            ))
          )}

          {sendMutation.isPending && (
            <ChatMessage
              content=""
              role="AI"
              createdAt={new Date().toISOString()}
              isThinking
            />
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input Area */}
        <form
          onSubmit={handleSubmit}
          className="border-t border-[var(--color-border)] p-4 bg-[var(--color-bg-secondary)]"
        >
          <div className="flex gap-3">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value.trimStart())}
              placeholder="Type your message..."
              className="input flex-1"
              disabled={sendMutation.isPending}
            />
            <button
              type="submit"
              disabled={!input.trim() || sendMutation.isPending}
              className={clsx(
                'btn-primary px-4',
                sendMutation.isPending && 'opacity-50 cursor-not-allowed'
              )}
            >
              {sendMutation.isPending ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : (
                <Send className="w-5 h-5" />
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};