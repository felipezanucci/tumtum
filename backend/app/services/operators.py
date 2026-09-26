"""Who processes personal data on TumTum's behalf — the one list (v1.1 §7).

The person's "Ver compartilhamentos" screen (`GET /api/users/me/sharing`),
the privacy policy and `docs/ropa.md` all describe the same operators; this
is the list they must agree with, so a new provider is added here first and
the documents cite it. Each entry says what the operator receives, why, and
where — and a region not yet confirmed says `[a confirmar]` rather than a
guess, because a wrong answer on a screen about data transfers is worse
than an honest gap.

`NEVER_SHARED_WITH` is the other half of the promise: nobody on it is an
operator, and nothing personal goes to them (product rule, 26/09).
"""

from dataclasses import dataclass


@dataclass(frozen=True)
class Operator:
    name: str
    role: str  # "operador" — or "nenhum dado pessoal" for a source we only read
    what: str
    why: str
    where: str


OPERATORS: tuple[Operator, ...] = (
    Operator(
        name="Railway",
        role="operador",
        what=("conta, noites, momentos, cards, consentimentos e registros de acesso"),
        why="hospedagem da API e do banco de dados",
        where="região [a confirmar]",
    ),
    Operator(
        name="Redis no Railway",
        role="operador",
        what="a imagem do card, por 7 dias",
        why="entregar a imagem do card rápido, sem desenhar de novo",
        where="região [a confirmar]",
    ),
    Operator(
        name="Vercel",
        role="operador",
        what="metadados de requisição e endereço IP de quem abre o site",
        why="hospedagem do site tumtum.cc",
        where="região [a confirmar]",
    ),
    Operator(
        name="Resend",
        role="operador",
        what="e-mail e nome",
        why="mandar os códigos de cadastro, de troca de e-mail e de "
        "recuperação de senha",
        where="região [a confirmar]",
    ),
    Operator(
        name="Sentry",
        role="operador",
        what="o erro técnico, sem corpo de requisição, sem cookies, sem senha",
        why="achar e corrigir erros do servidor — só quando está ligado",
        where="região [a confirmar]",
    ),
    Operator(
        name="API-Football",
        role="nenhum dado pessoal",
        what="nada seu: a TumTum só lê dela os eventos da partida",
        why="saber a hora do gol, do intervalo e do fim do jogo",
        where="—",
    ),
)

NEVER_SHARED_WITH: tuple[str, ...] = (
    "clubes",
    "artistas",
    "produtoras",
    "festivais",
    "anunciantes",
)
