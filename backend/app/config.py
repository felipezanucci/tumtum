from pydantic_settings import BaseSettings


def _emails(value: str) -> set[str]:
    return {email.strip().lower() for email in value.split(",") if email.strip()}


# Every placeholder this repository has ever shipped as a key. Each one is
# readable by anybody, so each one signs tokens anybody can make.
WEAK_SECRET_KEYS = frozenset(
    {
        "",
        "your-secret-key",
        "dev-secret-key-change-in-production",
        "change-me-to-a-random-secret-key",
        "test-secret-key",
    }
)
MIN_SECRET_KEY_LENGTH = 32


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
    # The guard below refuses to start on a placeholder key. Only a local
    # compose stack and CI, whose tokens sign nothing anybody can use, may
    # say so; Railway never sets this.
    allow_weak_secret_key: bool = False
    # asyncpg does not negotiate TLS unless asked. On Railway's private
    # network (`*.railway.internal`) the traffic never leaves it; a public
    # database host needs this true.
    database_ssl: bool = False
    # "A galera" (card 04): the number of consenting nights an event needs
    # before any collective figure is published, and the fewest people a
    # published minute may describe. The defaults are the legal opinion's
    # numbers (LGPD audit, AL-4); tests pass their own, production does not
    # lower them.
    crowd_min_nights: int = 100
    crowd_min_cell: int = 10
    # How long the raw series of a night outlives its analysis. The moments,
    # the night's summary and its cards stay while `keep_night` does; the
    # second-by-second readings are what the moments were made from, and
    # after this many days they go (contract: raw readings retention).
    raw_readings_retention_days: int = 30

    @property
    def waitlist_admins(self) -> set[str]:
        return _emails(self.waitlist_admin_emails)

    @property
    def admins(self) -> set[str]:
        return _emails(self.admin_emails) | self.waitlist_admins

    def is_admin(self, email: str) -> bool:
        return email.strip().lower() in self.admins

    def secret_key_problem(self) -> str | None:
        """Why this server must not start with its `SECRET_KEY`, or None.

        The key signs every access token and keys the hash of every sign-up
        code. The code's own default is public — it sits in this file on a
        public repository — so a deploy that forgot the variable would accept
        a token anyone can forge for any account (LGPD audit, CR-5). A
        placeholder or a short key is refused at startup instead of being
        discovered afterwards.
        """
        if self.allow_weak_secret_key:
            return None
        if self.secret_key in WEAK_SECRET_KEYS:
            return "SECRET_KEY is a published placeholder"
        if len(self.secret_key) < MIN_SECRET_KEY_LENGTH:
            return f"SECRET_KEY is shorter than {MIN_SECRET_KEY_LENGTH} characters"
        return None

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
