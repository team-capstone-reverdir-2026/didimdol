import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRightIcon } from 'lucide-react';
import { AuthShell } from '../components/AuthShell';
import { Field } from '../components/Field';
import { MASCOT } from '../data/clients';

export function Onboarding() {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');

  return (
    <AuthShell
      title="어떻게 불러드릴까요?"
      subtitle="상담 화면과 기록에 표시될 이름입니다. 언제든 변경할 수 있어요.">
      
      <form
        className="flex flex-col gap-5"
        onSubmit={(e) => {
          e.preventDefault();
          navigate('/home');
        }}>
        
        <div className="flex items-center gap-4 rounded-2xl bg-brand-50 p-4">
          <img src={MASCOT} alt="" className="h-14 w-14 rounded-xl object-contain" />
          <p className="text-sm leading-relaxed text-brand-800">
            {nickname.trim() ? `${nickname.trim()} 상담자님, 반갑습니다.` : '반갑습니다. 닉네임을 입력해 주세요.'}
          </p>
        </div>

        <Field
          label="닉네임"
          id="nickname"
          placeholder="예) 김수미"
          value={nickname}
          onChange={setNickname} />
        

        <button
          type="submit"
          disabled={!nickname.trim()}
          className="mt-1 inline-flex h-12 w-full items-center justify-center gap-2 rounded-full bg-brand-600 text-sm font-bold text-white transition-colors duration-150 ease-out hover:bg-brand-700 disabled:bg-brand-200 disabled:text-white">
          
          시작하기
          <ArrowRightIcon className="h-4 w-4" />
        </button>
      </form>
    </AuthShell>);

}