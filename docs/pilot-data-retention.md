# Plano de descarte dos dados do piloto

26/09/2026. Nasceu da auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, CR-3,
item D6): não havia data, rotina nem responsável para apagar o que o piloto
coleta. Este plano é a promessa do item 5 do termo
(`docs/pilot-consent-template.md`) posta em passos.

**A regra:** tudo o que o piloto gerou sobre os participantes some **30 dias
depois do evento**. Data de descarte: **[PENDENTE — Felipe: data do evento +
30 dias]**. Responsável: **Felipe Zanucci** (encarregado, `docs/dpo.md`).
Suplente: [PENDENTE — Felipe].

O que **não** some com o piloto, e por quê, está no fim ("O que fica").

---

## 1. Inventário: onde o dado do piloto existe

| # | Onde | O que tem | Como sai |
|---|---|---|---|
| 1 | **Banco no Railway** (Postgres) | `hr_sessions` do evento, `hr_data` (série bruta), `peaks`, `cards` + `shares`, `event_posts` + `event_post_reactions` + `post_reports` + `series_posts` do evento | `purge_event_data.py` (seção 2) |
| 2 | **Redis no Railway** | `card:image:{id}` e `card:image:{id}:og` dos cards do evento (expiram sozinhos em 7 dias) | O mesmo script apaga as chaves na hora |
| 3 | **Celulares dos participantes** (app `cc.tumtum.app`) | As noites no banco local (criptografado), cards em `cacheDir/cards`, cards salvos na galeria (DCIM/Camera), fotos de card | A pessoa apaga a noite no app ("Apagar esta noite") ou apaga a conta. Os cards que **ela** salvou na galeria ou postou são dela: avisar, não apagar |
| 4 | **Celulares emprestados pela equipe** (operador) | Tudo do item 3, de quem gravou nele | Apagar cada noite no app, sair da conta, e *Configurações do Android → Apps → TumTum → Limpar dados* |
| 5 | **ZIPs de operador** (`tumtum-night<N>.zip`, export bruto com R-R e movimento) | A série crua de uma noite | Listar cada ZIP gerado (seção 3), apagar do celular e de **todo destino para onde foi mandado** (Drive, e-mail, WhatsApp, computador) |
| 6 | **Sentry** | Eventos de erro do período. Desde 26/09 sem corpo de requisição, sem cookies, sem variáveis locais, mas ainda com caminhos de URL (IDs de sessão/card) | Apagar os eventos do período no projeto do Sentry (*Issues → selecionar → Delete*) ou confirmar que a retenção do plano já passou: [PENDENTE — retenção do plano do Sentry] |
| 7 | **Resend** | Log dos e-mails de código e de senha mandados aos participantes (endereço, assunto, data) | Retenção do próprio Resend: [PENDENTE — conferir no painel] |
| 8 | **Backups do Railway** | Cópias do banco com tudo do item 1 | Não dá para apagar seletivamente. Saem quando o backup expira: [PENDENTE — prazo do Railway] (`docs/backups.md`) |
| 9 | **Termos assinados e planilha de controle** | Nome, data de nascimento, escolhas | **Ficam** (prova do consentimento), fora do repositório; ver o termo |

## 2. O script: `backend/scripts/purge_event_data.py`

Apaga, de **um evento**, toda sessão, leitura, momento, card (com os
compartilhamentos), post (com reações, denúncias e vínculo com a turnê) e as
chaves de imagem de card no Redis. Não apaga contas, consentimentos nem o
evento em si.

**Antes de rodar:**

1. Ache o `event_id` do evento do piloto (web admin, ou `GET /api/events`).
2. Conte o que existe (seção 4) e anote os números.
3. Confirme que ninguém quer guardar a noite fora do piloto. Quem marcou o
   item C do termo e quer continuar com a noite na conta **não** está coberto
   pelo "piloto": decida com a pessoa antes, e registre a decisão.

**Rodar** (do diretório `backend/`, com as variáveis de produção):

```bash
railway link            # uma vez: escolha o projeto e o serviço do backend
railway run python scripts/purge_event_data.py <event_id>         # só conta
railway run python scripts/purge_event_data.py <event_id> --yes   # apaga
```

**Sem `--yes` o script só conta** o que apagaria: rode assim primeiro e
confira os números com as contagens da seção 4. `railway run` injeta
`DATABASE_URL`, `DATABASE_SSL` e `REDIS_URL` do serviço, sem que ninguém
copie senha para um arquivo. Nunca rode com um `.env` de produção salvo no
disco.

