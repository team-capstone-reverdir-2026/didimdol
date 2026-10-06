# ESConv에서 상담 전략별 피드백 점수와
# Affirmation 사용 비율에 따른 감정 강도 개선폭 비교


import json
import statistics
from pathlib import Path
from collections import defaultdict

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "esconv" / "ESConv.json"


def analyze_feedback_by_strategy(dialogues):
    scores_by_strategy = defaultdict(list)

    for dlg in dialogues:
        turns = dlg["dialog"]
        for i, turn in enumerate(turns):
            if turn.get("speaker") != "supporter":
                continue
            strategy = turn.get("annotation", {}).get("strategy")
            if not strategy:
                continue

            for j in range(i + 1, min(i + 4, len(turns))):
                nxt = turns[j]
                if nxt.get("speaker") == "seeker":
                    feedback = nxt.get("annotation", {}).get("feedback")
                    if feedback is not None:
                        scores_by_strategy[strategy].append(int(feedback))
                        break

    print("=== 1. 전략 직후 시커 피드백 점수 평균 (1~5점) ===")
    averaged = [
        (strategy, sum(scores) / len(scores), len(scores))
        for strategy, scores in scores_by_strategy.items()
    ]
    averaged.sort(key=lambda x: -x[1])
    for strategy, avg, n in averaged:
        print(f"{strategy}: 평균 {avg:.2f}점 (표본 {n}개)")


def analyze_emotion_drop_by_affirmation_ratio(dialogues):
    high_affirm_drops = []
    low_affirm_drops = []

    for dlg in dialogues:
        strategies = [
            t.get("annotation", {}).get("strategy")
            for t in dlg["dialog"]
            if t.get("speaker") == "supporter"
        ]
        strategies = [s for s in strategies if s]
        if not strategies:
            continue

        affirm_ratio = strategies.count("Affirmation and Reassurance") / len(strategies)

        try:
            seeker_score = dlg["survey_score"]["seeker"]
            initial = int(seeker_score["initial_emotion_intensity"])
            final = int(seeker_score["final_emotion_intensity"])
        except (KeyError, TypeError, ValueError):
            continue

        drop = initial - final
        if affirm_ratio >= 0.2:
            high_affirm_drops.append(drop)
        else:
            low_affirm_drops.append(drop)

    print("\n=== 2. Affirmation 사용 비율에 따른 감정강도 개선폭 비교 ===")
    if high_affirm_drops:
        print(f"Affirmation 20% 이상 사용한 대화: 평균 개선폭 {statistics.mean(high_affirm_drops):.2f} "
              f"(표본 {len(high_affirm_drops)}개)")
    if low_affirm_drops:
        print(f"Affirmation 20% 미만 사용한 대화: 평균 개선폭 {statistics.mean(low_affirm_drops):.2f} "
              f"(표본 {len(low_affirm_drops)}개)")


def main():
    with open(DATA_PATH, "r", encoding="utf-8") as f:
        dialogues = json.load(f)

    analyze_feedback_by_strategy(dialogues)
    analyze_emotion_drop_by_affirmation_ratio(dialogues)


if __name__ == "__main__":
    main()