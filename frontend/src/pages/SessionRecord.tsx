import { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowRightIcon, BarChart3Icon, FileTextIcon, NotebookPenIcon, SparklesIcon } from 'lucide-react';
import { viewSession, viewMemo, type ViewSessionResponse } from '../api/counsel';
import { viewClient, type ViewClientResponse } from '../api/clients';
import { formatTag } from '../utils/tags';
import manFace from '../assets/ljh/man_face.png';
import womanFace from '../assets/psy/woman_face.png';

function getClientFace(clientId: number): string {
  if (clientId === 1) return manFace;
  if (clientId === 2) return womanFace;
  return '';
}
import { Tag } from '../components/Tag';
import { Modal } from '../components/Modal';

type SessionData = ViewSessionResponse['data'];
type ClientData = ViewClientResponse['data'];

function formatDate(iso: string) {
  return iso.slice(2, 10).replace(/-/g, '.');
}

function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return s > 0 ? `${m}분 ${s}초` : `${m}분`;
}

export function SessionRecord() {
  const { sessionId = '' } = useParams();
  const navigate = useNavigate();

  const [session, setSession] = useState<SessionData | null>(null);
  const [clientDetail, setClientDetail] = useState<ClientData | null>(null);
  const [memoLines, setMemoLines] = useState<string[]>([]);
  const [memoOpen, setMemoOpen] = useState(false);

  useEffect(() => {
    if (!sessionId) return;
    viewSession(Number(sessionId)).then(async res => {
      const data = res.data;
      setSession(data);

      const [clientRes, memoRes] = await Promise.all([
        viewClient(data.client.clientId),
        viewMemo(Number(sessionId)),
      ]);
      setClientDetail(clientRes.data);
      if (memoRes.data.memo) {
        setMemoLines(memoRes.data.memo.split('\n').filter(Boolean));
      }
    });
  }, [sessionId]);

  if (!session || !clientDetail) {
    return (
      <div className="flex h-full items-center justify-center">
        <p className="text-sm text-ink-muted">불러오는 중…</p>
      </div>
    );
  }

  const links = [
    {
      to: `/sessions/${sessionId}/transcript`,
      icon: FileTextIcon,
      title: '축어록',
      desc: '그날의 상담이 어땠는지 되돌아보세요.',
    },
    {
      to: `/sessions/${sessionId}/report`,
      icon: BarChart3Icon,
      title: '평가 리포트',
      desc: '객관적 데이터로 상담을 돌아보세요.',
    },
  ];

  return (
    <div className="mx-auto w-full max-w-5xl px-8 py-10">
      <div className="flex items-start justify-between gap-6">
        <div>
          <p className="text-sm font-semibold text-brand-600">상담 번호 {session.counselNo}</p>
          <h1 className="mt-1.5 text-2xl font-bold text-ink">
            {clientDetail.clientName}님과의 {session.sessionRound}회기 상담은 이렇게 진행되었어요.
          </h1>
        </div>
        <button
          type="button"
          onClick={() => setMemoOpen(true)}
          className="inline-flex h-11 shrink-0 items-center gap-2 rounded-full border border-line bg-white px-5 text-sm font-semibold text-ink-soft transition-colors duration-150 ease-out hover:border-brand-300 hover:text-brand-700">
          <NotebookPenIcon className="h-4 w-4" />
          상담 메모
        </button>
      </div>

      <section className="mt-7 flex flex-wrap items-center gap-6 rounded-2xl border border-line bg-white p-6 shadow-card">
        {/* 임시: 로컬 이미지 사용. imageUrl 준비 후 아래 주석 해제하고 이 div/img 교체
        <img src={clientDetail.imageUrl} alt="" className="h-14 w-14 rounded-full object-cover" /> */}
        <div className="h-24 w-24 shrink-0 overflow-hidden rounded-2xl bg-brand-50">
          <img src={getClientFace(clientDetail.clientId)} alt="" className="h-full w-full object-cover" />
        </div>
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-lg font-bold text-ink">{clientDetail.clientName}</p>
            {clientDetail.tags.map(t => <Tag key={t}>{formatTag(t)}</Tag>)}
          </div>
          <p className="mt-1 text-sm text-ink-muted">
            {formatDate(session.createdAt)} · {session.sessionRound}회기
          </p>
        </div>
        <div className="ml-auto text-right">
          <p className="text-xs font-semibold text-ink-muted">총 진행 시간</p>
          <p className="text-3xl font-black tabular-nums text-brand-700">{formatDuration(session.duration)}</p>
        </div>
      </section>

      <section className="mt-5 rounded-2xl bg-brand-800 p-7 text-white">
        <div className="flex items-center gap-2 text-brand-200">
          <SparklesIcon className="h-4 w-4" />
          <h2 className="text-xs font-bold tracking-wide">AI 요약</h2>
        </div>
        <p className="mt-2.5 text-[1.05rem] font-medium leading-relaxed">{session.aiSummary}</p>
        <hr className="my-6 border-white/20" />
        <h3 className="text-xs font-bold text-brand-200">AI 한 줄 조언</h3>
        <p className="mt-2.5 text-[1.05rem] font-medium leading-relaxed">{session.aiAdvice}</p>
        <p className="mt-5 text-xs leading-relaxed text-brand-200">
          AI 요약과 조언은 참고용 관찰 기록이며, 슈퍼비전을 대신하지 않습니다.
        </p>
      </section>

      <ul className="mt-5 grid gap-4 md:grid-cols-2">
        {links.map(({ to, icon: Icon, title, desc }) => (
          <li key={title} className="flex">
            <Link
              to={to}
              className="group flex w-full flex-col rounded-2xl border border-line bg-white p-6 shadow-card transition-colors duration-150 ease-out hover:border-brand-300">
              <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand-50 text-brand-600">
                <Icon className="h-5 w-5" />
              </span>
              <span className="mt-4 text-base font-bold text-ink">{title}</span>
              <span className="mt-1.5 text-sm leading-relaxed text-ink-muted">{desc}</span>
              <span className="mt-auto inline-flex items-center gap-1.5 pt-6 text-sm font-semibold text-brand-600">
                다시 보러 가기
                <ArrowRightIcon className="h-4 w-4 transition-transform duration-150 ease-out group-hover:translate-x-0.5" />
              </span>
            </Link>
          </li>
        ))}
      </ul>

      <div className="mt-9 flex flex-wrap items-center justify-between gap-4">
        <Link
          to={`/cases/${session.counselNo}`}
          className="text-sm font-semibold text-brand-600 transition-colors duration-150 ease-out hover:text-brand-700">
          상담 번호 {session.counselNo} 전체 리포트 보기
        </Link>
        <button
          type="button"
          onClick={() => navigate(`/session/${clientDetail.clientId}`)}
          className="inline-flex h-12 items-center gap-2 rounded-full bg-brand-600 px-8 text-sm font-bold text-white shadow-glow transition-colors duration-150 ease-out hover:bg-brand-700">
          다음 회기 진행하기
          <ArrowRightIcon className="h-4 w-4" />
        </button>
      </div>

      <Modal open={memoOpen} onClose={() => setMemoOpen(false)} labelledBy="memo-title">
        <h2 id="memo-title" className="text-lg font-bold text-ink">
          상담 메모
        </h2>
        <ul className="mt-5 space-y-2.5 rounded-2xl bg-canvas p-5">
          {memoLines.map(line => (
            <li key={line} className="flex gap-2.5 text-sm leading-relaxed text-ink-soft">
              <span aria-hidden="true" className="mt-[0.45rem] h-1.5 w-1.5 shrink-0 rounded-full bg-brand-400" />
              {line}
            </li>
          ))}
        </ul>
      </Modal>
    </div>
  );
}
