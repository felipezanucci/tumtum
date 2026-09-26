# Contratos com operadores (DPA) — checklist

26/09/2026. A auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, item H6) achou
quatro operadores recebendo dado pessoal sem nenhum contrato de tratamento
referenciado, e sem região conhecida. Os quatro publicam um DPA (acordo de
tratamento de dados) padrão; na maioria dos planos ele já vale por referência
nos termos de uso, mas **é preciso baixar a versão vigente, guardar, e anotar
a região** — sem isso o `docs/ropa.md` não fecha.

**Onde guardar os PDFs:** fora do repositório, na mesma pasta dos termos do
piloto [PENDENTE — Felipe]. Aqui entra só a data e a região.

**O que conferir em cada DPA:**

1. Que cobre o plano contratado (alguns só valem em planos pagos).
2. Lista de suboperadores e como avisam de mudanças.
3. Aviso de incidente ao controlador, e em quanto tempo (precisamos disso
   para cumprir os 3 dias úteis de `docs/incident-response.md`).
4. Exclusão/devolução dos dados ao fim do contrato.
5. **Transferência internacional:** se a região não for o Brasil, a LGPD
   (art. 33) pede um mecanismo; o padrão hoje são as **cláusulas-padrão
   contratuais da ANPD** (Resolução CD/ANPD nº 19/2024). Ver se o DPA as
   inclui, ou se o operador oferece um aditivo com elas.

## Os quatro

Os endereços abaixo são o ponto de partida conhecido; confirmar no site de
cada um, porque mudam.

| Operador | O que assinar/baixar | Onde | Onde ver a região |
|---|---|---|---|
| **Railway** (API, Postgres, Redis) | DPA da Railway | railway.com → *Legal* → DPA; ou pedir ao suporte pelo painel | Cada serviço → *Settings* → *Region* (banco e backups: ver `docs/backups.md`) |
| **Vercel** (site tumtum.cc) | DPA da Vercel (vercel.com/legal/dpa) | Incorporado aos termos; baixar o PDF | Projeto → *Settings* → *Functions* → *Function Region*. Existe `gru1` (São Paulo) |
| **Resend** (e-mails) | DPA da Resend (resend.com/legal/dpa) | Site; alguns planos pedem assinatura pelo suporte | *Domains* → `mail.tumtum.cc` → região do domínio de envio |
| **Sentry** (erros) | DPA da Sentry (sentry.io/legal/dpa) | *Settings* → *Legal & Compliance* → aceitar o DPA | *Settings* → organização: **US** ou **EU**, escolhido na criação e não muda |

## Registro

| Operador | DPA baixado em | Versão/data do DPA | Região | Fora do Brasil? Mecanismo (art. 33) | Aviso de incidente em | Conferido por |
|---|---|---|---|---|---|---|
| Railway | [PENDENTE] | | [PENDENTE — região] | | | |
| Vercel | [PENDENTE] | | [PENDENTE — região] | | | |
| Resend | [PENDENTE] | | [PENDENTE — região] | | | |
| Sentry | [PENDENTE] | | [PENDENTE — região] | | | |

Também anotar, mesmo sem DPA formal:

| Serviço | Pergunta | Resposta |
|---|---|---|
| Caixa de e-mail oi@tumtum.cc | Qual provedor? Região? Tem DPA? | [PENDENTE — Felipe] |
| Cloudflare | O tráfego de tumtum.cc passa pelo proxy (nuvem laranja) ou só o DNS? | [PENDENTE — Felipe] |

**Sentry sem DPA:** se o DPA não puder ser aceito no plano atual, a
alternativa segura é deixar `SENTRY_DSN` vazio no Railway até poder (o backend
funciona sem ele). Registrar a escolha no `docs/decision-log.md`.

Quando esta tabela estiver cheia, copie as regiões para `docs/ropa.md` §2 e
tire os `[PENDENTE]` de lá.
