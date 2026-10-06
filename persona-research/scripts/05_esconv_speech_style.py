# ESConv에서 내담자와 상담자의 발화 길이 비교
# 전체 및 상담 전략별 내담자의 자기지시 표현 비율 분석


import json
import re
import statistics
from pathlib import Path
from collections import defaultdict

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "esconv" / "ESConv.json"

SELF_REF_PATTERN = re.compile(r"\b(i|i'm|i've|i'll|my|me|myself)\b", re.IGNORECASE)


def word_count(text: str) -> int:
    return len(text.split())


def self_ref_rate(text: str) -> float:
    wc = word_count(text)
    if wc == 0:
        return 0.0
    return len(SELF_REF_PATTERN.findall(text)) / wc


def main():
    with open(DATA_PATH, "r", encoding="utf-8") as f:
        dialogues = json.load(f)

    seeker_lengths = []
    supporter_lengths = []
    seeker_self_ref_rates = []

    for dlg in dialogues:
        for turn in dlg["dialog"]:
            content = turn.get("content", "")
            wc = word_count(content)
            if wc == 0:
                continue
            if turn.get("speaker") == "seeker":
                seeker_lengths.append(wc)
                seeker_self_ref_rates.append(self_ref_rate(content))
            elif turn.get("speaker") == "supporter":
                supporter_lengths.append(wc)

    print("=== 1. 발화 길이 비교 (단어 수) ===")
    print(f"시커(내담자) 평균: {statistics.mean(seeker_lengths):.1f}단어 (표본 {len(seeker_lengths)}개)")
    print(f"지지자(상담자) 평균: {statistics.mean(supporter_lengths):.1f}단어 (표본 {len(supporter_lengths)}개)")

    print("\n=== 2. 시커 발화의 자기지시 표현 비율 (전체 평균) ===")
    print(f"{statistics.mean(seeker_self_ref_rates) * 100:.1f}%")

    self_ref_by_strategy = defaultdict(list)
    for dlg in dialogues:
        turns = dlg["dialog"]
        for i, turn in enumerate(turns):
            if turn.get("speaker") != "supporter":
                continue
            strategy = turn.get("annotation", {}).get("strategy")
            if not strategy:
                continue
            if i + 1 < len(turns) and turns[i + 1].get("speaker") == "seeker":
                content = turns[i + 1].get("content", "")
                if word_count(content) > 0:
                    self_ref_by_strategy[strategy].append(self_ref_rate(content))

    print("\n=== 3. (참고) 전략 직후 시커 발화의 자기지시 표현 비율 ===")
    rows = [(s, statistics.mean(v), len(v)) for s, v in self_ref_by_strategy.items()]
    rows.sort(key=lambda x: -x[1])
    for strategy, avg, n in rows:
        print(f"{strategy}: {avg * 100:.1f}% (표본 {n}개)")


if __name__ == "__main__":
    main()