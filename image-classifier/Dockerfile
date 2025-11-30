## image-classifier/Dockerfile
#FROM python:3.9.18-slim-bullseye
#
#WORKDIR /app
#
## 사용자 생성
#RUN useradd --create-home --shell /bin/bash appuser
#
## 최소 시스템 패키지 설치
#RUN apt-get update && apt-get install -y curl \
#    && rm -rf /var/lib/apt/lists/* && apt-get clean
#
## 1) requirements.txt 먼저 복사 → 캐시 활용
#COPY requirements.txt .
#
## 2) 패키지 설치
#RUN pip install --no-cache-dir --upgrade pip && \
#    pip install --no-cache-dir -r requirements.txt
#
## 3) 앱 전체 복사
#COPY . .
#COPY gunicorn.conf.py .
#
## 권한 설정
#RUN chown -R appuser:appuser /app
#USER appuser
#
## 환경 변수
#ENV PYTHONUNBUFFERED=1
#ENV PYTHONDONTWRITEBYTECODE=1
#ENV OMP_NUM_THREADS=1
#ENV MKL_NUM_THREADS=1
#ENV TORCH_NUM_THREADS=1
#
## 헬스체크
#HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#    CMD curl -f http://localhost:5000/health || exit 1
#
#EXPOSE 5000
#
## Gunicorn 실행
#CMD ["gunicorn", "--config", "gunicorn.conf.py", "app:app"]
