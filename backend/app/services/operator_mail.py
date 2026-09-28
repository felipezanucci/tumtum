"""A mail to every operator, when something needs a person.

A report on the feed (#36) and a data-subject request (art. 18, due in 15
days) both land in a queue on the site — and a queue nobody is told about is
a queue nobody reads. So each one also sends a mail to `ADMIN_EMAILS`, with
the link to the page that answers it. Never blocks the act that caused it: a
mail outage must not undo a report or a request.
"""

import html

from app.config import settings
from app.services.email import EmailNotConfigured, send_email


async def tell_operators(*, subject: str, sentence: str, path: str) -> None:
    """`sentence`, then the link to `path` on the site, to every operator.

    `sentence` may carry text a person typed (an event's name); it is escaped
    for the HTML part, so markup in it is never markup in the operators' mail.
    """
    if not settings.admins:
        return
    link = f"{settings.site_url}{path}"
    text = f"{sentence}\n\nVeja e decida em {link}"
    markup = (
        f"<p>{html.escape(sentence)}</p>"
        f'<p>Veja e decida em <a href="{html.escape(link)}">{html.escape(link)}</a></p>'
    )
    for to in settings.admins:
        try:
            await send_email(to=to, subject=subject, html=markup, text=text)
        except EmailNotConfigured:
            return
        except Exception:  # one bad address must not silence the others
            continue
