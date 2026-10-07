# 디딤돌 백엔드 배포 가이드 (Render 기준)

## 1. 서비스 만들기
1. Render → New → **PostgreSQL** (무료 플랜이면 만료 기간 확인). Internal Database URL 의 host/db/user/password 를 메모.
2. Render → New → **Web Service** → 이 레포 선택
   - Root Directory: `backend`
   - Runtime: **Docker** (Dockerfile 자동 인식)
   - Health Check Path: `/api/clients` 가 아니라 인증 없이 열린 경로가 필요하면 비워둬도 됨 (포트 오픈만 확인)

## 2. 환경변수 (Web Service → Environment)
| 키 | 값 |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>:5432/<db>` (**jdbc: 접두사 필수**, Render 가 주는 `postgres://` 그대로 쓰면 안 됨) |
| `DB_USERNAME` / `DB_PASSWORD` | DB 계정 |
| `DDL_AUTO` | 최초 배포는 `update` (빈 DB 에 테이블 생성). 이후 `validate` 로 되돌리기 |
| `JWT_SECRET` | 32바이트 이상 랜덤 문자열 |
| `CORS_ALLOWED_ORIGINS` | 프론트 주소 (예: `https://didimdol.vercel.app`), 쉼표로 여러 개 |
| `SEED_ENABLED` | `true` (내담자 2명 + 페르소나 타입 시드, 재시작해도 중복 생성 안 됨) |
| `AI_PROVIDER` | `claude` (키 없이 화면만 보려면 `fake`) |
| `ANTHROPIC_API_KEY` | 키 |
| `ANTHROPIC_BASE_URL` | 게이트웨이 사용 시 `http://43.201.226.184:4000`, 직접 연결이면 생략 |
| `ANTHROPIC_MODEL` | 예: `claude-sonnet-4-6` |

`PORT` 는 Render 가 주입하며 앱이 자동으로 사용한다.

## 3. 배포 후 확인 (curl)
```
BASE=https://<서비스>.onrender.com
curl -s -X POST $BASE/api/auth/sign-up ...
curl -s $BASE/api/clients -H "Authorization: Bearer <token>"
```
SSE 는 `curl -N $BASE/api/sessions/<id>/stream?ticket=...` 로 확인.

## 4. 알아둘 점
- 무료 플랜은 일정 시간 요청이 없으면 슬립 → 첫 요청이 느림. 시연 전에 미리 깨워둘 것.
- 게이트웨이(http)는 서버→서버 호출이라 mixed-content 문제는 없지만, 키 예산($20)을 시연용으로만 쓸 것.
- 시간대는 Asia/Seoul 로 고정(Dockerfile). 프론트가 보내는 `startAt` 도 KST 기준 LocalDateTime 이어야 한다.
