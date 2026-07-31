import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';

/** Completes OAuth login by reading the authenticated user from the cookie session. */
export const OAuth2CallbackPage = () => {
  const { completeOAuthLogin } = useAuth();
  const [error, setError] = useState(false);

  useEffect(() => {
    completeOAuthLogin().catch(() => setError(true));
  }, [completeOAuthLogin]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">OAuth2 Error</h1>
          <p className="text-gray-600 dark:text-gray-400">Unable to complete sign-in.</p>
          <a href="/login" className="text-primary-600 hover:underline mt-4 block">Back to Login</a>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4" />
        <p className="text-gray-600 dark:text-gray-400">Completing OAuth2 login...</p>
      </div>
    </div>
  );
};

export default OAuth2CallbackPage;
