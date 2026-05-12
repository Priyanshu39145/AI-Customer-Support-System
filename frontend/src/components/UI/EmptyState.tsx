import { Inbox, FolderOpen } from 'lucide-react';
import clsx from 'clsx';

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

export const EmptyState = ({ icon, title, description, action, className }: EmptyStateProps) => (
  <div className={clsx('flex flex-col items-center justify-center py-16 px-4 text-center', className)}>
    <div className="w-16 h-16 rounded-2xl bg-[var(--color-bg-tertiary)] flex items-center justify-center mb-5">
      {icon || <FolderOpen className="w-8 h-8 text-[var(--color-text-tertiary)]" />}
    </div>
    <h3 className="text-lg font-semibold text-[var(--color-text)] mb-2">{title}</h3>
    {description && (
      <p className="text-sm text-[var(--color-text-secondary)] max-w-sm mb-6">{description}</p>
    )}
    {action && <div>{action}</div>}
  </div>
);

export const EmptyStateLegacy = ({ icon, title, description, action }: EmptyStateProps) => (
  <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
    {icon || <Inbox className="w-12 h-12 text-gray-400 mb-4" />}
    <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-1">{title}</h3>
    {description && <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">{description}</p>}
    {action}
  </div>
);