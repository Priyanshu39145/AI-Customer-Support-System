import { Loader2 } from 'lucide-react';
import clsx from 'clsx';

export const LoadingSpinner = ({
  size = 'md',
  className = '',
  color = 'primary',
}: {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  color?: 'primary' | 'white' | 'gray';
}) => {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' };
  const colors = {
    primary: 'text-primary-600',
    white: 'text-white',
    gray: 'text-[var(--color-text-tertiary)]',
  };

  return (
    <Loader2
      className={clsx('animate-spin', sizes[size], colors[color], className)}
    />
  );
};

export const PageLoader = () => (
  <div className="flex items-center justify-center min-h-[400px]">
    <div className="flex flex-col items-center gap-3">
      <LoadingSpinner size="lg" />
      <p className="text-sm text-[var(--color-text-secondary)]">Loading...</p>
    </div>
  </div>
);

export const ButtonLoader = ({ size = 'sm' }: { size?: 'sm' | 'md' }) => (
  <Loader2 className={clsx('animate-spin', size === 'sm' ? 'w-4 h-4' : 'w-5 h-5')} />
);