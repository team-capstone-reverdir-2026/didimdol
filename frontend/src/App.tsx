import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { Login } from './pages/Login';
import { SignUp } from './pages/SignUp';
import { Onboarding } from './pages/Onboarding';
import { Home } from './pages/Home';
import { Clients } from './pages/Clients';
import { Session } from './pages/Session';
import { History } from './pages/History';
import { SessionRecord } from './pages/SessionRecord';
import client from './api/client';
import { setupInterceptors } from './api/interceptors';

setupInterceptors(client, () => {
  window.location.href = '/';
});

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 인증 페이지 */}
        <Route path="/" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/onboarding" element={<Onboarding />} />

        {/* 상담 진행 (전체 화면, AppLayout 없음) */}
        <Route path="/session/:clientId" element={<Session />} />

        {/* 앱 페이지 (AppLayout 적용) */}
        <Route element={<AppLayout />}>
          <Route path="/home" element={<Home />} />
          <Route path="/clients" element={<Clients />} />
          <Route path="/sessions" element={<History />} />
          <Route path="/sessions/:sessionId" element={<SessionRecord />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
