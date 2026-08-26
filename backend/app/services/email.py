"""Sending mail, through Resend.

One provider, one function, no template engine. The only message TumTum sends
today is a password reset, and a mail layer built for messages that do not
exist yet is a layer nobody can verify.
"""

import httpx

from app.config import settings

RESEND_ENDPOINT = "https://api.resend.com/emails"


class EmailNotConfigured(RuntimeError):
    """No API key. Raised rather than silently returning success.

    A send that quietly does nothing is the failure this project keeps
    meeting: the caller believes a mail is on its way, the person waits for it
    forever, and nothing anywhere says otherwise.
    """


async def send_email(*, to: str, subject: str, html: str, text: str) -> None:
    if not settings.resend_api_key:
        raise EmailNotConfigured("RESEND_API_KEY is not set")

    async with httpx.AsyncClient(timeout=15) as client:
        response = await client.post(
            RESEND_ENDPOINT,
            headers={"Authorization": f"Bearer {settings.resend_api_key}"},
            json={
                "from": settings.email_from,
                "to": [to],
                "reply_to": settings.email_reply_to,
                "subject": subject,
                "html": html,
                # Some clients, and some corporate filters, only ever see this.
                "text": text,
            },
        )
        response.raise_for_status()
