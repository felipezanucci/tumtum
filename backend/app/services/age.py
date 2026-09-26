"""TumTum is for adults (LGPD audit, CR-4).

A heartbeat is health data, and the legal opinion keeps the product to people
of 18 and over rather than taking on the parental-consent regime of art. 14.
The birth date is the person's own statement — the audit is explicit that a
statement alone is weak — and the minimum is what the server enforces, so no
client can be the only thing standing between a teenager and an account.
"""

from datetime import date

MIN_AGE = 18

UNDER_AGE = "Você precisa ter 18 anos ou mais para usar a TumTum."


def age_on(birth_date: date, today: date) -> int:
    """Whole years lived by `today`; a birthday counts on the day itself."""
    years = today.year - birth_date.year
    if (today.month, today.day) < (birth_date.month, birth_date.day):
        years -= 1
    return years


def is_adult(birth_date: date, today: date) -> bool:
    """Whether someone born on `birth_date` is 18 or over on `today`.

    A date in the future is not an adult either: it is a slip of the picker,
    and "born next year" must not pass as "old enough".
    """
    if birth_date > today:
        return False
    return age_on(birth_date, today) >= MIN_AGE


def today_local() -> date:
    """Today where TumTum's people are — an 18th birthday starts at their midnight."""
    from datetime import datetime
    from zoneinfo import ZoneInfo

    from app.config import settings

    return datetime.now(ZoneInfo(settings.display_timezone)).date()
