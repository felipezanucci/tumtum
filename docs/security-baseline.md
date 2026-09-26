# Baseline de segurança — os cliques do Felipe

27/09/2026. É o checklist do item 16 (segurança dos painéis e dos segredos):
o que o `docs/ripd.md` carrega como **RR13** (sem MFA verificado nos painéis,
sem cofre de segredos) e **RR15** (um único administrador, sem revisão de
acesso), e o decision log como **P0.7**, parcial. Nada aqui se faz pelo
código: são configurações em oito painéis, e só o dono de cada conta chega
nelas. Item 83 do `docs/decision-log.md`.

**Como usar.** Uma tabela por sistema. Fazer a linha, guardar a evidência,
escrever a data em *Feito em*. Os caminhos de menu são os conhecidos em
27/09/2026; painel muda de nome, e onde o caminho não pôde ser conferido está
escrito *(conferir o nome)*. Se o caminho real for outro, corrigir aqui.

**Onde guardar as evidências:** prints e PDFs **fora do repositório**, na mesma
pasta dos DPAs e dos termos do piloto `[PENDENTE — Felipe: qual pasta]`. Um
print de painel costuma mostrar e-mail, IP ou nome — é dado pessoal e não
entra no git (o `privacy-scan` pega e-mail, não pega imagem). Aqui entra só a
data.

**Regras que valem para todos os sistemas:**

- **2FA por app autenticador ou chave física**, nunca só SMS. Os códigos de
  recuperação ficam no gerenciador de senhas e numa cópia offline — não no
  e-mail, não no Drive em texto aberto.
- **Uma conta por pessoa.** Nenhuma senha de painel é compartilhada; quem
  precisa de acesso é convidado com a própria conta e o menor papel que
  resolve (o processo está em `docs/confidentiality-and-training.md` §C).
- **Nenhum segredo colado** em chat, e-mail, documento, issue, PR ou sessão de
  IA. Se colou, é incidente nível 2 (`docs/incident-response.md` §3) e o
  segredo é rotacionado na hora.

---

## 1. GitHub — `felipezanucci/tumtum`

