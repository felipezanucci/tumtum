from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://user:password@localhost/tumtum"
    redis_url: str = "redis://localhost:6379"
    secret_key: str = "your-secret-key"
    # Add other settings as needed

settings = Settings()