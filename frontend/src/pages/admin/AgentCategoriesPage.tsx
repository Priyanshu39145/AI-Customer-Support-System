import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { ArrowLeft, Save } from 'lucide-react';
import agentService from '@/services/agentService';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';
import { CategoryType } from '@/services/ticketService';

const CATEGORIES: CategoryType[] = [
  'GENERAL',
  'TECHNICAL',
  'BILLING',
  'DELIVERY',
  'ACCOUNT',
  'REFUND',
  'PAYMENT',
  'PRODUCT',
];

export const AgentCategoriesPage = () => {
  const { agentId } = useParams<{ agentId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [selected, setSelected] = useState<CategoryType[]>([]);

  const { data: agents, isLoading } = useQuery({
    queryKey: ['agents'],
    queryFn: () => agentService.getAgents(),
  });

  const agent = agents?.find((a) => a.agentId === agentId);

  useEffect(() => {
    if (agent) {
      setSelected(agent.categories);
    }
  }, [agent]);

  const mutation = useMutation({
    mutationFn: (categories: CategoryType[]) => agentService.assignCategories(agentId!, categories),
    onSuccess: () => {
      showToast('success', 'Categories updated');
      navigate('/admin/agents');
    },
    onError: () => showToast('error', 'Failed to update categories'),
  });

  if (isLoading) {
    return <PageLoader />;
  }

  if (!agent) {
    return <EmptyState title="Agent not found" />;
  }

  const toggleCategory = (category: CategoryType) => {
    setSelected((prev) =>
      prev.includes(category) ? prev.filter((c) => c !== category) : [...prev, category]
    );
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <Link to="/admin/agents" className="inline-flex items-center gap-2 text-gray-500 hover:text-gray-700">
        <ArrowLeft className="w-4 h-4" />
        Back to Agents
      </Link>

      <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
        <h1 className="text-2xl font-bold mb-2">Manage Categories</h1>
        <p className="text-gray-500 mb-6">Assign expertise categories to {agent.name}</p>

        <div className="space-y-3">
          {CATEGORIES.map((category) => (
            <label
              key={category}
              className="flex items-center gap-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <input
                type="checkbox"
                checked={selected.includes(category)}
                onChange={() => toggleCategory(category)}
                className="w-5 h-5 text-primary-600"
              />
              <span className="font-medium">{category}</span>
            </label>
          ))}
        </div>

        {selected.length === 0 && (
          <p className="mt-4 text-sm text-red-500">
            At least one category is required
          </p>
        )}

        <button
          onClick={() => mutation.mutate(selected)}
          disabled={selected.length === 0 || mutation.isPending}
          className="btn-primary w-full mt-6 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Save className="w-4 h-4" />
          {mutation.isPending ? 'Saving...' : 'Save Categories'}
        </button>
      </div>
    </div>
  );
};
