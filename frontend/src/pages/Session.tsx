import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { ClipboardListIcon, NotebookPenIcon, SendIcon } from 'lucide-react';
import { makeCounsels, sessionMessage, sessionComplete, viewMemo, type ChatMessage } from '../api/counsel';
import { viewClient, type ViewClientResponse } from '../api/clients';
import { formatTag } from '../utils/tags';
import { Logo } from '../components/Logo';
import { Tag } from '../components/Tag';
import { Modal } from '../components/Modal';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { MemoPanel } from '../components/MemoPanel';
import { useCounselStream } from '../hooks/useCounselStream';
import MASCOT from '../assets/mascot.jpg';
import manNeutral from '../assets/ljh/man_neutral.png';
import manHappy from '../assets/ljh/man_happy.png';
import manSad from '../assets/ljh/man_sad.png';
import manAngry from '../assets/ljh/man_angry.png';
import womanNeutral from '../assets/psy/woman_neutral.png';
import womanHappy from '../assets/psy/woman_happy.png';
import womanSad from '../assets/psy/woman_sad.png';
import womanAngry from '../assets/psy/woman_angry.png';

type Emotion = 'NEUTRAL' | 'HAPPY' | 'SAD' | 'ANGRY';

function getClientStanding(clientId: number, emotion: Emotion): string {
  // 추후 백엔드 imageUrl 연동 시 이 함수 대신 API 응답 사용
  if (clientId === 1) {
    return { NEUTRAL: manNeutral, HAPPY: manHappy, SAD: manSad, ANGRY: manAngry }[emotion];
  }
  if (clientId === 2) {
    return { NEUTRAL: womanNeutral, HAPPY: womanHappy, SAD: womanSad, ANGRY: womanAngry }[emotion];
  }
  return '';
}

