"""Waitlist rules that are not about storage.

Small, but the piece that decides whether two submissions are one person.
"""


def normalize_email(raw: str) -> str:
    """The form in which an address is stored and compared.

    Addresses arrive from a phone keyboard, which capitalises the first letter
    by default and is generous with trailing spaces. `Felipe@Gmail.com ` and
    `felipe@gmail.com` are one person, and the unique index can only know that
    if both reach it in the same shape.

    Only case and surrounding whitespace are touched. The local part of an
    address is case-sensitive per RFC 5321 and providers are free to treat
    `a.b@` and `ab@` as different mailboxes, so anything cleverer — stripping
    dots, cutting `+tags` — would be us deciding that two real addresses are
    the same person. Lowercasing is already a small liberty; every provider we
    will meet in Brazil takes it, and the cost of being wrong is one duplicate
    row rather than a person who cannot sign up.
    """
    return raw.strip().lower()


def normalize_name(raw: str | None) -> str | None:
    """A name as typed, with the typing tidied away.

    Trims and collapses runs of whitespace, so "  Felipe   Zanucci " and
    "Felipe Zanucci" are stored the same. Nothing else: capitalisation is not
    ours to correct. "de Souza", "McDonald" and "van der Berg" are all spelled
    the way their owner spells them, and a title-casing helper would quietly
    rename people.

    Returns None for anything that was only whitespace — an empty string in a
    name column is a value that means "nothing", which is what None is for.
    """
    if raw is None:
        return None
    cleaned = " ".join(raw.split())
    return cleaned or None