O repositório é **público** e está na conta pessoal, não numa organização.
Railway e Vercel fazem deploy a partir de `main`: quem escreve em `main`
escreve em produção.

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **2FA na conta** | Avatar → *Settings* → *Password and authentication* → *Two-factor authentication* → *Enable*. App autenticador; depois, em *Passkeys* / *Security keys*, uma chave física se houver. Baixar os *recovery codes* | Print da página com 2FA *Enabled* (sem os códigos) | |
| **2FA na organização** | Não se aplica hoje: não há organização. Se o repositório for movido para uma: *Organization* → *Settings* → *Authentication security* → *Require two-factor authentication for everyone* | — | — |
| **Colaboradores** | Repositório → *Settings* → *Collaborators*. Hoje deve ser só o Felipe. Qualquer outro nome: tirar ou justificar aqui | Print da lista | |
| **Apps com acesso ao repositório** | Avatar → *Settings* → *Applications* → *Installed GitHub Apps* (Railway, Vercel, Claude) e *Authorized OAuth Apps*. Em cada app instalado: *Configure* → *Repository access* → *Only select repositories*. Revogar o que ninguém reconhece | Print das duas listas | |
| **Branch protection em `main`** | Repositório → *Settings* → *Rules* → *Rulesets* → *New branch ruleset* (ou *Branches* → *Add classic branch protection rule*). Alvo: `main`. Ligar: *Require a pull request before merging* com **0 aprovações** (Felipe faz o próprio merge; 1 aprovação o trancaria para fora); *Require status checks to pass* com **Privacy Scan**, **Backend Test**, **Backend Lint**, **Frontend Build**, **Frontend Test**, **Frontend Lint** (os nomes dos jobs de `.github/workflows/ci.yml`); *Block force pushes*; *Restrict deletions*. **Antes**, abrir *Actions* → *CI* e ver o último run em `main` verde — com o `privacy-scan` vermelho (item 79), exigi-lo tranca todo merge até o código ser corrigido | Print do ruleset ativo | |
| **Assinatura de commits** (opcional) | *Settings* → *SSH and GPG keys* → *New SSH key* → *Key type: Signing Key*. No computador: `git config --global gpg.format ssh`, `git config --global user.signingkey <caminho da chave .pub>`, `git config --global commit.gpgsign true`. Na mesma página, *Vigilant mode* marca como *Unverified* o que não for assinado. Os commits das sessões do Claude Code não saem assinados com a chave do Felipe — por isso não exigir assinatura no ruleset | Print da chave de assinatura cadastrada | |
| **Revisão dos secrets do repositório** | Repositório → *Settings* → *Secrets and variables* → *Actions*. Os que o código usa são **quatro**: `TUMTUM_UPLOAD_KEYSTORE_BASE64`, `TUMTUM_UPLOAD_KEYSTORE_PASSWORD`, `TUMTUM_UPLOAD_KEY_ALIAS`, `TUMTUM_UPLOAD_KEY_PASSWORD` (`build-app.yml`, job `release`). Qualquer outro: descobrir para que serve ou apagar | Print da lista (o GitHub mostra só nomes e datas) | |
| **Tokens pessoais com prazo** | Avatar → *Settings* → *Developer settings* → *Personal access tokens*. Em *Tokens (classic)*: apagar todo token sem data de expiração. Novos só em *Fine-grained tokens*, com *Expiration* de no máximo 90 dias e *Repository access* só no que precisa. Mesma revisão em *Settings* → *SSH and GPG keys*: apagar chave de computador que não existe mais | Print das listas | |
| **Dependabot alerts** | Repositório → *Settings* → *Code security* (*Code security and analysis* em contas antigas) → *Dependabot alerts* → *Enable*. Opcional: *Dependabot security updates* (abre PR sozinho). No mesmo lugar, ligar *Secret scanning* e *Push protection* — grátis em repositório público, e pegam chaves que o `privacy-scan` não conhece | Print da página com os três ligados | |
| **E-mail de notificação** | *Settings* → *Notifications*: alertas de segurança vão para um endereço que alguém lê | — | |

## 2. Railway — API, Postgres, Redis

Onde mora tudo o que sobe: contas, ledger, noites guardadas, logs. É o painel
mais sensível da lista.

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **2FA** | Avatar → *Account Settings* → *Security* → *Two-Factor Authentication* *(conferir o nome)*. Se a conta entra por "Login with GitHub", o 2FA que protege é o do GitHub (§1) — fazer os dois | Print com 2FA ativo | |
| **Contas individuais** | Workspace/projeto → *Settings* → *Members* *(conferir o nome)*. Cada pessoa com a própria conta; nenhuma credencial do Felipe em outro computador. Quem pode ver *Variables* pode ler o `SECRET_KEY` — o papel mais baixo que resolve | Print da lista de membros | |
| **Tokens de API** | *Account Settings* → *Tokens*: apagar os que ninguém usa. Projeto → *Settings* → *Tokens*: idem | Print | |
| **Variáveis como segredo** | Serviço do backend → *Variables*. Em `SECRET_KEY`, `RESEND_API_KEY`, `API_FOOTBALL_KEY`, `SETLIST_FM_API_KEY`: menu **⋮** da variável → *Seal* *(conferir)* — o valor deixa de aparecer no painel e na API. Selar é sem volta: guardar o valor no gerenciador **antes** | Print da lista com os cadeados (sem valores) | |
| **`SECRET_KEY` forte** | Mesma aba. ≥ 32 caracteres, gerado com `python -c "import secrets; print(secrets.token_urlsafe(48))"` no próprio computador. Desde 26/09 a API **não sobe** com um placeholder ou chave curta (`config.py`, `WEAK_SECRET_KEYS`). `ALLOW_WEAK_SECRET_KEY` **não existe** no Railway (ou é `false`) | Anotar só "conferido, N caracteres" | |
| **Rotação do `SECRET_KEY`** | Procedimento completo no §9, com o passo que faz todo mundo entrar de novo — **trocar a chave sozinha não desloga ninguém** | Linha no decision log com data e motivo, nunca o valor | |
| **`DATABASE_SSL`** | `true`, a não ser que `DATABASE_URL` aponte para `*.railway.internal` (rede privada, o tráfego não sai dela) | Print de `DATABASE_URL` com a senha coberta, mostrando o host | |
| **Postgres sem porta pública** | Serviço *Postgres* → *Settings* → *Networking* → *Public Networking* *(conferir)*: se há um TCP proxy público e nada fora do Railway precisa dele, remover. Banco exposto na internet protegido só por senha é o cenário D6 do RIPD | Print | |
| **`ENVIRONMENT=production`** | Variável do backend. Esconde `/api/demo/*`. Conferir abrindo `https://<host da API>/api/demo/seed` no navegador: **404** em produção (a rota nem é montada); **405** quer dizer que ela existe e a variável está errada | Print da variável | |
| **As outras variáveis** | `SENTRY_DSN` vazio **ou** com DPA e LIA (§5); `ADMIN_EMAILS` e `WAITLIST_ADMIN_EMAILS` só com quem opera; `CROWD_MIN_NIGHTS` e `CROWD_MIN_CELL` ausentes ou 100 e 10 (nunca menores em produção); `RAW_READINGS_RETENTION_DAYS` ausente ou 7 | Anotar "conferido" | |
| **Backups e região** | As sete perguntas de `docs/backups.md` (frequência, prazo, criptografia, região, quem restaura, Redis, dumps em computador). A resposta vai para lá e para `docs/dpa-checklist.md` | A tabela de `docs/backups.md` preenchida | |
| **Deploy só de `main`** | Serviço do backend → *Settings* → *Source*: branch `main`. Com o ruleset do §1, nada chega aqui sem CI verde | Print | |

