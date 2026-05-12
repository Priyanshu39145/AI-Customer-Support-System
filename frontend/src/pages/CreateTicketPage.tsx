import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, Send, AlertCircle, Ticket } from 'lucide-react';
import ticketService from '@/services/ticketService';
import { useToast } from '@/components/Toast/ToastProvider';

const ticketSchema = z.object({
  title: z.string().min(5, 'Title must be at least 5 characters'),
  description: z.string().min(20, 'Description must be at least 20 characters'),
  category: z.enum([
    'GENERAL', 'TECHNICAL', 'BILLING', 'DELIVERY', 'ACCOUNT', 'REFUND', 'PAYMENT', 'PRODUCT',
  ]).optional(),
});

type TicketForm = z.infer<typeof ticketSchema>;

const categories = [
  { value: 'GENERAL', label: 'General', description: 'General inquiries' },
  { value: 'TECHNICAL', label: 'Technical', description: 'Technical issues' },
  { value: 'BILLING', label: 'Billing', description: 'Billing questions' },
  { value: 'DELIVERY', label: 'Delivery', description: 'Delivery problems' },
  { value: 'ACCOUNT', label: 'Account', description: 'Account issues' },
  { value: 'REFUND', label: 'Refund', description: 'Refund requests' },
  { value: 'PAYMENT', label: 'Payment', description: 'Payment issues' },
  { value: 'PRODUCT', label: 'Product', description: 'Product inquiries' },
];

export const CreateTicketPage = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TicketForm>({
    resolver: zodResolver(ticketSchema),
  });

  const onSubmit = async (data: TicketForm) => {
    setIsSubmitting(true);
    try {
      await ticketService.createTicket(data);
      showToast('success', 'Ticket created successfully');
      navigate('/tickets');
    } catch {
      showToast('error', 'Failed to create ticket');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Back Link */}
      <Link
        to="/tickets"
        className="inline-flex items-center gap-2 text-[var(--color-text-secondary)] hover:text-[var(--color-text)] transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Tickets
      </Link>

      {/* Form Card */}
      <div className="card p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2.5 rounded-xl bg-primary-50 dark:bg-primary-900/20">
            <Ticket className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-[var(--color-text)]">
              Create New Ticket
            </h1>
            <p className="text-sm text-[var(--color-text-secondary)]">
              Submit a support request
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-[var(--color-text)] mb-2">
              Title
            </label>
            <input
              type="text"
              {...register('title')}
              placeholder="Brief description of your issue"
              className={`input ${errors.title ? 'input-error' : ''}`}
            />
            {errors.title && (
              <p className="text-sm text-red-500 mt-1.5 flex items-center gap-1.5">
                <AlertCircle className="w-4 h-4" />
                {errors.title.message}
              </p>
            )}
          </div>

          {/* Category */}
          <div>
            <label className="block text-sm font-medium text-[var(--color-text)] mb-2">
              Category
            </label>
            <select
              {...register('category')}
              className="select"
            >
              <option value="">Select a category (optional)</option>
              {categories.map((cat) => (
                <option key={cat.value} value={cat.value}>
                  {cat.label} - {cat.description}
                </option>
              ))}
            </select>
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-[var(--color-text)] mb-2">
              Description
            </label>
            <textarea
              {...register('description')}
              placeholder="Describe your issue in detail. Please include any relevant information that might help us assist you better..."
              className={`input min-h-[150px] resize-none ${errors.description ? 'input-error' : ''}`}
            />
            <div className="flex justify-between mt-1.5">
              {errors.description ? (
                <p className="text-sm text-red-500 flex items-center gap-1.5">
                  <AlertCircle className="w-4 h-4" />
                  {errors.description.message}
                </p>
              ) : (
                <span />
              )}
              <span className="text-xs text-[var(--color-text-tertiary)]">
                Minimum 20 characters
              </span>
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="btn-primary w-full py-3"
          >
            {isSubmitting ? (
              <span className="flex items-center gap-2">
                <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Creating...
              </span>
            ) : (
              <span className="flex items-center gap-2">
                Create Ticket
                <Send className="w-4 h-4" />
              </span>
            )}
          </button>
        </form>
      </div>
    </div>
  );
};