# AnnoMI에서 내담자 발화의 길이와 회피 표현 사용 비율을 비교,
# 고품질 상담 대화의 전반부와 후반부 저항 비율 분석


import pandas as pd
from pathlib import Path

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "annomi" / "AnnoMI-full.csv"

HEDGE_PHRASES = [
    "i don't know", "i dont know", "i guess", "not really",
    "i mean", "whatever", "i suppose", "kind of", "sort of", "not a problem"
]


def contains_hedge(text: str) -> bool:
    t = str(text).lower()
    return any(phrase in t for phrase in HEDGE_PHRASES)


def main():
    df = pd.read_csv(DATA_PATH)
    df = df.sort_values(["transcript_id", "utterance_id"]).reset_index(drop=True)

    client_df = df[df["interlocutor"] == "client"].copy()
    client_df["word_count"] = client_df["utterance_text"].str.split().str.len()

    print("=== 1. 내담자 발화 유형별 길이(단어 수) ===")
    print(client_df.groupby("client_talk_type")["word_count"].agg(["mean", "median", "count"]).round(1))

    print("\n=== 2. 회피 표현 포함 비율 비교 ===")
    sustain_df = client_df[client_df["client_talk_type"] == "sustain"]
    change_df = client_df[client_df["client_talk_type"] == "change"]

    sustain_hedge_rate = sustain_df["utterance_text"].apply(contains_hedge).mean() * 100
    change_hedge_rate = change_df["utterance_text"].apply(contains_hedge).mean() * 100

    print(f"sustain 발화: {sustain_hedge_rate:.1f}% (표본 {len(sustain_df)}개)")
    print(f"change 발화 (비교용): {change_hedge_rate:.1f}% (표본 {len(change_df)}개)")

    print("\n=== 3. 고품질(mi_quality=high) 대화, 회기 전반부 vs 후반부 저항 비율 ===")
    high_df = df[df["mi_quality"] == "high"]

    front_flags = []
    back_flags = []
    for transcript_id, group in high_df.groupby("transcript_id"):
        group = group.sort_values("utterance_id")
        client_rows = group[group["interlocutor"] == "client"]
        n = len(client_rows)
        if n < 4:
            continue
        half = n // 2
        front = client_rows.iloc[:half]
        back = client_rows.iloc[half:]
        front_flags.extend((front["client_talk_type"] == "sustain").tolist())
        back_flags.extend((back["client_talk_type"] == "sustain").tolist())

    front_rate = sum(front_flags) / len(front_flags) * 100
    back_rate = sum(back_flags) / len(back_flags) * 100
    print(f"전반부 저항 비율: {front_rate:.1f}% (발화 {len(front_flags)}개)")
    print(f"후반부 저항 비율: {back_rate:.1f}% (발화 {len(back_flags)}개)")


if __name__ == "__main__":
    main()