## 3. Vercel — tumtum.cc

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **2FA** | Avatar → *Account Settings* → *Authentication* → *Two-Factor Authentication* *(conferir)*. Se a conta entra pelo GitHub, fazer também o §1 | Print | |
| **Membros** | *Team Settings* → *Members*: só quem precisa, cada um com a própria conta | Print | |
| **`NEXT_PUBLIC_API_URL` no build** | Projeto → *Settings* → *Environment Variables*: aponta para a API do Railway em *Production* (e em *Preview*, se os previews devem falar com ela). É embutida **no build**: trocar o valor não muda nada até *Deployments* → último deploy → **⋮** → *Redeploy*. Tudo que começa com `NEXT_PUBLIC_` vai para o navegador de qualquer pessoa — **nunca** um segredo com esse prefixo. Conferir: o site entra na conta; se `lib/api.ts` disser *"Não foi possível falar com o servidor em http://localhost:8000"*, a variável falta | Print da lista de variáveis | |
| **Proteção de deploy de preview** | Projeto → *Settings* → *Deployment Protection* → *Vercel Authentication*: **Standard Protection** (previews atrás de login, produção aberta). Está assim desde 25/08 — o decision log registra previews atrás de login "by design". Não ligar *Protection Bypass for Automation* nem links compartilháveis de preview sem motivo escrito aqui | Print | |
| **Logs** | Projeto → *Logs*. Guardam IP, caminho (`/cards/<id>`) e user agent de cada visita. **Não** criar *Log Drain* para terceiro sem entrar no `docs/dpa-checklist.md` primeiro. Prazo de retenção do plano: `[PENDENTE — conferir no plano]`, anotar no `docs/data-retention-policy.md` | Print da página *Settings* → *Log Drains* vazia | |
| **Região das funções** | Projeto → *Settings* → *Functions* → *Function Region* (existe `gru1`, São Paulo). Resposta para `docs/dpa-checklist.md` | — | |
| **Deploy de produção só de `main`** | Projeto → *Settings* → *Git* → *Production Branch*: `main` | Print | |

