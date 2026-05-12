import { Outlet } from 'react-router-dom';
import { Navbar } from '@/components/UI/Navbar';
import { Sidebar } from '@/components/UI/Sidebar';

export const MainLayout = () => {
  return (
    <div className="min-h-screen bg-[var(--color-bg-secondary)]">
      <Sidebar />
      <div className="lg:pl-64">
        <Navbar />
        <main className="p-4 md:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
};