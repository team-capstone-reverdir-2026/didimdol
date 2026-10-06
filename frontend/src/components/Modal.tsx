import React, { useEffect } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { XIcon } from 'lucide-react';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  labelledBy?: string;
  size?: 'sm' | 'md' | 'lg';
  showClose?: boolean;
}

const widths = { sm: 'max-w-sm', md: 'max-w-xl', lg: 'max-w-3xl' };

export function Modal({
  open,
  onClose,
  children,
  labelledBy,
  size = 'md',
  showClose = true
}: ModalProps) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open &&
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
          <motion.div
          className="absolute inset-0 bg-brand-900/35"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2, ease: [0.23, 1, 0.32, 1] }}
          onClick={onClose} />
        
          <motion.div
          role="dialog"
          aria-modal="true"
          aria-labelledby={labelledBy}
          className={`relative w-full ${widths[size]} max-h-[88vh] overflow-y-auto scroll-slim rounded-3xl bg-white p-7 shadow-pop`}
          initial={{ opacity: 0, scale: 0.96, y: 8 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.97, y: 4 }}
          transition={{ duration: 0.22, ease: [0.23, 1, 0.32, 1] }}>
          
            {showClose &&
          <button
            type="button"
            onClick={onClose}
            aria-label="닫기"
            className="absolute right-5 top-5 grid h-9 w-9 place-items-center rounded-full text-ink-muted transition-colors duration-150 ease-out hover:bg-brand-50 hover:text-brand-700">
            
                <XIcon className="h-5 w-5" />
              </button>
          }
            {children}
          </motion.div>
        </div>
      }
    </AnimatePresence>);

}