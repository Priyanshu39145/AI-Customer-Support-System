import clsx from 'clsx';

export const TicketStatusBadge = ({ status }: { status: string }) => {
  const styles = {
    OPEN: {
      bg: 'bg-amber-50 dark:bg-amber-950/30',
      text: 'text-amber-700 dark:text-amber-400',
      dot: 'bg-amber-500',
    },
    IN_PROGRESS: {
      bg: 'bg-blue-50 dark:bg-blue-950/30',
      text: 'text-blue-700 dark:text-blue-400',
      dot: 'bg-blue-500',
    },
    CLOSED: {
      bg: 'bg-slate-50 dark:bg-slate-800/50',
      text: 'text-slate-700 dark:text-slate-400',
      dot: 'bg-slate-400',
    },
  };

  const style = styles[status as keyof typeof styles] || styles.CLOSED;

  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium',
        style.bg,
        style.text
      )}
    >
      <span className={clsx('w-1.5 h-1.5 rounded-full', style.dot)} />
      {status.replace('_', ' ')}
    </span>
  );
};

export const PriorityBadge = ({ priority }: { priority: string }) => {
  const styles = {
    LOW: {
      bg: 'bg-slate-100 dark:bg-slate-800',
      text: 'text-slate-700 dark:text-slate-300',
      icon: 'text-slate-500',
    },
    MEDIUM: {
      bg: 'bg-blue-50 dark:bg-blue-950/30',
      text: 'text-blue-700 dark:text-blue-400',
      icon: 'text-blue-500',
    },
    HIGH: {
      bg: 'bg-orange-50 dark:bg-orange-950/30',
      text: 'text-orange-700 dark:text-orange-400',
      icon: 'text-orange-500',
    },
  };

  const style = styles[priority as keyof typeof styles] || styles.MEDIUM;

  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium',
        style.bg,
        style.text
      )}
    >
      {priority}
    </span>
  );
};

export const CategoryBadge = ({ category }: { category: string }) => {
  const styles: Record<string, { bg: string; text: string }> = {
    GENERAL: { bg: 'bg-slate-100 dark:bg-slate-800', text: 'text-slate-700 dark:text-slate-300' },
    TECHNICAL: { bg: 'bg-violet-50 dark:bg-violet-950/30', text: 'text-violet-700 dark:text-violet-400' },
    BILLING: { bg: 'bg-emerald-50 dark:bg-emerald-950/30', text: 'text-emerald-700 dark:text-emerald-400' },
    DELIVERY: { bg: 'bg-amber-50 dark:bg-amber-950/30', text: 'text-amber-700 dark:text-amber-400' },
    ACCOUNT: { bg: 'bg-indigo-50 dark:bg-indigo-950/30', text: 'text-indigo-700 dark:text-indigo-400' },
    REFUND: { bg: 'bg-red-50 dark:bg-red-950/30', text: 'text-red-700 dark:text-red-400' },
    PAYMENT: { bg: 'bg-pink-50 dark:bg-pink-950/30', text: 'text-pink-700 dark:text-pink-400' },
    PRODUCT: { bg: 'bg-cyan-50 dark:bg-cyan-950/30', text: 'text-cyan-700 dark:text-cyan-400' },
  };

  const style = styles[category] || styles.GENERAL;

  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
        style.bg,
        style.text
      )}
    >
      {category}
    </span>
  );
};

// Role Badge for admin panels
export const RoleBadge = ({ role }: { role: string }) => {
  const styles: Record<string, { bg: string; text: string }> = {
    ADMIN: { bg: 'bg-purple-50 dark:bg-purple-950/30', text: 'text-purple-700 dark:text-purple-400' },
    AGENT: { bg: 'bg-blue-50 dark:bg-blue-950/30', text: 'text-blue-700 dark:text-blue-400' },
    USER: { bg: 'bg-slate-100 dark:bg-slate-800', text: 'text-slate-700 dark:text-slate-300' },
  };

  const style = styles[role] || styles.USER;

  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
        style.bg,
        style.text
      )}
    >
      {role}
    </span>
  );
};