# AnnoMI에서 상담자 행동을 세부 유형으로 구분
# 각 세부 행동 직후 나타난 내담자 반응 비율 분석


import pandas as pd
from pathlib import Path

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "annomi" / "AnnoMI-full.csv"


def behavior_label(row) -> str:
    behavior = row["main_therapist_behaviour"]

    def clean(sub):
        # 값이 없거나(NaN) 'n/a' 문자열이면 세부유형 없음으로 처리
        if pd.isna(sub) or str(sub).strip().lower() == "n/a":
            return None
        return str(sub).strip()

    if behavior == "question":
        sub = clean(row.get("question_subtype"))
        return f"question_{sub}" if sub else "question_unknown"
    if behavior == "reflection":
        sub = clean(row.get("reflection_subtype"))
        return f"reflection_{sub}" if sub else "reflection_unknown"
    if behavior == "therapist_input":
        sub = clean(row.get("therapist_input_subtype"))
        return f"input_{sub}" if sub else "input_unknown"
    return behavior  # "other"는 그대로


def main():
    df = pd.read_csv(DATA_PATH)
    df = df.sort_values(["transcript_id", "utterance_id"]).reset_index(drop=True)
    df["behavior_detail"] = df.apply(behavior_label, axis=1)

    pairs = []
    for i in range(1, len(df)):
        prev_row = df.iloc[i - 1]
        curr_row = df.iloc[i]
        if prev_row["transcript_id"] != curr_row["transcript_id"]:
            continue
        if prev_row["interlocutor"] != "therapist" or curr_row["interlocutor"] != "client":
            continue
        pairs.append({
            "behavior_detail": prev_row["behavior_detail"],
            "client_reaction": curr_row["client_talk_type"],
        })

    pairs_df = pd.DataFrame(pairs)

    cross_table = pd.crosstab(
        pairs_df["behavior_detail"],
        pairs_df["client_reaction"],
        normalize="index"
    ) * 100

    print("=== 세부 행동별 내담자 반응 비율 (%) ===")
    print(cross_table.round(1))

    print("\n=== (참고) 각 세부 행동이 몇 번씩 등장했는지 ===")
    print(pairs_df["behavior_detail"].value_counts())


if __name__ == "__main__":
    main()