# ESConv 데이터셋의 전체 구조와 상담 전략별 분포 확인
# 특정 전략 직후의 대화 반응 샘플 출력


import json
from pathlib import Path
from collections import Counter

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "esconv" / "ESConv.json"


def load_esconv() -> list:
    with open(DATA_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def main():
    dialogues = load_esconv()

    print("=== 1. 전체 대화 개수 ===")
    print(len(dialogues))

    print("\n=== 2. 대화 key 목록 (구조 확인) ===")
    print(list(dialogues[0].keys()))

    print("\n=== 3. 대화 내부 ===")
    first = dialogues[0]
    print("문제 유형:", first.get("problem_type"))
    print("감정 유형:", first.get("emotion_type"))
    print("상황 설명:", first.get("situation"))
    print("대화 턴 개수:", len(first.get("dialog", [])))

    print("\n=== 4. 전체 대화에서 전략별 등장 횟수 ===")
    strategy_counter = Counter()
    for dlg in dialogues:
        for turn in dlg.get("dialog", []):
            annotation = turn.get("annotation", {}) or {}
            strategy = annotation.get("strategy")
            if strategy:
                strategy_counter[strategy] += 1
    for strategy, count in strategy_counter.most_common():
        print(f"{strategy}: {count}")

    print("\n=== 5. Affirmation and Reassurance 직후 반응 샘플 ===")
    sample_count = 0
    for dlg in dialogues:
        turns = dlg.get("dialog", [])
        for i, turn in enumerate(turns):
            annotation = turn.get("annotation", {}) or {}
            if annotation.get("strategy") == "Affirmation and Reassurance" and i + 1 < len(turns):
                print(f"- [지지자] {turn.get('content')}")
                print(f"  [다음 반응] {turns[i + 1].get('content')}")
                sample_count += 1
            if sample_count >= 3:
                break
        if sample_count >= 3:
            break


if __name__ == "__main__":
    main()