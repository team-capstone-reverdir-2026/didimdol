import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRightIcon, ClockIcon } from 'lucide-react';
import { sessionList, type SessionListResponse } from '../api/counsel';
import { formatTag } from '../utils/tags';
import { viewClient } from '../api/clients';
import { Tag } from '../components/Tag';
import MASCOT from '../assets/mascot.png';

type SessionItem = SessionListResponse['data']['sessions'][number];

function formatDate(iso: string) {
  return iso.slice(2, 10).replace(/-/g, '.');
}

function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}분 ${s}초`;
}

export function Home() {
  const [sessions, setSessions] = useState<SessionItem[]>([]);
  const [nickname, setNickname] = useState('');
  const [clientImages, setClientImages] = useState<Record<number, string>>({});

  useEffect(() => {
    sessionList({ limit: 6 }).then(async res => {
      setSessions(res.data.sessions);
      setNickname(res.data.nickname);

      const uniqueClientIds = [...new Set(res.data.sessions.map(s => s.client.clientId))];
      const results = await Promise.all(uniqueClientIds.map(id => viewClient(id)));
      const imageMap: Record<number, string> = {};
      results.forEach((r, i) => {
        imageMap[uniqueClientIds[i]] = r.data.imageUrl;
      });
      setClientImages(imageMap);
    });
  }, []);

  return (
    <div className="mx-auto w-full max-w-6xl px-8 py-10">
      <section className="relative overflow-hidden rounded-3xl bg-brand-800 px-9 py-10 text-white">
        <div className="relative z-10 max-w-xl">
          <p className="text-sm font-semibold text-brand-200">
            {nickname} 상담자님, 오늘도 한 걸음
          </p>
          <h1 className="mt-3 text-3xl font-bold leading-snug">
            부담 없이 연습하고,
            <br />
            남은 기록으로 스스로를 점검하세요.
          </h1>
          <p className="mt-4 text-sm leading-relaxed text-brand-100">
            AI 내담자와의 회기는 축어록과 평가 리포트로 남습니다. 실제 상담을 대체하지 않는 수련용
            공간이에요.
          </p>
          <Link
            to="/clients"
            className="mt-7 inline-flex h-12 items-center gap-2 rounded-full bg-white px-7 text-sm font-bold text-brand-700 transition-colors duration-150 ease-out hover:bg-brand-50">
            상담하러 가기
            <ArrowRightIcon className="h-4 w-4" />
          </Link>
        </div>
        <img
          src={MASCOT}
          alt=""
          className="pointer-events-none absolute -bottom-6 right-6 hidden h-52 w-52 object-contain opacity-95 lg:block" />
      </section>

      <section className="mt-12">
        <div className="mb-5 flex items-end justify-between">
          <div>
            <h2 className="text-xl font-bold text-ink">최근 진행한 상담</h2>
            <p className="mt-1 text-sm text-ink-muted">마지막으로 남긴 회기부터 이어서 볼 수 있어요.</p>
          </div>
          <Link
            to="/sessions"
            className="text-sm font-semibold text-brand-600 transition-colors duration-150 ease-out hover:text-brand-700">
            전체 기록 보기
          </Link>
        </div>

        <ul className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {sessions.map((s) => (
            <li key={s.sessionId} className="flex">
              <article className="flex w-full flex-col rounded-2xl border border-line bg-white p-5 shadow-card">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-ink-muted">{formatDate(s.createdAt)}</span>
                  <span className="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700">
                    상담 번호 {s.counselNo}
                  </span>
                </div>

                <div className="mt-4 flex items-center gap-3">
                  {clientImages[s.client.clientId] && (
                    <img
                      src={clientImages[s.client.clientId]}
                      alt=""
                      className="h-11 w-11 rounded-full object-cover" />
                  )}
                  <div>
                    <p className="text-base font-bold text-ink">{s.client.clientName}</p>
                    <p className="text-xs text-ink-muted">{s.sessionRound}회기</p>
                  </div>
                </div>

                <div className="mt-3.5 flex flex-wrap gap-1.5">
                  {s.client.tags.map((t) => (
                    <Tag key={t}>{formatTag(t)}</Tag>
                  ))}
                </div>

                <div className="mt-4 flex items-center gap-1.5 text-sm text-ink-soft">
                  <ClockIcon className="h-4 w-4 text-brand-400" />
                  진행 시간 {formatDuration(s.duration)}
                </div>

                <Link
                  to={`/sessions/${s.sessionId}`}
                  className="mt-auto inline-flex items-center gap-1.5 pt-5 text-sm font-semibold text-brand-600 transition-colors duration-150 ease-out hover:text-brand-700">
                  기록 보러가기
                  <ArrowRightIcon className="h-4 w-4" />
                </Link>
              </article>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
