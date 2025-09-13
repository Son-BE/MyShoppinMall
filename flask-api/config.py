# config.py
import os
from typing import Optional

class Config:
    def __init__(self):
        # Docker 환경 감지
        self.is_docker = os.path.exists('/.dockerenv') or os.getenv('DOCKER_ENV') == 'true'

        # 기본 호스트 설정 (환경별)
        default_mysql_host = "shop-mysql" if self.is_docker else "localhost"
        default_redis_host = "shop-redis" if self.is_docker else "localhost"

        # MySQL 설정
        self.MYSQL_HOST: str = os.getenv("MYSQL_HOST", default_mysql_host)
        self.MYSQL_PORT: int = int(os.getenv("MYSQL_PORT", "3306"))
        self.MYSQL_DATABASE: str = os.getenv("MYSQL_DATABASE", "SonStar")
        self.MYSQL_USER: str = os.getenv("MYSQL_USER", "root")
        self.MYSQL_PASSWORD: str = os.getenv("MYSQL_PASSWORD", "hi092787!!!")

        # Redis 설정
        self.REDIS_HOST: str = os.getenv("REDIS_HOST", default_redis_host)
        self.REDIS_PORT: int = int(os.getenv("REDIS_PORT", "6379"))
        self.REDIS_DB: int = int(os.getenv("REDIS_DB", "0"))

        # 데이터 파일 경로
        self.DATA_DIR: str = os.getenv("DATA_DIR", "/app/data" if self.is_docker else "./data")

        # 데이터 디렉토리 생성
        os.makedirs(self.DATA_DIR, exist_ok=True)

        # 로깅 설정
        self.LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")

    @property
    def mysql_url(self) -> str:
        return f"mysql+pymysql://{self.MYSQL_USER}:{self.MYSQL_PASSWORD}@{self.MYSQL_HOST}:{self.MYSQL_PORT}/{self.MYSQL_DATABASE}"

    @property
    def redis_url(self) -> str:
        return f"redis://{self.REDIS_HOST}:{self.REDIS_PORT}/{self.REDIS_DB}"

    def get_file_path(self, filename: str) -> str:
        """데이터 디렉토리 내 파일 경로 반환"""
        return os.path.join(self.DATA_DIR, filename)

    def print_config(self):
        """현재 설정 출력 (디버깅용)"""
        print(f"Environment: {'Docker' if self.is_docker else 'Local'}")
        print(f"MySQL: {self.MYSQL_HOST}:{self.MYSQL_PORT}")
        print(f"Redis: {self.REDIS_HOST}:{self.REDIS_PORT}")
        print(f"Data Dir: {self.DATA_DIR}")

# 전역 설정 객체
config = Config()