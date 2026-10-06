import { Link, useNavigate } from 'react-router-dom';
import { AuthShell } from '../components/AuthShell';
import { Field } from '../components/Field';
import { useState } from 'react';
import { signIn, scheduleSilentRefresh } from '../api/auth';

export function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  return (
    <AuthShell
      title="로그인"
      subtitle="상담 수련을 이어서 진행해 보세요."
      footer={
      <>
          아직 계정이 없으신가요?{' '}
          <Link to="/signup" className="font-semibold text-brand-600 hover:text-brand-700">
            회원가입
          </Link>
        </>
      }>
      
      <form
        className="flex flex-col gap-4"
        onSubmit={async(e) => {
          e.preventDefault();
          try {
            const res = await signIn({ username, password });
            localStorage.setItem('accessToken', res.data.accessToken);
            localStorage.setItem('refreshToken', res.data.refreshToken);
            localStorage.setItem('expiresIn', res.data.expiresIn.toString());
            localStorage.setItem('expiresAt', res.data.expiresAt);
            scheduleSilentRefresh(res.data.expiresIn);
            navigate('/home');
          } catch (error) {
            setError('아이디 또는 비밀번호가 올바르지 않습니다.');
          }
        }}>
        
        <Field label="아이디" id="login-username" type="username" placeholder="counselor123" value={username} onChange={setUsername} />
        <Field label="비밀번호" id="login-password" type="password" placeholder="••••••••" value={password} onChange={setPassword} />
        {error && <p className="text-sm text-red-500">{error}</p>}
        <button
          type="submit"
          className="mt-2 h-12 w-full rounded-full bg-brand-600 text-sm font-bold text-white shadow-glow transition-colors duration-150 ease-out hover:bg-brand-700">
          
          로그인
        </button>
      </form>
    </AuthShell>);

}