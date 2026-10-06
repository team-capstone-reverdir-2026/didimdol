import { Modal } from './Modal';

interface ConfirmDialogProps {
  open: boolean;
  title: React.ReactNode;
  description?: React.ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = '확인',
  cancelLabel = '취소',
  destructive = false,
  onConfirm,
  onCancel
}: ConfirmDialogProps) {
  return (
    <Modal open={open} onClose={onCancel} size="sm" showClose={false} labelledBy="confirm-title">
      <div className="text-center">
        <h2 id="confirm-title" className="text-lg font-bold leading-relaxed text-ink">
          {title}
        </h2>
        {description && <p className="mt-3 text-sm leading-relaxed text-ink-soft">{description}</p>}
        <div className="mt-7 flex gap-2.5">
          <button
            type="button"
            onClick={onCancel}
            className="h-11 flex-1 rounded-full border border-line bg-white text-sm font-semibold text-ink-soft transition-colors duration-150 ease-out hover:bg-brand-50">
            
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className={`h-11 flex-1 rounded-full text-sm font-semibold text-white transition-colors duration-150 ease-out ${
            destructive ? 'bg-danger hover:bg-[#B23A59]' : 'bg-brand-600 hover:bg-brand-700'}`
            }>
            
            {confirmLabel}
          </button>
        </div>
      </div>
    </Modal>);

}