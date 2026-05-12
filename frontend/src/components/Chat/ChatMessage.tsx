import ReactMarkdown from 'react-markdown';
import { format } from 'date-fns';
import { User, Bot, Sparkles } from 'lucide-react';
import clsx from 'clsx';

interface ChatMessageProps {
  content: string;
  role: 'USER' | 'AI';
  createdAt: string;
  isThinking?: boolean;
}

export const ChatMessage = ({ content, role, createdAt, isThinking }: ChatMessageProps) => {
  const isUser = role === 'USER';

  const safeFormatTime = (date: string | undefined): string => {
    if (!date) return 'N/A';
    const parsed = new Date(date);
    if (isNaN(parsed.getTime())) return 'N/A';
    return format(parsed, 'h:mm a');
  };

  return (
    <div className={clsx('flex gap-3', isUser && 'flex-row-reverse')}>
      {/* Avatar */}
      <div
        className={clsx(
          'w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0',
          isUser
            ? 'bg-gradient-to-br from-primary-400 to-primary-600'
            : 'bg-gradient-to-br from-purple-500 to-violet-600'
        )}
      >
        {isUser ? (
          <User className="w-4 h-4 text-white" />
        ) : (
          <Sparkles className="w-4 h-4 text-white" />
        )}
      </div>

      {/* Message Bubble */}
      <div className={clsx('flex-1 max-w-[80%]', isUser && 'text-right')}>
        <div
          className={clsx(
            'inline-block px-4 py-3 rounded-2xl',
            isUser
              ? 'bg-gradient-to-br from-primary-500 to-primary-600 text-white rounded-br-md'
              : 'bg-[var(--color-bg-tertiary)] text-[var(--color-text)] rounded-bl-md'
          )}
        >
          {isThinking ? (
            <TypingIndicator />
          ) : (
            <div className="prose prose-sm dark:prose-invert max-w-none">
              <ReactMarkdown>{content}</ReactMarkdown>
            </div>
          )}
        </div>
        <p className="text-xs text-[var(--color-text-tertiary)] mt-1.5">
          {safeFormatTime(createdAt)}
        </p>
      </div>
    </div>
  );
};

const TypingIndicator = () => (
  <div className="flex items-center gap-1.5 py-1">
    <span className="typing-dot w-2 h-2 bg-[var(--color-text-tertiary)] rounded-full" />
    <span className="typing-dot w-2 h-2 bg-[var(--color-text-tertiary)] rounded-full" />
    <span className="typing-dot w-2 h-2 bg-[var(--color-text-tertiary)] rounded-full" />
  </div>
);