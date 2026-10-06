const TAG_LABELS: Record<string, string> = {
  SILENCE_RESISTANT: '침묵저항형',
  APPROVAL_SEEKING: '인정요구형',
};

export function formatTag(tag: string): string {
  return TAG_LABELS[tag] ?? tag;
}
