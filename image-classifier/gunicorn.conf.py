import multiprocessing
import os

# 서버 설정
bind = "0.0.0.0:5000"
workers = 1  # CPU 집약적 작업이므로 1개로 제한
worker_class = "sync"
worker_connections = 10  # 동시 연결 수 제한

# 타임아웃 설정 (중요!)
timeout = 60  # 60초로 증가
keepalive = 2
graceful_timeout = 30

# 메모리 관리
max_requests = 100  # 100 요청마다 워커 재시작
max_requests_jitter = 10  # 랜덤 지터 추가
preload_app = True  # 메모리 사용량 최적화

# 로깅
loglevel = "info"
accesslog = "-"
errorlog = "-"
access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s" %(D)s'

# 프로세스 관리
pidfile = "/tmp/gunicorn.pid"
tmp_upload_dir = "/tmp"

# 리소스 제한
limit_request_line = 8192
limit_request_fields = 100
limit_request_field_size = 8192