from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://user:password@localhost/tumtum"
    redis_url: str = "redis://localhost:6379"
    secret_key: str = "your-secret-key"
    google_client_id: str = ""
    google_client_secret: str = ""
    setlist_fm_api_key: str = ""
    api_football_key: str = ""
    anthropic_api_key: str = ""
    sentry_dsn: str = ""
    environment: str = "development"

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
