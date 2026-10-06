import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShieldAlertIcon } from 'lucide-react';
import { AuthShell } from '../components/AuthShell';
import { Field } from '../components/Field';
import { Modal } from '../components/Modal';
import { signUp } from '../api/auth';

const notices = [
'디딤돌의 내담자는 실제 사람이 아닌 AI 시뮬레이션입니다.',
'이 서비스는 상담 수련 목적이며, 실제 상담·진단·치료를 대체할 수 없습니다.',
'AI가 제공하는 요약·평가는 참고 자료이며, 슈퍼비전을 대신하지 않습니다.',
'실제 내담자의 식별 정보를 입력하지 마세요.'];
const [username, setUsername] = useState('');
const [password, setPassword] = useState('');
const [confirmPW, setConfirmPW] = useState('');
const [nickname, setNickname] = useState('');
const [error, setError] = useState('');


export function SignUp() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  return (
    <>
      <AuthShell
        title="회원가입"
        subtitle="수련용 계정을 만들고 AI 내담자와 연습을 시작하세요."
        footer={
        <>
            이미 계정이 있으신가요?{' '}
            <Link to="/" className="font-semibold text-brand-600 hover:text-brand-700">
              로그인
            </Link>
          </>
        }>
        
        <form
          className="flex flex-col gap-4"
          onSubmit={async (e) => {
            e.preventDefault();
            if (password !== confirmPW) {
              setError('비밀번호가 일치하지 않습니다.');
              return;
            }
            setError(' ');
            setOpen(true);
          }}>
          
          <Field label="아이디" id="signup-username" type="username" placeholder="counselor123" value={username} onChange={setUsername} />
          <Field
            label="비밀번호"
            id="signup-password"
            type="password"
            placeholder="8자 이상"
            hint="영문·숫자를 포함해 8자 이상 입력해 주세요." value={password} onChange={setPassword} />
          <Field label="비밀번호 확인" id="signup-password2" type="password" placeholder="••••••••" value={confirmPW} onChange={setConfirmPW}/>
          <Field label="닉네임" id="signup-nickname" type="nickname" placeholder="김수미" value={nickname} onChange={setNickname}/>
          {error && <p className="text-sm text-red-500">{error}</p>}

          <button
            type="submit"
            className="mt-2 h-12 w-full rounded-full bg-brand-600 text-sm font-bold text-white shadow-glow transition-colors duration-150 ease-out hover:bg-brand-700">
            
            가입하기
          </button>
        </form>
      </AuthShell>

      <Modal open={open} onClose={() => setOpen(false)} labelledBy="notice-title" showClose={false}>
        <div className="flex items-start gap-3">
          <span className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-brand-100 text-brand-700">
            <ShieldAlertIcon className="h-5 w-5" />
          </span>
          <div>
            <h2 id="notice-title" className="text-lg font-bold text-ink">
              시작하기 전에 확인해 주세요
            </h2>
            <p className="mt-1 text-sm text-ink-muted">
              디딤돌을 사용하기 위해 아래 내용에 동의가 필요합니다.
            </p>
          </div>
        </div>

        <ul className="mt-6 space-y-3 rounded-2xl bg-canvas p-5">
          {notices.map((n) =>
          <li key={n} className="flex gap-2.5 text-sm leading-relaxed text-ink-soft">
              <span aria-hidden="true" className="mt-[0.45rem] h-1.5 w-1.5 shrink-0 rounded-full bg-brand-400" />
              {n}
            </li>
          )}
        </ul>

        <button
          type="button"
          onClick={async () => 
            {try{
              await signUp({ username, password, nickname });
              navigate('/');
            } catch {
              setOpen(false);
              setError('회원가입에 실패했습니다. 다시 시도해 주세요.');
            }
            }}
          className="mt-6 h-12 w-full rounded-full bg-brand-600 text-sm font-bold text-white transition-colors duration-150 ease-out hover:bg-brand-700">
          확인했습니다
        </button>
      </Modal>
    </>);

}