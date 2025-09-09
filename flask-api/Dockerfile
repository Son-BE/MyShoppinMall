#FastApi용 Dockerfile
FROM python:3.10-slim

WORKDIR /app

# ✅ 추가: 시스템 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    gcc \
    g++ \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# ✅ 추가: 데이터 디렉토리 생성
RUN mkdir -p /app/data

COPY requirements.txt .

# ✅ 수정: pip 업그레이드 및 설치
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# ✅ 추가: 환경변수 기본값 설정
ENV PYTHONUNBUFFERED=1
ENV DATA_DIR=/app/data

EXPOSE 5000

# ✅ 추가: 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:5000/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000"]
