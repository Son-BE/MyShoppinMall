# image-classifier/gunicorn.conf.py
import multiprocessing
import os

# 서버 설정
bind = "0.0.0.0:5000"
workers = 1
worker_class = "sync"
worker_connections = 10

# 타임아웃 설정
timeout = 60
keepalive = 2
graceful_timeout = 30

# 메모리 관리
max_requests = 100
max_requests_jitter = 10
preload_app = True

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