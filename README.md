# SNS-gpt

React 프런트엔드 + Spring Boot 백엔드 + PostgreSQL로 구성된 뉴스 요약 서비스입니다.

## 구성

- `frontend/`: React (Vite)
- `backend/`: Spring Boot 3.3 + Java 17 + PostgreSQL (Gradle)

## Docker Compose 실행

```bash
docker compose up --build
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

## 백엔드 실행

```bash
cd backend
gradle bootRun
```

환경 설정은 `backend/src/main/resources/application.yml`을 참고하세요. 기본 DB는 `sns_gpt`이며 계정은 `sns_gpt`/`sns_gpt`입니다.

API 키는 `backend/secrets.json`에 저장합니다. 예시는 `backend/secrets.json.example`을 참고하세요.

## 프런트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

백엔드는 `http://localhost:8080` 기준으로 호출합니다.

## 주요 기능

- Bloomberg/Investing.com 뉴스를 크롤링하여 `.txt` 파일로 저장
- 파일 경로를 DB에 저장하고 API로 전달
- GPT10 탭에서 최신 10건의 헤드라인 요약 파일 생성
- Chat with GPT 탭을 통해 뉴스 전문가 챗봇과 대화
- ChatGPT Google 계정으로 로그인 및 마이페이지 관리