## 4. Resend — os e-mails de código e senha

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **2FA** | *Settings* → conta → autenticação de dois fatores *(conferir o nome)* | Print | |
| **Membros** | *Settings* → *Team*: só o Felipe | Print | |
| **Chave com escopo mínimo** | *API Keys* → *Create API Key* → *Permission*: **Sending access** (não *Full access*) → *Domain*: `mail.tumtum.cc`. É a única coisa que o backend faz com ela. Apagar toda outra chave da lista | Print da lista: uma chave, *Sending access*, um domínio | |
| **Rotação da chave** | §9 | Linha no decision log | |
| **Domínio `mail.tumtum.cc`** | *Domains* → `mail.tumtum.cc` → *Verified* (verificado em 26/08/2026, seis minutos). Os registros SPF/DKIM ficam no DNS do subdomínio — ninguém apaga (§7). Envio só de `oi@mail.tumtum.cc` (`EMAIL_FROM` no `config.py`); a resposta vai para oi@tumtum.cc | Print do domínio verificado | Verificado 26/08/2026 |
| **Sem rastreio de abertura e clique** | *Domains* → `mail.tumtum.cc` → *Configuration* → *Open tracking* e *Click tracking* **desligados** *(conferir)*. Um pixel num e-mail de código de cadastro é dado de comportamento que nenhuma finalidade da TumTum cobre | Print | |
| **O que o Resend guarda** | A aba *Emails* mostra cada mensagem enviada, com destinatário e conteúdo (código de 6 dígitos incluso). Prazo do plano: `[PENDENTE — conferir]`, anotar no `docs/data-retention-policy.md` | — | |
| **DMARC** (opcional, com cuidado) | Um registro `_dmarc` no domínio raiz alcança **também** o Google Workspace, que envia de oi@tumtum.cc. Começar com `p=none`, ler os relatórios, e só então endurecer. Nunca mexer nos registros MX (§7) | — | |

## 5. Sentry — erros do backend

Só existe se `SENTRY_DSN` estiver preenchido no Railway. Se não estiver, a
tabela inteira é "não se aplica" — e é a saída segura enquanto a LIA do Sentry
(`docs/ropa.md` §4, O22) e o DPA não existirem.

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **Existe?** | Railway → backend → *Variables* → `SENTRY_DSN`. Vazio: parar aqui e escrever "sem Sentry" em *Feito em* | — | |
| **2FA** | *User Settings* → *Account* → *Security* → *Two-Factor Authentication*. Na organização: *Organization Settings* → *General Settings* → *Security & Privacy* → *Require Two-Factor Authentication* | Print dos dois | |
| **Data scrubbing ligado** | *Organization Settings* → *Security & Privacy*: *Data Scrubber* **on**, *Use Default Scrubbers* **on**, *Prevent Storing of IP Addresses* **on**; em *Additional Sensitive Fields*: `bpm`, `email`, `birth_date`, `password`, `token`, `code`. O backend já manda sem PII, sem corpo e sem variáveis locais (`send_default_pii=False`); o scrubber é a segunda camada | Print | |
| **DSN** | *Project Settings* → *Client Keys (DSN)*. Não dá acesso aos dados, mas deixa qualquer um enviar eventos falsos. Se vazar: *Generate New Key*, trocar no Railway, desabilitar a antiga | — | |
| **Retenção de eventos** | Definida pelo plano, em geral sem controle nos planos baixos: `[PENDENTE — conferir o número]`, anotar no `docs/data-retention-policy.md` | Print da página do plano | |
| **DPA e região** | *Organization Settings* → *Legal & Compliance* → aceitar o DPA; região (US ou EU) escolhida na criação. Registro em `docs/dpa-checklist.md` | PDF do DPA fora do repo | |
| **Membros** | *Organization Settings* → *Members* | Print | |

