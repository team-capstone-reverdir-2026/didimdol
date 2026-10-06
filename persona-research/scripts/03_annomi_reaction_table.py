# AnnoMI 데이터셋에서 상담자 발화와 직후 내담자 반응을 짝지어
# 행동 유형별 반응 비율 분석

import pandas as pd
from pathlib import Path

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "annomi" / "AnnoMI-full.csv"


def main():
    df = pd.read_csv(DATA_PATH)

    df = df.sort_values(["transcript_id", "utterance_id"]).reset_index(drop=True)

    pairs = []
    for i in range(1, len(df)):
        prev_row = df.iloc[i - 1]
        curr_row = df.iloc[i]

        if prev_row["transcript_id"] != curr_row["transcript_id"]:
            continue
        if prev_row["interlocutor"] != "therapist" or curr_row["interlocutor"] != "client":
            continue

        pairs.append({
            "therapist_behavior": prev_row["main_therapist_behaviour"],
            "client_reaction": curr_row["client_talk_type"],
        })

    pairs_df = pd.DataFrame(pairs)

    print(f"=== 전체 짝지어진 쌍 개수: {len(pairs_df)} ===\n")

    cross_table = pd.crosstab(
        pairs_df["therapist_behavior"],
        pairs_df["client_reaction"],
        normalize="index"
    ) * 100

    print("=== 상담자 행동 유형별 내담자 반응 비율 (%) ===")
    print(cross_table.round(1))

    print("\n=== (참고) 각 상담자 행동이 전체에서 몇 번씩 등장했는지 ===")
    print(pairs_df["therapist_behavior"].value_counts())


if __name__ == "__main__":
    main()