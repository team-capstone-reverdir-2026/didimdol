import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRightIcon, ClockIcon } from 'lucide-react';
import { sessionList, type SessionListResponse } from '../api/counsel';
import { formatTag } from '../utils/tags';
import { Tag } from '../components/Tag';

type SessionItem = SessionListResponse['data']['sessions'][number];

const LIMIT = 10;

function formatDate(iso: string) {
  return iso.slice(2, 10).replace(/-/g, '.');
}

function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return s > 0 ? `${m}분 ${s}초` : `${m}분`;
}

export function History() {
  const [sessions, setSessions] = useState<SessionItem[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = async (cursor?: number) => {
    setLoading(true);
    try {
      const res = await sessionList({ idAfter: cursor, limit: LIMIT });
      setSessions(prev => cursor ? [...prev, ...res.data.sessions] : res.data.sessions);
      setNextCursor(res.data.nextCursor);
      setHasNext(res.data.hasNext);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  return (
    <div className="mx-auto w-full max-w-4xl px-8 py-10">
      <header className="mb-8">
        <h1 className="text-2xl font-bold text-ink">상담 기록</h1>
        <p className="mt-2 text-sm text-ink-muted">진행한 모든 회기를 확인할 수 있어요.</p>
      </header>

      {sessions.length === 0 && !loading ? (
        <p className="text-sm text-ink-muted">아직 진행한 상담이 없어요.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {sessions.map(s => (
            <li key={s.sessionId}>
              <Link
                to={`/sessions/${s.sessionId}`}
                className="flex items-center justify-between rounded-2xl border border-line bg-white p-5 shadow-card transition-colors duration-150 ease-out hover:border-brand-300">
                <div className="flex items-center gap-5">
                  <div className="flex flex-col gap-1">
                    <div className="flex items-center gap-2">
                      <span className="text-base font-bold text-ink">{s.client.clientName}</span>
                      <span className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-semibold text-brand-700">
                        {s.counselNo}번 상담 · {s.sessionRound}회기
                      </span>
                    </div>
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {s.client.tags.map(t => <Tag key={t}>{formatTag(t)}</Tag>)}
                    </div>
                    <div className="mt-2 flex items-center gap-3 text-xs text-ink-muted">
                      <span>{formatDate(s.createdAt)}</span>
                      <span className="flex items-center gap-1">
                        <ClockIcon className="h-3.5 w-3.5 text-brand-400" />
                        {formatDuration(s.duration)}
                      </span>
                    </div>
                  </div>
                </div>
                <ArrowRightIcon className="h-4 w-4 shrink-0 text-ink-muted" />
              </Link>
            </li>
          ))}
        </ul>
      )}

      {hasNext && (
        <div className="mt-6 flex justify-center">
          <button
            type="button"
            onClick={() => load(nextCursor ?? undefined)}
            disabled={loading}
            className="h-11 rounded-full border border-line px-7 text-sm font-semibold text-ink-soft transition-colors duration-150 ease-out hover:border-brand-400 hover:text-brand-600 disabled:opacity-50">
            {loading ? '불러오는 중…' : '더 보기'}
          </button>
        </div>
      )}
    </div>
  );
}
