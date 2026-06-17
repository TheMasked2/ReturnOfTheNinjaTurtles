import type { MouseEvent } from "react";

interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (nextPage: number) => void;
  disabled?: boolean;
}

export function Pagination({ page, totalPages, onPageChange, disabled }: PaginationProps) {
  const canPrev = page > 0;
  const canNext = page < Math.max(totalPages - 1, 0);

  const handlePrevious = (event: MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
    if (canPrev) {
      onPageChange(page - 1);
    }
  };

  const handleNext = (event: MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
    if (canNext) {
      onPageChange(page + 1);
    }
  };

  return (
    <div className="pagination">
      <button
        type="button"
        className="button button-secondary"
        onClick={handlePrevious}
        disabled={!canPrev || disabled}
      >
        Previous
      </button>

      <span className="pagination-status">
        Page {Math.min(page + 1, Math.max(totalPages, 1))} of {Math.max(totalPages, 1)}
      </span>

      <button
        type="button"
        className="button button-secondary"
        onClick={handleNext}
        disabled={!canNext || disabled}
      >
        Next
      </button>
    </div>
  );
}