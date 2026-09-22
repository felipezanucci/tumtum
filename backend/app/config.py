from pydantic_settings import BaseSettings


def _emails(value: str) -> set[str]:
    return {email.strip().lower() for email in value.split(",") if email.strip()}


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
    # Who operates the platform: registers events, attaches a match or a
    # setlist to one, corrects what is there. Comma-separated emails. Events
    # are TumTum's — the fan never creates one (product rule, 21/09) — so an
    # empty list closes those endpoints to everyone, and the waitlist's own
    # admins are admins here too so that one setting on Railway is enough.
    admin_emails: str = ""
    # Resend. Empty means the app cannot send mail, and every path that needs
    # to says so out loud rather than pretending it sent something.
    resend_api_key: str = ""
    # Must sit on the domain verified with Resend — `mail.tumtum.cc`. Sending
    # from anywhere else is refused.
    email_from: str = "TumTum <oi@mail.tumtum.cc>"
    # Replies go somewhere a person reads. `mail.tumtum.cc` only sends; a
    # reply to it would vanish, and someone who answers "não fui eu que pedi"
    # deserves to reach a human rather than a black hole.
    email_reply_to: str = "oi@tumtum.cc"
    # Where a reset link points. The API and the site are different hosts, so
    # this cannot be derived from the request.
    site_url: str = "https://tumtum.cc"
    environment: str = "development"

    @property
    def waitlist_admins(self) -> set[str]:
        return _emails(self.waitlist_admin_emails)

    @property
    def admins(self) -> set[str]:
        return _emails(self.admin_emails) | self.waitlist_admins

    def is_admin(self, email: str) -> bool:
        return email.strip().lower() in self.admins

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