**Depois de rodar com `--yes`:** repita as contagens da seção 4. Todas têm de
dar 0.

## 3. Registro dos ZIPs de operador

Cada export de noite feito no piloto entra nesta tabela **no dia em que foi
feito**. A tabela vive **fora do repositório**, junto com os termos; aqui fica
só o modelo.

| Noite | Gerado em | Por quem | Mandado para (cada destino) | Apagado em | Conferido por |
|---|---|---|---|---|---|
| `tumtum-night<N>.zip` | dd/mm | | | | |

Desde 26/09 o nome do arquivo não carrega mais o identificador do
participante, nem o ZIP o MAC do sensor.

## 4. Como comprovar

Rodar antes e depois do script, pelo `railway run` ou pelo painel de dados do
Railway, com o `event_id` no lugar de `:e`:

```sql
SELECT count(*) FROM hr_sessions WHERE event_id = :e;
SELECT count(*) FROM hr_data d JOIN hr_sessions s ON s.id = d.session_id WHERE s.event_id = :e;
SELECT count(*) FROM peaks p JOIN hr_sessions s ON s.id = p.session_id WHERE s.event_id = :e;
SELECT count(*) FROM cards c JOIN hr_sessions s ON s.id = c.session_id WHERE s.event_id = :e;
SELECT count(*) FROM event_posts WHERE event_id = :e;
```

E no Redis, para cada `card_id` anotado antes: `EXISTS card:image:<card_id>`
tem de dar 0.

A prova do descarte é o **registro de descarte** (fora do repositório, junto
com os termos): data e hora, quem rodou, o `event_id`, as contagens antes e
depois, a lista de ZIPs apagados com seus destinos, e o status de cada
celular. No repositório entra só uma linha no `docs/decision-log.md`:
*"piloto <evento>: dados descartados em dd/mm, contagens a zero"* — sem nome
nenhum.

## 5. Checklist

| # | Passo | Data | Responsável | Feito |
|---|---|---|---|---|
| 1 | Data de descarte escrita no termo de cada participante | antes do evento | Felipe | [ ] |
| 2 | Registro de ZIPs aberto (vazio) e planilha de controle criada, fora do repositório | antes do evento | Felipe | [ ] |
| 3 | No dia seguinte ao evento: cada ZIP gerado está no registro com seus destinos | evento + 1 | Felipe | [ ] |
| 4 | Aviso aos participantes, uma semana antes: "seus dados do piloto saem em dd/mm; se quiser guardar a noite, fala com a gente" | descarte − 7 | Felipe | [ ] |
| 5 | Contagens "antes" anotadas, e o script rodado sem `--yes` bate com elas | descarte | Felipe | [ ] |
| 6 | `purge_event_data.py <event_id> --yes` rodado | descarte | Felipe | [ ] |
| 7 | Contagens "depois" = 0, Redis sem as chaves | descarte | Felipe | [ ] |
| 8 | ZIPs apagados de cada destino | descarte | Felipe | [ ] |
| 9 | Celulares emprestados limpos (item 4 do inventário) | descarte | Felipe | [ ] |
| 10 | Eventos do Sentry do período apagados, ou retenção confirmada | descarte | Felipe | [ ] |
| 11 | Aviso aos participantes: "apagamos"; lembrar que cards salvos ou postados por eles são deles | descarte | Felipe | [ ] |
| 12 | Registro de descarte fechado; linha no decision log | descarte | Felipe | [ ] |
| 13 | Backup mais antigo com dados do piloto expirou | descarte + [PENDENTE — prazo do Railway] | Felipe | [ ] |

## O que fica, e por quê

- **As contas** dos participantes. Uma conta não é dado do piloto: a pessoa
  apaga quando quiser (Configurações → Apagar conta). Oferecer no aviso do
  passo 11.
- **Os consentimentos** (`consents`). Provam o que a pessoa autorizou; saem
  com a conta.
- **O `access_log`** (IP, rota, horário). Guarda obrigatória de 6 meses pelo
  Marco Civil da Internet (art. 15); o ciclo de manutenção apaga aos 180 dias.
  Não tem batimento.
- **O `data_access_log`** (quem leu o quê, sem bpm). Sai com a conta.
- **Os termos assinados**: 5 anos após o descarte, fora do repositório.
- **O evento** e sua linha do tempo (músicas, gols): não são dados pessoais.
