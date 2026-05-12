import api from './api';

const companyPolicyService = {
  async uploadPolicy(formData: FormData): Promise<string> {
    const response = await api.post(
      '/admin/company-policy/upload',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );

    return response.data;
  },
};

export default companyPolicyService;