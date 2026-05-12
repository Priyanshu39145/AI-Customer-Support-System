import { ChevronDown, Sun, Moon, LogOut, User, Settings } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useTheme } from '@/contexts/ThemeContext';
import clsx from 'clsx';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getInitials = (name: string) => {
    return name
      ?.split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2) || 'U';
  };

  return (
    <header className="h-16 border-b border-[var(--color-border)] bg-[var(--color-bg)] px-6 flex items-center justify-between sticky top-0 z-40">
      {/* Spacer for mobile menu button */}
      <div className="w-10 lg:w-0" />

      {/* Page Title Area - can be customized per page */}
      <div className="flex-1 lg:flex-none">
        <h1 className="text-lg font-semibold text-[var(--color-text)] hidden lg:block">
          AI Customer Support
        </h1>
      </div>

      {/* Right Side Actions */}
      <div className="flex items-center gap-2">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className={clsx(
            'p-2.5 rounded-lg transition-all duration-200',
            'hover:bg-[var(--color-bg-tertiary)] text-[var(--color-text-secondary)]',
            'focus:outline-none focus:ring-2 focus:ring-primary-500/20'
          )}
          title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {theme === 'dark' ? (
            <Sun className="w-5 h-5" />
          ) : (
            <Moon className="w-5 h-5" />
          )}
        </button>

        {/* User Menu */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className={clsx(
              'flex items-center gap-2.5 px-2.5 py-1.5 rounded-lg transition-all duration-200',
              'hover:bg-[var(--color-bg-tertiary)]',
              'focus:outline-none focus:ring-2 focus:ring-primary-500/20'
            )}
          >
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white text-xs font-medium shadow-sm">
              {getInitials(user?.name || '')}
            </div>
            <div className="hidden md:block text-left">
              <span className="text-sm font-medium text-[var(--color-text)] block">
                {user?.name}
              </span>
              <span className="text-xs text-[var(--color-text-tertiary)]">
                {user?.role}
              </span>
            </div>
            <ChevronDown
              className={clsx(
                'w-4 h-4 text-[var(--color-text-tertiary)] transition-transform duration-200',
                dropdownOpen && 'rotate-180'
              )}
            />
          </button>

          {/* Dropdown Menu */}
          {dropdownOpen && (
            <div className="dropdown animate-scale-in">
              {/* User Info Header */}
              <div className="px-4 py-3 border-b border-[var(--color-border)]">
                <p className="text-sm font-medium text-[var(--color-text)]">
                  {user?.name}
                </p>
                <p className="text-xs text-[var(--color-text-tertiary)]">
                  {user?.email}
                </p>
                <span className="inline-flex items-center px-2 py-0.5 mt-2 rounded text-xs font-medium bg-primary-50 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400">
                  {user?.role}
                </span>
              </div>

              {/* Menu Items */}
              <div className="py-1">
                <button
                  onClick={() => {
                    setDropdownOpen(false);
                    // Future: navigate to profile
                  }}
                  className="dropdown-item w-full"
                >
                  <User className="w-4 h-4" />
                  Profile
                </button>
                <button
                  onClick={() => {
                    setDropdownOpen(false);
                    // Future: navigate to settings
                  }}
                  className="dropdown-item w-full"
                >
                  <Settings className="w-4 h-4" />
                  Settings
                </button>
              </div>

              {/* Logout */}
              <div className="border-t border-[var(--color-border)] py-1">
                <button
                  onClick={() => {
                    setDropdownOpen(false);
                    logout();
                  }}
                  className="dropdown-item-danger w-full"
                >
                  <LogOut className="w-4 h-4" />
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};