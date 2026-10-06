# AnnoMI 데이터셋을 불러와 전체 규모와 발화 유형별 분포를 확인
# sustain 발화의 실제 샘플 출력

import pandas as pd
from pathlib import Path

DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "raw" / "annomi" / "AnnoMI-full.csv"


def load_annomi() -> pd.DataFrame:
    return pd.read_csv(DATA_PATH)


def main():
    df = load_annomi()

    print("=== 1. 전체 크기 ===")
    print(f"총 발화(행) 개수: {len(df)}")
    print(f"총 대화(transcript) 개수: {df['transcript_id'].nunique()}")

    print("\n=== 2. 컬럼 목록 ===")
    print(list(df.columns))

    print("\n=== 3. 내담자 발화 유형(client_talk_type) 분포 ===")
    client_df = df[df["interlocutor"] == "client"]
    print(client_df["client_talk_type"].value_counts())

    print("\n=== 4. 상담자 발화 유형(main_therapist_behaviour) 분포 ===")
    therapist_df = df[df["interlocutor"] == "therapist"]
    print(therapist_df["main_therapist_behaviour"].value_counts())

    print("\n=== 5. sustain(저항) 발화 실제 샘플 5개 ===")
    sustain_samples = client_df[client_df["client_talk_type"] == "sustain"].head(5)
    for _, row in sustain_samples.iterrows():
        print(f"- [대화 {row['transcript_id']}] {row['utterance_text']}")


if __name__ == "__main__":
    main()