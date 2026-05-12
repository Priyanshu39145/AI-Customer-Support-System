import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { setTokens, setUserRole, setUserData } from '@/services/api';

/**
 * OAuth2 Callback Page.
 *
 * This page handles the callback from Google OAuth2 login.
 * The backend redirects here with tokens in URL query parameters.
 *
 * Flow:
 * 1. Extract accessToken, refreshToken, user from URL params
 * 2. Store tokens in localStorage via setTokens()
 * 3. Store user data in localStorage
 * 4. Reload app to hydrate AuthContext properly
 */
export const OAuth2CallbackPage = () => {
  const [searchParams] = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Extract tokens from URL parameters
    const accessToken = searchParams.get('accessToken');
    const refreshToken = searchParams.get('refreshToken');
    const userData = searchParams.get('user');

    if (accessToken && refreshToken) {
      // Step 1: Store tokens in localStorage
      setTokens(accessToken, refreshToken);

      // Step 2: Parse and store user data
      if (userData) {
        try {
          const user = JSON.parse(decodeURIComponent(userData));

          setUserRole(user.role);
          setUserData(user);

          // Step 3: Reload app to hydrate AuthContext properly
          switch (user.role) {
            case 'ADMIN':
              window.location.href = '/admin/dashboard';
              break;

            case 'AGENT':
              window.location.href = '/agent/dashboard';
              break;

            default:
              window.location.href = '/dashboard';
          }
        } catch {
          setError('Failed to parse user data');
        }
      } else {
        window.location.href = '/dashboard';
      }
    } else {
      setError('No tokens received from OAuth2 provider');
    }
  }, [searchParams]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">
            OAuth2 Error
          </h1>

          <p className="text-gray-600 dark:text-gray-400">
            {error}
          </p>

          <a
            href="/login"
            className="text-primary-600 hover:underline mt-4 block"
          >
            Back to Login
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4" />

        <p className="text-gray-600 dark:text-gray-400">
          Completing OAuth2 login...
        </p>
      </div>
    </div>
  );
};

export default OAuth2CallbackPage;