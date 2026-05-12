import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import userService, { RoleType } from '@/services/userService';

export const UsersPage = () => {

  const queryClient = useQueryClient();

  const { data: users } = useQuery({
    queryKey: ['users'],
    queryFn: () => userService.getAllUsers(),
  });

  const roleMutation = useMutation({
    mutationFn: ({
      userId,
      role,
    }: {
      userId: string;
      role: RoleType;
    }) => userService.updateUserRole(userId, role),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['users'],
      });
    },
  });

  return (
    <div className="space-y-6">

      <div>
        <h1 className="text-2xl font-bold text-white">
          Users
        </h1>

        <p className="text-gray-400">
          Manage user roles
        </p>
      </div>

      <div className="grid gap-4">

        {users?.map((user) => (

          <div
            key={user.id}
            className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-5 flex items-center justify-between"
          >

            <div>
              <h2 className="font-semibold">
                {user.name}
              </h2>

              <p className="text-sm text-gray-500">
                {user.email}
              </p>
            </div>

            <select
              value={user.role}
              onChange={(e) =>
                roleMutation.mutate({
                  userId: user.id,
                  role: e.target.value as RoleType,
                })
              }
              className="input w-40"
            >
              <option value="USER">USER</option>
              <option value="AGENT">AGENT</option>
              <option value="ADMIN">ADMIN</option>
            </select>

          </div>
        ))}
      </div>
    </div>
  );
};