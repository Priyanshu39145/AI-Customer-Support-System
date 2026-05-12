import { useQuery } from '@tanstack/react-query';
import { User } from 'lucide-react';
import { Link } from 'react-router-dom';
import agentService from '@/services/agentService';
import { PageLoader } from '@/components/UI/LoadingSpinner';
import { EmptyState } from '@/components/UI/EmptyState';
import { useToast } from '@/components/Toast/ToastProvider';

export const AdminAgentsPage = () => {
  const { showToast } = useToast();

  const { data: agents, isLoading, error } = useQuery({
    queryKey: ['agents'],
    queryFn: () => agentService.getAgents(),
    retry: 1,
  });

  if (error) {
    showToast('error', 'Failed to load agents');
  }

  if (isLoading) {
    return <PageLoader />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Agents</h1>
        <p className="text-gray-500">Manage support agents</p>
      </div>

      {agents?.length === 0 ? (
        <EmptyState title="No agents" description="No agents have been registered yet" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {agents?.map((agent) => (
            <div
              key={agent.id}
              className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-5"
            >
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                  <User className="w-6 h-6 text-primary-600 dark:text-primary-400" />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold">{agent.name}</h3>
                  <p className="text-sm text-gray-500">{agent.email}</p>
                </div>
              </div>

              <div className="mt-4">
                <p className="text-sm text-gray-500 mb-2">Categories</p>
                <div className="flex flex-wrap gap-1">
                  {agent.categories?.length > 0 ? (
                    agent.categories.map((cat) => (
                      <span
                        key={cat}
                        className="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded text-xs"
                      >
                        {cat}
                      </span>
                    ))
                  ) : (
                    <span className="text-sm text-gray-400">No categories assigned</span>
                  )}
                </div>
              </div>

              <Link
                to={`/admin/agents/${agent.agentId}/categories`}
                className="mt-4 btn-secondary w-full block text-center"
              >
                Manage Categories
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};