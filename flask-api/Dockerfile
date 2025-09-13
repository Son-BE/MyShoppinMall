# FastAPI용 Dockerfile (Java 없는 버전)
FROM python:3.10-slim

WORKDIR /app

# 기본 시스템 패키지만 설치
RUN apt-get update && apt-get install -y \
    curl \
    gcc \
    g++ \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# 데이터 디렉토리 생성
RUN mkdir -p /app/data

# requirements 복사 및 설치
COPY requirements.txt .

# pip 업그레이드 및 패키지 설치
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 환경변수 기본값 설정
ENV PYTHONUNBUFFERED=1
ENV DATA_DIR=/app/data
ENV PYTHONPATH=/app
ENV DOCKER_ENV=true

EXPOSE 5000

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD curl -f http://localhost:5000/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000"]