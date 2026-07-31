import api from './api';

const companyPolicyService = {
  async uploadPolicy(formData: FormData): Promise<string> {

      console.log(formData instanceof FormData);

        for (const [key, value] of formData.entries()) {

          console.log(key, value);

        }

    const response = await api.post(
      '/admin/company-policy/upload',
      formData
    );

    return response.data;
  },
};

export default companyPolicyService;