## 6. Google Play Console — `cc.tumtum.app`

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **2FA** | A conta Google dona do perfil de desenvolvedor: myaccount.google.com → *Segurança* → *Verificação em duas etapas* → chave de acesso ou app autenticador. O Play exige verificação em duas etapas para contas de desenvolvedor | Print | |
| **Quem tem acesso** | Play Console → *Usuários e permissões*. Cada pessoa com o próprio login Google e só as permissões do que faz; ninguém com *Administrador* além do Felipe | Print da lista | |
| **Upload key fora do repositório** | O `.jks` da chave de upload vive **só** nos quatro secrets do GitHub (§1) e numa cópia offline cifrada, com as senhas no gerenciador. Nunca em `android/`, nunca no Drive em claro. (A chave que **está** no repositório é a `debug.keystore`, dos APKs de teste — RR3 do RIPD; por isso participante recebe o app só pelo Play) | Anotar onde está a cópia offline, sem o caminho completo | |
| **Se a upload key vazar ou se perder** | Play Console → *Protegido com o Google Play* → *Proteção da Google Play Store* → *Gerencie a Assinatura de Apps* → *Solicitar redefinição da chave de upload*. A chave de assinatura do app é do Google e não muda. Depois, trocar os quatro secrets do GitHub | — | |
| **Conta de revisor** | Login e senha da conta que o revisor do Google usa ficam no gerenciador e no campo do Play Console — nunca em `docs/` (a auditoria de 26/09 tirou de lá) | — | |

## 7. DNS de tumtum.cc — Cloudflare ou GoDaddy

**Primeiro, saber quem responde pelo DNS.** O `CLAUDE.md` lista Cloudflare como
CDN, mas o decision log de 26/08 registra os registros do Resend feitos **no
GoDaddy**, e o `docs/dpa-checklist.md` ainda pergunta se o proxy da Cloudflare
está ligado. Quem controla o DNS controla o e-mail, a verificação do TikTok e
para onde o site aponta.

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **Quem é o DNS** | No computador: `dig NS tumtum.cc +short` (ou qualquer "NS lookup" na web). Nameservers `*.ns.cloudflare.com` = Cloudflare; `*.domaincontrol.com` = GoDaddy. Escrever a resposta aqui e no `docs/dpa-checklist.md` | A saída do comando | |
| **2FA no registrador** | GoDaddy: *Conta* → *Configurações de login e segurança* → *Verificação em duas etapas* *(conferir)*. Mesmo se o DNS estiver na Cloudflare, quem tem o registrador pode trocar os nameservers | Print | |
| **2FA na Cloudflare** (se usada) | *My Profile* → *Authentication* → *Two-Factor Authentication* | Print | |
| **Quem edita o DNS** | Cloudflare: *Manage Account* → *Members*. GoDaddy: *Acesso delegado* *(conferir)*. Só o Felipe | Print | |
| **Trava de transferência e renovação** | No registrador: *Domain lock* ligado, renovação automática ligada, cartão válido. Um domínio que expira leva o e-mail do encarregado junto | Print | |
| **Registros que ninguém "arruma"** | Os **MX** do domínio raiz apontam para o Google Workspace (o e-mail oi@tumtum.cc); os registros do Resend em `mail.tumtum.cc`; o TXT `tiktok-developers-site-verification` no `@`; os A/CNAME do Vercel. Apagar qualquer um deles derruba algo que ninguém liga ao DNS | Print da zona inteira, guardado como referência | |
| **Proxy (nuvem laranja)** | Se a Cloudflare estiver com proxy, ela vê IP e requisição de quem visita — é operador (`docs/dpa-checklist.md`). Anotar ligado/desligado | — | |

## 8. E-mail oi@tumtum.cc — Google Workspace

É o canal do encarregado (`docs/dpo.md`): pedido de titular, aviso de
incidente, resposta de quem recebeu um código sem pedir. Quem entra nesta
caixa lê pedidos de titulares.

