import api from './api';

export type RoleType = 'USER' | 'AGENT' | 'ADMIN';

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  role: RoleType;
}

const userService = {

  async getAllUsers(): Promise<UserResponse[]> {
    const response = await api.get('/admin/users');
    return response.data;
  },

  async updateUserRole(userId: string, role: RoleType) {
    const response = await api.put(`/admin/users/${userId}/role`, {
      role,
    });

    return response.data;
  },
};

export default userService;