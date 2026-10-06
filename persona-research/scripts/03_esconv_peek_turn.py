# ESConv 데이터셋의 설문 점수, 첫 번째 대화의 초기 턴 구조 확인

import json
from pathlib import Path

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "esconv" / "ESConv.json"


def main():
    with open(DATA_PATH, "r", encoding="utf-8") as f:
        dialogues = json.load(f)

    first = dialogues[0]

    print("=== survey_score 출력 ===")
    print(first.get("survey_score"))

    print("\n=== 대화 1개의 턴을 처음 6개까지 출력 ===")
    for i, turn in enumerate(first["dialog"][:6]):
        print(f"\n--- turn {i} ---")
        print(turn)


if __name__ == "__main__":
    main()