| Controle | Como fazer | Evidência a guardar | Feito em |
|---|---|---|---|
| **2FA na conta** | myaccount.google.com → *Segurança* → *Verificação em duas etapas* | Print | |
| **2FA obrigatória no domínio** | admin.google.com → *Segurança* → *Autenticação* → *Verificação em duas etapas* → *Permitir* e *Aplicação* **ligada** | Print | |
| **Quem lê** | A caixa é de uma pessoa ou é um grupo/alias? admin.google.com → *Diretório* → *Usuários* e *Grupos*. Escrever aqui quem recebe: `[PENDENTE — Felipe]` | Anotar | |
| **Encaminhamentos e filtros** | Gmail → ⚙ → *Ver todas as configurações* → *Encaminhamento e POP/IMAP* e *Filtros e endereços bloqueados*. Nenhum encaminhamento que ninguém reconhece — é o truque clássico de quem invade uma caixa | Print das duas abas | |
| **Delegação e apps** | Gmail → *Contas* → *Conceder acesso à sua conta*: vazio. myaccount.google.com → *Segurança* → *Seus acessos a apps e serviços de terceiros*: revogar o que ninguém usa | Print | |
| **Recuperação** | myaccount.google.com → *Segurança*: telefone e e-mail de recuperação que são do Felipe e ainda existem | — | |
| **DPA** | O Google Workspace é operador para O19/O20 (`docs/ropa.md`). Aceitar o *Cloud Data Processing Addendum* em admin.google.com → *Conta* → *Configurações da conta* → *Termos jurídicos* *(conferir)* e registrar em `docs/dpa-checklist.md` | PDF fora do repo | |

---

## 9. Rotação de segredos: quando e como

**Quando rotacionar qualquer um deles:** (a) suspeita de vazamento — colado
num chat, numa sessão de IA, num print, num commit; (b) saída de alguém que
podia vê-lo (§C do `docs/confidentiality-and-training.md`); (c) uma vez por ano,
na revisão de setembro, mesmo sem motivo. Cada rotação é uma linha no decision
log: **data, qual segredo, por quê — nunca o valor**. Nenhum valor passa por
chat ou por sessão do Claude Code: é gerado e colado pelo Felipe, direto no
painel.

| Segredo | Onde vive | Como rotacionar | O que quebra no meio |
|---|---|---|---|
| **`SECRET_KEY`** | Railway, backend | Abaixo, em passos | Access tokens (1 h) e códigos de 6 dígitos em andamento |
| **`RESEND_API_KEY`** | Railway, backend; Resend | Resend → *API Keys* → *Create API Key* (*Sending access*, `mail.tumtum.cc`) → Railway → *Variables* → `RESEND_API_KEY` → novo valor → deploy → testar com *Esqueci a senha* na própria conta → só então apagar a chave antiga no Resend | Nada, se a antiga só for apagada depois do teste |
| **`API_FOOTBALL_KEY`** | Railway, backend; painel da API-Football | No painel da conta da API-Football (dashboard.api-football.com) gerar a nova chave *(conferir se o painel regenera; se não, pedir ao suporte)* → Railway → deploy | O acompanhamento ao vivo da partida. **Nunca rotacionar em dia de jogo cadastrado** |
| `SETLIST_FM_API_KEY` | Railway, backend; setlist.fm | setlist.fm → *API* → *My API Keys* *(conferir)* → nova chave → Railway → deploy → apagar a antiga | Busca de setlist até o deploy |
| **Snap, TikTok, Meta** | Os IDs em `android/app/src/main/res/values/instagram.xml` | **Não são segredos**: são IDs públicos de cliente (o próprio arquivo diz), impressos em toda cópia do app. Nada a rotacionar no app. A troca de staging/sandbox para produção, quando Snap e TikTok aprovarem, **não é rotação**. O que é segredo são as **contas dos portais** (developers.facebook.com, developers.snap.com, developers.tiktok.com) — 2FA em cada uma — e os *client secret* / *app secret* que os portais mostram, que a TumTum **nunca usou nem copiou**. Se um aparecer onde não devia: Meta → app → *Configurações do app* → *Básico* → *Chave secreta do app* → *Redefinir*; TikTok e Snap → a página do app no portal → *client secret* → gerar outro *(conferir o nome)* | Nada no app |
| Upload key do Play | Secrets do GitHub + cópia offline | §6, *Se a upload key vazar* | Builds de release até os secrets novos |
| Tokens pessoais (GitHub, Railway, Vercel) | Painel de cada um | Apagar e, se preciso, criar outro com prazo | O que usava o token |

### `SECRET_KEY` — o procedimento, e por que trocar a chave não desloga ninguém

O que o código faz, lido em 27/09 (`core/auth.py`, `services/refresh_tokens.py`,
`services/password_reset.py`, `services/signup_codes.py`):

- A chave **assina os access tokens** (JWT, 1 h) e **é a chave do hash dos
  códigos de 6 dígitos** (cadastro e troca de e-mail).
