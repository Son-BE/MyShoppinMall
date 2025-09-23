# nlp-server/Dockerfile
FROM python:3.10-slim

WORKDIR /app

# 시스템 패키지 설치
RUN apt-get update && apt-get install -y curl gcc g++ \
    && rm -rf /var/lib/apt/lists/* && apt-get clean

# 모델 캐시 디렉토리 생성
RUN mkdir -p /app/model_cache

# 1) requirements.txt 먼저 복사 → 캐시 활용
COPY requirements.txt .

# 2) 패키지 설치
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 3) 앱 전체 복사
COPY . .

# 환경 변수
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV TRANSFORMERS_CACHE=/app/model_cache
ENV HF_HOME=/app/model_cache

# 모델 다운로드 (캐시 활용)
#RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('intfloat/multilingual-e5-large-instruct')"

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:5001/health || exit 1

EXPOSE 5001

# 앱 실행
CMD ["uvicorn", "nlp_service:app", "--host", "0.0.0.0", "--port", "5001"]
