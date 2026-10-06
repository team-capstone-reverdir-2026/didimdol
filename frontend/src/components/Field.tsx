interface FieldProps {
  label: string;
  id: string;
  type?: string;
  placeholder?: string;
  hint?: string;
  value?: string;
  onChange?: (value: string) => void;
}

export function Field({
  label,
  id,
  type = 'text',
  placeholder,
  hint,
  value,
  onChange
}: FieldProps) {
  return (
    <div>
      <label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-ink-soft">
        {label}
      </label>
      <input
        id={id}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        className="h-12 w-full rounded-xl border border-line bg-canvas px-4 text-sm text-ink placeholder:text-ink-faint transition-colors duration-150 ease-out focus:border-brand-400 focus:bg-white" />
      
      {hint && <p className="mt-1.5 text-xs text-ink-muted">{hint}</p>}
    </div>);

}