import React from 'react';

interface TagProps {
  children: React.ReactNode;
  tone?: 'default' | 'solid' | 'onDark';
}

export function Tag({ children, tone = 'default' }: TagProps) {
  const styles = {
    default: 'bg-brand-100 text-brand-700',
    solid: 'bg-brand-600 text-white',
    onDark: 'bg-white/20 text-white'
  }[tone];

  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${styles}`}>
      {children}
    </span>);

}