import React, { useEffect, useRef } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { XIcon } from 'lucide-react';

interface MemoPanelProps {
  open: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  side?: 'left' | 'right';
}

export function MemoPanel({ open, onClose, title, children, side = 'left' }: MemoPanelProps) {
  const panelRef = useRef<HTMLElement>(null);

  useEffect(() => {
    if (!open) return;
    const onPointerDown = (e: PointerEvent) => {
      const target = e.target;
      if (!(target instanceof Node) || panelRef.current?.contains(target)) return;
      if (target instanceof Element && target.closest('[data-panel-toggle]')) return;
      onClose();
    };
    document.addEventListener('pointerdown', onPointerDown);
    return () => document.removeEventListener('pointerdown', onPointerDown);
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open &&
      <motion.aside
        ref={panelRef}
        aria-label={title}
        className={`absolute bottom-0 top-16 z-30 w-[22rem] max-w-[85%] overflow-y-auto scroll-slim border-line bg-white p-6 shadow-pop ${
        side === 'left' ? 'left-0 border-r' : 'right-0 border-l'}`
        }
        initial={{ opacity: 0, x: side === 'left' ? -16 : 16 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: side === 'left' ? -12 : 12 }}
        transition={{ duration: 0.22, ease: [0.23, 1, 0.32, 1] }}>
        
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-base font-bold text-ink">{title}</h2>
            <button
            type="button"
            onClick={onClose}
            aria-label="패널 닫기"
            className="grid h-8 w-8 place-items-center rounded-full text-ink-muted transition-colors duration-150 ease-out hover:bg-brand-50 hover:text-brand-700">
            
              <XIcon className="h-4.5 w-4.5" />
            </button>
          </div>
          {children}
        </motion.aside>
      }
    </AnimatePresence>);

}