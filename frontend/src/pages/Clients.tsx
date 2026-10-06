import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRightIcon } from 'lucide-react';
import { clientList, viewClient, type ViewClientResponse, type ClientSummary } from '../api/clients';
import { Tag } from '../components/Tag';
import { Modal } from '../components/Modal';

export function Clients() {
  const navigate = useNavigate();
  const [clients, setClients] = useState<ClientSummary[]>([]);
  const [selected, setSelected] = useState<ViewClientResponse['data'] | null>(null);

  useEffect(() => {
    clientList().then(res => setClients(res.data.clients));
  }, []);

  return (
    <div className="mx-auto w-full max-w-6xl px-8 py-10">
      <header>
        <h1 className="text-2xl font-bold text-ink">오늘의 상담을 시작해 보세요.</h1>
        <p className="mt-2 text-sm text-ink-muted">
          내담자를 선택하면 접수 면접 내용을 확인한 뒤 회기를 시작할 수 있어요.
        </p>
      </header>

      <ul className="mt-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {clients.map((c) =>
        <li key={c.clientId} className="flex">
            <button
            type="button"
            onClick={async () => {
              const res = await viewClient(c.clientId);
              setSelected(res.data);
            }}
            className="group flex w-full flex-col overflow-hidden rounded-2xl border border-line bg-white text-left shadow-card transition-colors duration-150 ease-out hover:border-brand-300">
            
              <span className="block aspect-square w-full overflow-hidden bg-brand-50">
                <img
                src={c.imageUrl}
                alt={`${c.clientName} 내담자`}
                className="h-full w-full object-cover transition-transform duration-200 ease-out group-hover:scale-[1.03]" />
              
              </span>
              <span className="flex flex-1 flex-col p-5">
                <span className="flex items-baseline gap-2">
                  <span className="text-base font-bold text-ink">{c.clientName}</span>
                  {/*<span className="text-xs text-ink-muted">
                    {c.age}세 · {c.job}
                  </span>*/}
                </span>
                <span className="mt-3 flex flex-wrap gap-1.5">
                  {c.tags.map((t) =>
                <Tag key={t}>{t}</Tag>
                )}
                </span>
                <span className="mt-auto inline-flex items-center gap-1.5 pt-5 text-sm font-semibold text-brand-600">
                  접수 내용 보기
                  <ArrowRightIcon className="h-4 w-4 transition-transform duration-150 ease-out group-hover:translate-x-0.5" />
                </span>
              </span>
            </button>
          </li>
        )}
      </ul>

      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        size="lg"
        labelledBy="client-detail-title">
        
        {selected &&
        <div>
            <div className="flex flex-col gap-6 sm:flex-row">
              <img
              src={selected.imageUrl}
              alt={`${selected.clientName} 내담자`}
              className="h-40 w-40 shrink-0 rounded-2xl object-cover" />
            
              <div className="pt-1">
                <h2 id="client-detail-title" className="text-2xl font-bold text-ink">
                  {selected.clientName}
                </h2>
                <dl className="mt-4 grid grid-cols-[3.5rem_1fr] gap-y-2 text-sm">
                  <dt className="text-ink-muted">나이</dt>
                  <dd className="font-medium text-ink">{selected.age}세</dd>
                  <dt className="text-ink-muted">직업</dt>
                  <dd className="font-medium text-ink">{selected.job}</dd>
                </dl>
                <div className="mt-4 flex flex-wrap gap-1.5">
                  {selected.tags.map((t) =>
                <Tag key={t} tone="solid">
                      {t}
                    </Tag>
                )}
                </div>
              </div>
            </div>

            <div className="mt-8 space-y-5 rounded-2xl bg-canvas p-6">
              <IntakeRow label="신청 경위" value={selected.referralReason} />
              <IntakeRow label="호소 문제 영역" value={selected.problemArea} />
              <IntakeRow label="상담 신청 이유" value={selected.counselingReason} />
            </div>

            <div className="mt-7 flex flex-col items-center justify-between gap-4 sm:flex-row">
              <p className="text-xs leading-relaxed text-ink-muted">
                30분 타이머로 진행되며, 종료 시 축어록과 평가 리포트가 생성됩니다.
              </p>
              <button
              type="button"
              onClick={() => navigate(`/session/${selected.clientId}`)}
              className="inline-flex h-12 shrink-0 items-center gap-2 rounded-full bg-brand-600 px-8 text-sm font-bold text-white shadow-glow transition-colors duration-150 ease-out hover:bg-brand-700">
              
                상담 시작
                <ArrowRightIcon className="h-4 w-4" />
              </button>
            </div>
          </div>
        }
      </Modal>
    </div>);

}

function IntakeRow({ label, value }: {label: string;value: string;}) {
  return (
    <div>
      <h3 className="text-sm font-bold text-brand-700">{label}</h3>
      <p className="mt-1.5 text-sm leading-relaxed text-ink-soft">{value}</p>
    </div>);

}