function format(total: number) {
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export function Session() {
  const { clientId = '' } = useParams();
  const navigate = useNavigate();

  const [sessionId, setSessionId] = useState<number>(0);
  const [sseTicket, setSseTicket] = useState<string>('');
  const [counselNo, setCounselNo] = useState<number>(0);
  const [sessionRound, setSessionRound] = useState<number>(0);
  const [nickname, setNickname] = useState<string>('');
  const [clientDetail, setClientDetail] = useState<ViewClientResponse['data'] | null>(null);

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [remaining, setRemaining] = useState(30 * 60);
  const [panel, setPanel] = useState<'intake' | 'memo' | null>(null);
  const [confirming, setConfirming] = useState(false);
  const [finished, setFinished] = useState(false);
  const [thinking, setThinking] = useState(false);
  const [memo, setMemo] = useState('');
  const endRef = useRef<HTMLDivElement>(null);

  // 내담자 정보 로드 (세션 시작 전)
  useEffect(() => {
    viewClient(Number(clientId)).then(res => setClientDetail(res.data));
  }, [clientId]);

  // 메모 로드
  useEffect(() => {
    if (!sessionId) return;
    viewMemo(sessionId).then(res => {
      if (res.data.memo) setMemo(res.data.memo);
    });
  }, [sessionId]);

  const { aiContent, currentEmotion, isCompleted, lastCompletedMessage, clearChat } = useCounselStream({
    sessionId,
    sseTicket,
  });

  const standingImage = getClientStanding(Number(clientId), currentEmotion);

  // AI 문장 완성 시 messages에 추가
  useEffect(() => {
    if (!lastCompletedMessage) return;
    setMessages(prev => [
      ...prev,
      { id: `a${prev.length}`, from: 'client', text: lastCompletedMessage.content },
    ]);
    clearChat();
    setThinking(false);
  }, [lastCompletedMessage]);

  // SSE가 session-closed 보내면 자동 종료
  useEffect(() => {
    if (isCompleted) setFinished(true);
  }, [isCompleted]);

  // 타이머
  useEffect(() => {
    if (finished) return;
    const id = window.setInterval(() => setRemaining(r => Math.max(0, r - 1)), 1000);
    return () => window.clearInterval(id);
  }, [finished]);

  // 스크롤
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, thinking]);

  const send = async () => {
    const text = draft.trim();
    if (!text) return;
    setDraft('');
    setMessages(prev => [...prev, { id: `c${prev.length}`, from: 'counselor', text }]);
    setThinking(true);

    if (!sessionId) {
      // 첫 메시지 → initialMessage로 makeCounsels 호출
      const startAt = new Date().toISOString();
      const res = await makeCounsels({ clientId: Number(clientId), initialMessage: text, startAt });
      const d = res.data;
      setSessionId(d.sessionId);
      setSseTicket(d.sseTicket);
      setCounselNo(d.counselNo);
      setSessionRound(d.sessionRound);
      setNickname(d.nickname);
      if (d.previousMemo) setMemo(d.previousMemo);
    } else {
      // 이후 메시지 → sessionMessage 호출
      await sessionMessage(sessionId, { message: text });
    }
  };

  const handleConfirmEnd = async () => {
    setConfirming(false);
    const endAt = new Date().toISOString();
    await sessionComplete(sessionId, { memo: memo || null, endAt });
    setFinished(true);
  };

  return (
    <div className="flex h-full w-full bg-canvas">
      <aside className="hidden w-[15.5rem] shrink-0 flex-col border-r border-line bg-white px-4 pb-6 pt-7 lg:flex">
        <div className="mb-8 px-2">
          <Logo size="md" />
        </div>
        <div className="rounded-2xl bg-brand-50 p-4">
          <p className="text-xs font-bold text-brand-700">진행 중인 회기</p>
          <p className="mt-2 text-sm font-semibold text-ink">
            {clientDetail?.clientName ?? '—'} 내담자
          </p>
          <p className="mt-1 text-xs text-ink-muted">{sessionRound}회기 · AI 시뮬레이션</p>
        </div>
        <div className="mt-auto space-y-2">
          <PanelButton
            icon={<ClipboardListIcon className="h-4 w-4" />}
            label="내담자 설문지"
            active={panel === 'intake'}
            onClick={() => setPanel(panel === 'intake' ? null : 'intake')} />
          <PanelButton
            icon={<NotebookPenIcon className="h-4 w-4" />}
            label="상담 메모"
            active={panel === 'memo'}
            onClick={() => setPanel(panel === 'memo' ? null : 'memo')} />
        </div>
      </aside>

      <div className="relative flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 shrink-0 items-center justify-between border-b border-line bg-white px-8">
          <div className="flex items-center gap-2.5">
            <span className="rounded-full bg-brand-50 px-3 py-1.5 text-xs font-bold text-brand-700">
              {counselNo}번 상담 · {sessionRound}회기
            </span>
            <span
              className="rounded-full bg-brand-600 px-3 py-1.5 text-xs font-bold tabular-nums text-white"
              aria-live="off">
              타이머 {format(remaining)}
            </span>
          </div>
          <span className="text-sm font-semibold text-ink-soft">{nickname} 상담자님</span>
        </header>

        <div className="flex min-h-0 flex-1">
          <div className="hidden w-[20rem] shrink-0 flex-col items-center justify-end border-r border-line bg-white px-6 pb-8 xl:flex">
            {clientDetail?.imageUrl && (
              <img
                src={clientDetail.imageUrl}
                alt={`${clientDetail.clientName} 내담자`}
                className="w-full rounded-2xl object-cover" />
            )}
            <div className="mt-5 w-full text-center">
              <p className="text-lg font-bold text-ink">{clientDetail?.clientName}</p>
              <div className="mt-3 flex flex-wrap justify-center gap-1.5">
                {clientDetail?.tags.map(t => <Tag key={t}>{formatTag(t)}</Tag>)}
              </div>
            </div>
          </div>

          <div className="flex min-w-0 flex-1 flex-col">
            <div className="relative min-h-0 flex-1">

              {/* 스탠딩 이미지 */}
              {standingImage && (
                <div className="pointer-events-none absolute inset-y-0 left-0 z-[1] hidden w-[18rem] sm:block md:w-[20rem] lg:w-[22rem]">
                  <img
                    src={standingImage}
                    alt={`${clientDetail?.clientName ?? ''} 내담자`}
                    draggable={false}
                    className="absolute bottom-0 left-0 h-[96%] w-auto max-w-[145%] select-none object-contain object-left-bottom" />
                </div>
              )}

              <div
                className={`relative z-[2] h-full overflow-y-auto scroll-slim px-8 ${
                  standingImage
                    ? 'pt-[min(9rem,22%)] pb-8 sm:pl-[18rem] md:pl-[20rem] lg:pl-[22rem]'
                    : 'py-8'
                }`}>
                <ul className={`flex max-w-2xl flex-col gap-5 ${standingImage ? 'mr-auto' : 'mx-auto'}`}>
                  {messages.map(m => (
                    <li key={m.id} className={m.from === 'client' ? 'flex justify-start' : 'flex justify-end'}>
                      <motion.div
                        initial={{ opacity: 0, y: 8, scale: 0.98 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        transition={{ duration: 0.22, ease: [0.23, 1, 0.32, 1] }}
                        className={`relative px-5 py-4 text-[0.95rem] leading-relaxed ${
                          m.from === 'client'
                            ? `rounded-2xl rounded-bl-md border border-line bg-white text-ink ${
                                standingImage ? 'max-w-sm' : 'max-w-[80%] rounded-tl-md'
                              }`
                            : 'max-w-[80%] rounded-2xl rounded-tr-md bg-brand-600 text-white'
                        }`}>
                        {m.from === 'client' && standingImage && (
                          <span
                            aria-hidden="true"
                            className="absolute -left-[6px] top-6 h-3 w-3 rotate-45 border-b border-l border-line bg-white" />
                        )}
                        {m.text}
                      </motion.div>
                    </li>
                  ))}

                  {/* 스트리밍 중인 AI 발화 */}
                  <AnimatePresence>
                    {aiContent && (
                      <motion.li
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="flex justify-start">
                        <div className={`relative px-5 py-4 text-[0.95rem] leading-relaxed rounded-2xl rounded-bl-md border border-line bg-white text-ink ${standingImage ? 'max-w-sm' : 'max-w-[80%] rounded-tl-md'}`}>
                          {standingImage && (
                            <span aria-hidden="true" className="absolute -left-[6px] top-6 h-3 w-3 rotate-45 border-b border-l border-line bg-white" />
                          )}
                          {aiContent}
                        </div>
                      </motion.li>
                    )}
                    {thinking && !aiContent && (
                      <motion.li
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        transition={{ duration: 0.15, ease: 'easeOut' }}
                        className="flex justify-start">
                        <span className={`relative rounded-2xl rounded-bl-md border border-line bg-white px-5 py-4 text-sm text-ink-muted ${standingImage ? '' : 'rounded-tl-md'}`}>
                          {standingImage && (
                            <span aria-hidden="true" className="absolute -left-[6px] top-5 h-3 w-3 rotate-45 border-b border-l border-line bg-white" />
                          )}
                          {clientDetail?.clientName ?? '내담자'}님이 답변을 고르고 있어요…
                        </span>
                      </motion.li>
                    )}
                  </AnimatePresence>
                </ul>
                <div ref={endRef} />
              </div>
            </div>

            <div className="shrink-0 border-t border-line bg-white px-8 py-5">
              <div className="mx-auto flex max-w-2xl items-end gap-3">
                <div className="flex gap-2 xl:hidden">
                  <IconOnly label="내담자 설문지" onClick={() => setPanel('intake')}>
                    <ClipboardListIcon className="h-4 w-4" />
                  </IconOnly>
                  <IconOnly label="상담 메모" onClick={() => setPanel('memo')}>
                    <NotebookPenIcon className="h-4 w-4" />
                  </IconOnly>
                </div>
                <label htmlFor="utterance" className="sr-only">상담자 발화 입력</label>
                <textarea
                  id="utterance"
                  rows={1}
                  value={draft}
                  onChange={e => setDraft(e.target.value)}
                  onKeyDown={e => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      send();
                    }
                  }}
                  placeholder="어떤 부분부터 이야기해볼까요?"
                  className="max-h-32 min-h-[3rem] flex-1 resize-none rounded-2xl border border-line bg-canvas px-4 py-3.5 text-sm text-ink placeholder:text-ink-faint transition-colors duration-150 ease-out focus:border-brand-400 focus:bg-white" />
                <button
                  type="button"
                  onClick={send}
                  aria-label="발화 전송"
                  className="grid h-12 w-12 shrink-0 place-items-center rounded-full bg-brand-600 text-white transition-colors duration-150 ease-out hover:bg-brand-700">
                  <SendIcon className="h-[1.125rem] w-[1.125rem]" />
                </button>
                <button
                  type="button"
                  onClick={() => setConfirming(true)}
                  className="h-12 shrink-0 rounded-full border border-line px-5 text-sm font-semibold text-ink-soft transition-colors duration-150 ease-out hover:border-danger hover:text-danger">
                  상담 종료
                </button>
              </div>
            </div>
          </div>
        </div>

        <MemoPanel open={panel === 'intake'} onClose={() => setPanel(null)} title="내담자 설문지">
          {clientDetail && (
            <>
              <dl className="grid grid-cols-[3rem_1fr] gap-y-2 text-sm">
                <dt className="text-ink-muted">이름</dt>
                <dd className="font-medium text-ink">{clientDetail.clientName}</dd>
              </dl>
              <div className="mt-4 flex flex-wrap gap-1.5">
                {clientDetail.tags.map(t => <Tag key={t}>{formatTag(t)}</Tag>)}
              </div>
              <div className="mt-6 space-y-5">
                <IntakeBlock label="신청 경위" value={clientDetail.referralReason} />
                <IntakeBlock label="호소 문제 영역" value={clientDetail.problemArea} />
                <IntakeBlock label="상담 신청 이유" value={clientDetail.counselingReason} />
              </div>
            </>
          )}
        </MemoPanel>

        <MemoPanel open={panel === 'memo'} onClose={() => setPanel(null)} title="상담 메모">
          <textarea
            aria-label="상담 메모 작성"
            value={memo}
            onChange={e => setMemo(e.target.value)}
            className="h-[70vh] w-full resize-none rounded-2xl border border-line bg-canvas p-4 text-sm leading-loose text-ink-soft transition-colors duration-150 ease-out focus:border-brand-400 focus:bg-white" />
        </MemoPanel>
      </div>

      <ConfirmDialog
        open={confirming}
        title={<>상담을<br />종료하시겠습니까?</>}
        description="종료 후에는 발화를 추가할 수 없습니다."
        onCancel={() => setConfirming(false)}
        onConfirm={handleConfirmEnd} />

      <Modal open={finished} onClose={() => navigate('/home')} showClose={false} labelledBy="done-title">
        <div className="text-center">
          <motion.img
            src={MASCOT}
            alt=""
            className="mx-auto h-28 w-28 object-contain"
            initial={{ y: 0 }}
            animate={{ y: [-4, 4, -4] }}
            transition={{ duration: 2.4, repeat: Infinity, ease: 'easeInOut' }} />
          <h2 id="done-title" className="mt-4 text-xl font-bold text-ink">
            수고하셨습니다. 지금 기분은 어떠신가요?
          </h2>
          <p className="mt-2 text-sm text-ink-muted">잠시 숨을 고르고 오늘의 회기를 마무리해요.</p>

          <div className="mt-6 rounded-2xl bg-brand-50 py-6">
            <p className="text-xs font-semibold text-brand-700">총 진행 시간</p>
            <p className="mt-1.5 text-4xl font-black tabular-nums text-brand-700">
              {format(30 * 60 - remaining)}
            </p>
          </div>

          <p className="mt-5 rounded-2xl bg-canvas p-4 text-xs leading-relaxed text-ink-soft">
            축어록과 평가 리포트를 정리하고 있어요. 화면을 나가도 계속 진행되며, 완료되면 상담기록에서
            확인할 수 있습니다.
          </p>

          <button
            type="button"
            onClick={() => navigate('/home')}
            className="mt-6 h-12 w-full rounded-full bg-brand-600 text-sm font-bold text-white transition-colors duration-150 ease-out hover:bg-brand-700">
            메인 화면으로
          </button>
        </div>
      </Modal>
    </div>
  );
}

function PanelButton({
  icon, label, active, onClick,
}: {
  icon: React.ReactNode;
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex w-full items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm font-semibold transition-colors duration-150 ease-out ${
        active ? 'bg-brand-600 text-white' : 'text-ink-soft hover:bg-brand-50 hover:text-brand-700'
      }`}>
      {icon}
      {label}
    </button>
  );
}

function IconOnly({
  label, onClick, children,
}: {
  label: string;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      aria-label={label}
      onClick={onClick}
      className="grid h-12 w-12 place-items-center rounded-full border border-line text-ink-soft transition-colors duration-150 ease-out hover:bg-brand-50 hover:text-brand-700">
      {children}
    </button>
  );
}

function IntakeBlock({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <h3 className="text-sm font-bold text-brand-700">{label}</h3>
      <p className="mt-1.5 text-sm leading-relaxed text-ink-soft">{value}</p>
    </div>
  );
}
