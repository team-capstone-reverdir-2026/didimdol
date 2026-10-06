import { Logo } from './Logo';

interface AuthShellProps {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}

export function AuthShell({ title, subtitle, children, footer }: AuthShellProps) {
  return (
    <div className="flex min-h-full w-full items-center justify-center bg-canvas px-5 py-14">
      <div className="w-full max-w-[26rem]">
        <div className="mb-8 flex flex-col items-center text-center">
          <Logo size="lg" />
          <p className="mt-4 text-sm text-ink-muted">AI 내담자와 함께하는 상담 수련</p>
        </div>

        <section className="rounded-3xl border border-line bg-white p-8 shadow-card">
          <h1 className="text-xl font-bold text-ink">{title}</h1>
          {subtitle && <p className="mt-2 text-sm leading-relaxed text-ink-muted">{subtitle}</p>}
          <div className="mt-7">{children}</div>
        </section>

        {footer && <div className="mt-6 text-center text-sm text-ink-muted">{footer}</div>}
      </div>
    </div>);

}