- Os **refresh tokens (90 dias) não dependem dela**: são valores aleatórios,
  guardados como SHA-256 simples. Trocar a chave invalida o access token de
  todo mundo, o app e o site pedem um novo com o refresh token — e conseguem.
  **Ninguém precisa entrar de novo.** O mesmo vale para os links de *esqueci
  a senha* em andamento.
- **O tombstone de exclusão** (`services/tombstones.py`, em construção em
  27/09): a marca do e-mail de uma conta apagada é um HMAC com esta chave.
  Depois de uma rotação, as marcas antigas de e-mail deixam de bater; a marca
  do id da conta não depende da chave e continua achando toda conta que um
  backup restaurado trouxer de volta. O que perde a rede são códigos e linhas
  da lista de espera de endereços apagados — de vida curta. **Nunca
  rotacionar no meio de uma restauração de backup.**

Isso é certo numa rotação de rotina. Numa rotação por **vazamento** não basta:
com a chave, alguém pode ter assinado um access token de qualquer conta e,
dentro dele, feito o que a pessoa faria. Por isso o passo 4.

1. **Gerar** no próprio computador:
   `python -c "import secrets; print(secrets.token_urlsafe(48))"`. Guardar no
   gerenciador de senhas. Não colar em lugar nenhum além do passo 2.
2. **Trocar**: Railway → serviço do backend → *Variables* → `SECRET_KEY` →
   novo valor → aplicar as mudanças e deixar o deploy terminar. A API recusa
   subir com chave curta ou de exemplo, então um deploy que não sobe é o
   primeiro sinal de valor colado errado.
3. **Conferir**: entrar na própria conta pelo app e pelo site. Os códigos de
   cadastro e de troca de e-mail enviados antes da troca não servem mais — a
   pessoa pede outro código, e é isso que ela verá.
4. **Só em vazamento — todo mundo faz login de novo.** Railway → serviço
   *Postgres* → aba de dados/consulta *(conferir o nome)*, ou `psql` pelo
   *Connect*:

   ```sql
   UPDATE refresh_tokens
      SET revoked_at = now(), revoke_reason = 'reset'
    WHERE revoked_at IS NULL;
   ```

   `reset` é o motivo que o próprio servidor usa quando uma senha é redefinida
   (`revoke_all`). A consulta roda do lado do Felipe; o que volta para uma
   sessão de IA, se houver, é só a contagem de linhas
   (`docs/incident-response.md` §1).
5. **Conferir no celular** que o app, com o refresh recusado, diz *"Entra na
   sua conta"* em vez de uma tela vazia ou um erro qualquer — é a classe de
   defeito "o app dizendo algo falso sobre o próprio estado".
6. **Registrar**: decision log (data, motivo, sem valor); se foi vazamento,
   também `docs/incident-register.md` e o runbook de
   `docs/incident-response.md`.

## 10. Revisão trimestral de acesso

Toda virada de trimestre (próximas: **27/12/2026**, **27/03/2027** — junto da
revisão semestral do RIPD —, 27/06/2027, 27/09/2027, esta última com a
rotação anual do §9). Uma linha por sistema; "ainda precisa?" = não é remoção
no mesmo dia.

| Data | Sistema | Quem tem acesso (nome, papel) | Ainda precisa? | 2FA de todos conferido | Tokens/chaves: algum sem prazo ou vencendo? | Apps/integrações revistos | Revisado por |
|---|---|---|---|---|---|---|---|
| | GitHub | | | | | | |
| | Railway | | | | | | |
| | Vercel | | | | | | |
| | Resend | | | | | | |
| | Sentry | | | | | | |
| | Google Play Console | | | | | | |
| | DNS (registrador / Cloudflare) | | | | | | |
| | oi@tumtum.cc (Google Workspace) | | | | | | |
| | Portais Snap / TikTok / Meta | | | | | | |
| | API-Football / setlist.fm | | | | | | |
| | Admin da TumTum (`ADMIN_EMAILS`, `WAITLIST_ADMIN_EMAILS`) | | | | | | |
