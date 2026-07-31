import { MoreVertical, Edit, Trash, XCircle, Sparkles } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { Conversation } from '@/services/conversationService';
import { useToast } from '@/components/Toast/ToastProvider';
import clsx from 'clsx';

interface ConversationCardProps {
  conversation: Conversation;
  onRename: (id: string, title: string) => void;
  onDelete: (id: string, permanent?: boolean) => void;
  onClose: (id: string) => void;
}

export const ConversationCard = ({
  conversation,
  onRename,
  onDelete,
  onClose,
}: ConversationCardProps) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editTitle, setEditTitle] = useState(conversation.conversationTitle);
  useEffect(() => {
                                        setEditTitle(conversation.conversationTitle);
                                    }, [conversation.conversationTitle]);
  const menuRef = useRef<HTMLDivElement>(null);
  const { showToast } = useToast();

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleRename = () => {
    const trimmedTitle = editTitle.trim();

    if (!trimmedTitle) {
      setEditMode(false);
      return;
    }

    if (trimmedTitle.length > 80) {
      showToast('error', 'Title cannot exceed 80 characters');
      return;
    }

    if (trimmedTitle !== conversation.conversationTitle) {
      onRename(conversation.conversationId, trimmedTitle);
    }

    setEditMode(false);
  };

  return (
    <div className="group card-hover p-4 cursor-pointer">
      <div className="flex items-start justify-between gap-3">
        {editMode ? (
          <input
            type="text"
            value={editTitle}
            onChange={(e) => setEditTitle(e.target.value)}
            onBlur={handleRename}
            onKeyDown={(e) => e.key === 'Enter' && handleRename()}
            maxLength={80}
            className="input text-sm py-1.5 flex-1"
            autoFocus
          />
        ) : (
          <div className="flex items-start gap-3 flex-1 min-w-0">
            <div className="p-2 rounded-lg flex-shrink-0 bg-primary-50 dark:bg-primary-900/20">
              <Sparkles className="w-4 h-4 text-primary-500" />
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="font-medium text-[var(--color-text)] truncate">
                {conversation.conversationTitle}
              </h3>
            </div>
          </div>
        )}

        {/* Menu */}
        <div className="relative" ref={menuRef}>
          <button
            onClick={(e) => {
              e.stopPropagation();
              setMenuOpen(!menuOpen);
            }}
            className="p-1.5 rounded-lg hover:bg-[var(--color-bg-tertiary)] transition-colors opacity-0 group-hover:opacity-100"
          >
            <MoreVertical className="w-4 h-4 text-[var(--color-text-secondary)]" />
          </button>

          {menuOpen && (
            <div className="dropdown animate-scale-in z-20">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setEditMode(true);
                  setMenuOpen(false);
                }}
                className="dropdown-item w-full"
              >
                <Edit className="w-4 h-4" />
                Rename
              </button>

              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onClose(conversation.conversationId);
                  setMenuOpen(false);
                }}
                className="dropdown-item w-full"
              >
                <XCircle className="w-4 h-4" />
                Close
              </button>

              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onDelete(conversation.conversationId);
                  setMenuOpen(false);
                }}
                className="dropdown-item-danger w-full"
              >
                <Trash className="w-4 h-4" />
                Delete
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};