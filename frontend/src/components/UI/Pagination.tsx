import { ChevronLeft, ChevronRight } from 'lucide-react';
import clsx from 'clsx';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export const Pagination = ({ currentPage, totalPages, onPageChange }: PaginationProps) => {
  if (totalPages <= 1) return null;

  const pages = [];
  const maxVisible = 5;
  let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages, start + maxVisible);

  if (end - start < maxVisible) {
    start = Math.max(0, end - maxVisible);
  }

  for (let i = start; i < end; i++) {
    pages.push(i);
  }

  return (
    <div className="flex items-center justify-center gap-1">
      {/* Previous Button */}
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className="p-2 rounded-lg hover:bg-[var(--color-bg-tertiary)] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      >
        <ChevronLeft className="w-4 h-4 text-[var(--color-text-secondary)]" />
      </button>

      {/* First Page */}
      {pages[0] > 0 && (
        <>
          <button
            onClick={() => onPageChange(0)}
            className="w-8 h-8 rounded-lg text-sm font-medium text-[var(--color-text-secondary)] hover:bg-[var(--color-bg-tertiary)] transition-colors"
          >
            1
          </button>
          {pages[0] > 1 && <span className="px-1 text-[var(--color-text-tertiary)]">...</span>}
        </>
      )}

      {/* Page Numbers */}
      {pages.map((page) => (
        <button
          key={page}
          onClick={() => onPageChange(page)}
          className={clsx(
            'w-8 h-8 rounded-lg text-sm font-medium transition-all duration-200',
            page === currentPage
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-[var(--color-text-secondary)] hover:bg-[var(--color-bg-tertiary)]'
          )}
        >
          {page + 1}
        </button>
      ))}

      {/* Last Page */}
      {pages[pages.length - 1] < totalPages - 1 && (
        <>
          {pages[pages.length - 1] < totalPages - 2 && (
            <span className="px-1 text-[var(--color-text-tertiary)]">...</span>
          )}
          <button
            onClick={() => onPageChange(totalPages - 1)}
            className="w-8 h-8 rounded-lg text-sm font-medium text-[var(--color-text-secondary)] hover:bg-[var(--color-bg-tertiary)] transition-colors"
          >
            {totalPages}
          </button>
        </>
      )}

      {/* Next Button */}
      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
        className="p-2 rounded-lg hover:bg-[var(--color-bg-tertiary)] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      >
        <ChevronRight className="w-4 h-4 text-[var(--color-text-secondary)]" />
      </button>
    </div>
  );
};