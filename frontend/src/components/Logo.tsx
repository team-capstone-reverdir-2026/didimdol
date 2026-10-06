import React from 'react';

export const MASCOT = "../assets/mascot.png";
export const LOGO_SRC = "../assets/logo.png";

interface LogoProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const heights = { sm: 'h-7', md: 'h-9', lg: 'h-16' };

export function Logo({ size = 'md', className = '' }: LogoProps) {
  return (
    <img
      src={LOGO_SRC}
      alt="디딤돌"
      className={`${heights[size]} w-auto select-none ${className}`}
      draggable={false} />);


}