# 더 안정적인 버전 사용
FROM python:3.9.18-slim-bullseye

# 작업 디렉토리 설정
WORKDIR /app

# 사용자 생성 (보안)
RUN useradd --create-home --shell /bin/bash appuser

# 필수 시스템 패키지만 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Python 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 앱 파일 복사
COPY . .
COPY gunicorn.conf.py .

# 권한 설정
RUN chown -R appuser:appuser /app
USER appuser

# 환경 변수 설정
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV OMP_NUM_THREADS=1
ENV MKL_NUM_THREADS=1
ENV TORCH_NUM_THREADS=1

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# 포트 노출
EXPOSE 5000

# Gunicorn으로 실행
CMD ["gunicorn", "--config", "gunicorn.conf.py", "app:app"]