import { Outlet } from 'react-router-dom';
import { Sidebar } from '../layout/Sidebar';

export const COUNSELOR_NAME = '김수미';

export function AppLayout() {
  return (
    <div className="flex h-full w-full bg-canvas">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 shrink-0 items-center justify-end border-b border-line bg-white/80 px-8 backdrop-blur">
          <div className="flex items-center gap-3">
            <span className="text-sm font-semibold text-ink-soft">
              {COUNSELOR_NAME} 상담자님
            </span>
            <span
              aria-hidden="true"
              className="grid h-9 w-9 place-items-center rounded-full bg-brand-100 text-sm font-bold text-brand-700">
              
              김
            </span>
          </div>
        </header>
        <main className="min-w-0 flex-1 overflow-y-auto scroll-slim">
          <Outlet />
        </main>
      </div>
    </div>);

}