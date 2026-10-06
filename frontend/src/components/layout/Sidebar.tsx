import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { HomeIcon, LogOutIcon, MessagesSquareIcon, NotebookTextIcon } from 'lucide-react';
import { Logo } from '../Logo';
import { signOut, clearSilentRefresh } from '../../api/auth';

const nav = [
{ to: '/home', label: '홈', icon: HomeIcon },
{ to: '/clients', label: '상담', icon: MessagesSquareIcon },
{ to: '/sessions', label: '상담기록', icon: NotebookTextIcon }];


export function Sidebar() {
  const navigate = useNavigate();

  return (
    <nav
      aria-label="주요 메뉴"
      className="flex w-[15.5rem] shrink-0 flex-col border-r border-line bg-white px-4 pb-6 pt-7">
      
      <NavLink to="/home" className="mb-9 px-2" aria-label="디딤돌 홈">
        <Logo size="md" />
      </NavLink>

      <ul className="flex flex-col gap-1">
        {nav.map(({ to, label, icon: Icon }) =>
        <li key={to}>
            <NavLink
            to={to}
            className={({ isActive }) =>
            `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition-colors duration-150 ease-out ${
            isActive ?
            'bg-brand-600 text-white shadow-glow' :
            'text-ink-soft hover:bg-brand-50 hover:text-brand-700'}`

            }>
            
              <Icon className="h-[1.15rem] w-[1.15rem]" strokeWidth={2} />
              {label}
            </NavLink>
          </li>
        )}
      </ul>

      <div className="mt-auto rounded-2xl bg-brand-50 p-4">
        <p className="text-[0.7rem] font-semibold text-brand-700">수련 전용 시뮬레이션</p>
        <p className="mt-1.5 text-[0.7rem] leading-relaxed text-ink-muted">
          디딤돌의 내담자는 AI입니다. 실제 상담과 슈퍼비전을 대체하지 않습니다.
        </p>
      </div>

      <button
        type="button"
        onClick={async () => {
          const accessToken = localStorage.getItem('accessToken');
          if (accessToken) {
            await signOut({ accessToken }).catch(() => {});
          }
          clearSilentRefresh();
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          navigate('/');
        }}
        className="mt-3 flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold text-ink-muted transition-colors duration-150 ease-out hover:bg-brand-50 hover:text-brand-700">
        <LogOutIcon className="h-[1.15rem] w-[1.15rem]" strokeWidth={2} />
        로그아웃
      </button>
    </nav>);

}