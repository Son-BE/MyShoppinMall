#    # flask-api/Dockerfile
#    FROM python:3.10-slim
#
#    WORKDIR /app
#
#    # 시스템 패키지 설치
#    RUN apt-get update && apt-get install -y \
#        curl gcc g++ pkg-config \
#        && rm -rf /var/lib/apt/lists/*
#
#    # 데이터 디렉토리 생성
#    RUN mkdir -p /app/data
#
#    # requirements.txt 복사 및 설치
#    COPY requirements.txt .
#    RUN pip install --no-cache-dir --upgrade pip && \
#        pip install --no-cache-dir -r requirements.txt
#
#    # 소스 코드 복사
#    COPY . .
#
#    # 환경변수 설정 (NaN 처리 개선)
#    ENV PYTHONUNBUFFERED=1
#    ENV DATA_DIR=/app/data
#    ENV PYTHONPATH=/app
#    ENV DOCKER_ENV=true
#    ENV PANDAS_FUTURE_DTYPES=false
#    ENV TF_CPP_MIN_LOG_LEVEL=2
#
#    # 포트 노출
#    EXPOSE 5000
#
#    # 헬스체크
#    HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
#      CMD curl -f http://localhost:5000/health || exit 1
#
#    # 앱 실행
#    CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000"]