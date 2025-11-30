from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # MySQL
    mysql_host: str = "127.0.0.1"
    mysql_port: int = 3307
    mysql_user: str
    mysql_password: str = ""
    mysql_root_password: str = ""
    mysql_database: str

    # PostgreSQL
    postgres_host: str = "127.0.0.1"
    postgres_port: int = 5432
    postgres_user: str
    postgres_password: str
    postgres_db: str

    # OpenAI
    openai_api_key: str = ""

    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()