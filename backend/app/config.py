from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://user:password@localhost/tumtum"
    redis_url: str = "redis://localhost:6379"
    secret_key: str = "your-secret-key"
    google_client_id: str = ""
    google_client_secret: str = ""
    setlist_fm_api_key: str = ""
    api_football_key: str = ""
    sentry_dsn: str = ""
    # Timestamps are stored in UTC, but a share card has to say the time the
    # person actually lived — "19h31" on a card for a moment felt at 16h31 is
    # simply wrong, and it is the part of the card that gets posted publicly.
    # Phase 0 runs on São Paulo events; when events carry their own timezone
    # this becomes a per-event value rather than a setting.
    display_timezone: str = "America/Sao_Paulo"
    # Who may read the public waitlist. Comma-separated emails; empty means
    # nobody, which is the safe default — the list is other people's contact
    # details, and "any signed-in user" is not an access rule for that.
    waitlist_admin_emails: str = ""
    environment: str = "development"

    @property
    def waitlist_admins(self) -> set[str]:
        return {
            email.strip().lower()
            for email in self.waitlist_admin_emails.split(",")
            if email.strip()
        }

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
