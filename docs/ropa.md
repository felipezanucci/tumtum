# Registro das operações de tratamento (ROPA)

Art. 37 da LGPD. **Versão de 26/09/2026 (noite)**, reescrita a partir do
parecer v1.1 (§3, matriz de bases por finalidade, e §21, campos do registro).
A primeira versão, da mesma manhã, nasceu da remediação da auditoria
(`RELATORIO-AUDITORIA-LGPD.md`, item L1) e punha quase tudo sob consentimento.
A v1.1 corrige isso: **cada operação tem a sua base**, e o consentimento fica
só onde ele é de fato a base.

**Cada linha é uma operação, não uma categoria de dado.** Uma operação nova, um
operador novo, um prazo novo ou uma base nova é uma linha nova ou alterada
aqui, e uma entrada no `docs/decision-log.md`, antes de ir ao ar.

- **Controlador:** [PENDENTE — Felipe: razão social e CNPJ]
- **Encarregado:** Felipe Zanucci, oi@tumtum.cc, assunto "Privacidade" —
  **provisório e em conflito** (`docs/dpo.md` §Conflito de interesse; item 80
  do decision log)
- **Owner de todas as linhas:** Felipe (produto e privacidade), até existir
  encarregado independente. A coluna existe para quando isso mudar.
- **RIPD:** `docs/ripd.md`, rascunho de 26/09 (noite). A coluna RIPD aponta os
  cenários de dano de lá (D1–D8, §6).
- **Países:** todos `[PENDENTE]` até o Transfer Register de
  `docs/dpa-checklist.md` estar cheio.
- **Dado sensível:** batimento cardíaco é dado de saúde (art. 5º, II), e os
  derivados (momento, curva, pico, qualidade) seguem o mesmo padrão enquanto
  ligados à pessoa (parecer §2.1).

**Como o ledger grava a base** (tabela `consents`, 26/09 noite): cada linha
leva `legal_basis` — `consent_art11` para `read_heart_rate`, `keep_night`,
`crowd_stats`, `artist_compare` e `improve_detection`; `consent_art7` para
`marketing`; `contract_art7` para `terms` —, `scope` (o que a finalidade
cobre) e `proof` (sha256 de `finalidade:versão_do_texto`), além da versão do
texto (`CONSENT_TEXT_VERSION`), quando foi dado, quando foi revogado, o meio e
o cliente.

## 1. As operações

Duas tabelas com o mesmo número de linha: a primeira diz o quê e por quê; a
segunda, onde, por quanto tempo e como. Juntas, uma linha por operação com os
campos do parecer §21.

### 1A. O quê e com que base

| # | Processo / feature | Finalidade | Titulares | Dados | Sensibilidade | Origem | Base legal |
|---|---|---|---|---|---|---|---|
| O1 | **Criar conta** e aceitar os Termos (`terms`) | Abrir a conta que guarda as noites da pessoa | Pessoas 18+ que se cadastram | Nome, e-mail, senha (hash bcrypt), data de nascimento; aceite dos Termos no ledger | Pessoal comum | A pessoa (app, site) | Execução de contrato / procedimentos preliminares, **art. 7º, V** (`contract_art7`). **VALIDAR** o texto contratual (parecer §3) e se basta um sinal 18+ em vez da data inteira (§8) |
| O2 | **Código de cadastro** por e-mail (`signup_codes`) | Provar que o e-mail é da pessoa antes de a conta existir | Quem inicia o cadastro | E-mail, nome, hash da senha, código de 6 dígitos (hash com chave), tentativas | Pessoal comum | A pessoa; código gerado pela TumTum | Procedimentos preliminares a contrato, **art. 7º, V** |
| O3 | **Entrar e manter a sessão** (login; access token 1 h; refresh token 90 d, rotacionado) | Manter a pessoa conectada e impedir que outro use a conta | Usuários | Tokens (refresh só como hash, com família, pai, motivo de revogação), horários | Pessoal; **dado de autenticação** (critério de risco da Res. CD/ANPD 15/2024) | Gerado pela TumTum | Prevenção à fraude e segurança do titular na autenticação, **art. 11, II, g** — **VALIDAR**: o token não é dado de saúde; a hipótese do art. 11 foi escolhida porque a sessão é a porta para ele; o advogado diz se art. 7º, V basta |
| O4 | **Trocar e-mail** (`email_changes`) | Mudar o endereço da conta provando o novo | Usuários | Novo e-mail, código (hash), tentativas | Pessoal comum | A pessoa | Execução de contrato, **art. 7º, V** |
| O5 | **Recuperar senha** (`password_reset_tokens`) | Devolver o acesso a quem esqueceu a senha | Usuários | E-mail, token (hash) | Pessoal comum | A pessoa | Execução de contrato, **art. 7º, V** |
| O6 | **Ler o batimento e gerar o momento**, no celular (`read_heart_rate`) | Encontrar quando o coração da pessoa subiu em relação a ela mesma na noite, e mostrar a curva | Usuários 18+ que autorizaram | bpm com data e hora na janela do evento ±30 min (relógio) ou do "Começar" ao fim (cinta); R-R e movimento do celular (só no aparelho, para o filtro "fora da pele"); no setup, os últimos 60 min, uma vez | **Saúde** (art. 5º, II); derivados com o mesmo padrão | Relógio via Health Connect; cinta BLE | Consentimento específico e destacado, **art. 11, I** (`consent_art11`). Sem ele a função não opera |
| O7 | **Guardar a noite** na coleção (`keep_night`), incluindo importar arquivo Polar pelo site | Guardar a noite e os momentos na conta, para rever, gerar card e postar | Usuários que autorizaram e tocaram "Guardar minha noite na TumTum" | `hr_sessions` (evento, início, fim, média/máx/mín, qualidade, fonte), `hr_data` (bpm por instante), `peaks`, `cards` | **Saúde** | O app, por toque; arquivo Polar enviado pela pessoa | Consentimento, **art. 11, I**, **opt-in separado** do O6 (`keep_night`). Sem ele, 403 `consent_required` |
| O8 | **Publicar e compartilhar um card** | Deixar a pessoa mostrar um momento fora da TumTum | Usuários | Card (pico, hora, evento — cada um com interruptor na prévia), imagem, `published_at`, `shares` (rede, horário). O nome é resolvido na leitura, não copiado no card | **Derivado de saúde**, tornado público pela pessoa | A pessoa | Consentimento, **art. 11, I**, expresso pelo ato de compartilhar depois da prévia. **VALIDAR**: o toque não grava linha no ledger; a prova é `published_at` + `shares` |
| O9 | **A galera** — estatística coletiva (`crowd_stats`) | Mostrar quantas pessoas sentiram o mesmo momento no evento | Usuários que autorizaram, com noite guardada no evento | Momentos agregados por evento | **Saúde**, agregada | Noites do O7 | Consentimento, **art. 11, I**, **mais anonimização real antes de qualquer saída**. **Bloqueada até o teste formal** de anonimização (parecer §15.1, gate P2): N mínimo é um controle, não prova de anonimato |
| O10 | **Na mesma vibe** — comparar com artista/atleta (`artist_compare`) | — | — | — (não existe) | Saúde de dois titulares | — | **Fora do MVP.** Consentimento dos dois titulares e contratos específicos (gate P3). O consentimento do fã é colhido; nada é tratado |
| O11 | **Melhorar o detector** (`improve_detection`) | Usar noites reais para ajustar o detector | Usuários que autorizaram | Noites com o consentimento ativo | **Saúde** | Noites do O7 | Consentimento, **art. 11, I**. **Nenhum processo usa hoje**; desenvolvimento e testes só com dados sintéticos (P0.12) |
| O12 | **Comunicações por e-mail** (`marketing`) | Contar novidades da TumTum | Usuários que autorizaram | E-mail, nome | Pessoal comum | A pessoa | Consentimento, **art. 7º, I** (`consent_art7`). Usar o card de alguém em peça de marketing exigiria outro consentimento e licença — não é feito |
| O13 | **Feed do evento**: post, SENTI, turnê | Publicar um momento para quem esteve no mesmo evento | Usuários com noite medida no evento | `event_posts` (bpm, hora do momento, rótulo, frase, skin), `event_post_reactions`, `series_posts` | **Derivado de saúde**, publicado a um público restrito | A pessoa | Consentimento, **art. 11, I, por post**: cada post é um ato; levá-lo às outras datas da turnê é outro ato (`series_posts`). Mesma **VALIDAR** do O8 (a prova é a linha do post) |
| O14 | **Denúncias e bloqueios** | Aplicar as regras de convivência e defender a comunidade | Quem denuncia, quem é denunciado, quem bloqueia | `post_reports` (motivo, resolução), `user_blocks` | Comum; a denúncia pode apontar um post com bpm | A pessoa | Execução de contrato (**art. 7º, V**, regras dos Termos) e exercício regular de direitos (**art. 7º, VI**; **art. 11, II, d** quando toca post com bpm) — **VALIDAR** |
| O15 | **Lista de espera** do site | Avisar quando a TumTum abrir | Visitantes do site (idade não verificada) | E-mail, nome e sobrenome (opcionais), página de origem | Pessoal comum | A pessoa | Consentimento, **art. 7º, I** |
| O16 | **Registro de acesso a aplicação** (`access_log`) | Guarda obrigatória de registros de acesso | Todo mundo que chama a API | IP, método, rota, status, horário, id da conta | Pessoal | Gerado pela API | Cumprimento de obrigação legal, **art. 7º, II** (Marco Civil, art. 15) |
| O17 | **Registro de quem leu dado de saúde** (`data_access_log`) | Saber quem leu a noite de quem, detectar abuso e responder a incidente | Titulares cujos dados foram lidos; quem leu | Ator, titular, recurso, ação, IP, horário. **Sem bpm** | Pessoal de segurança | Gerado pela API | Prevenção à fraude e segurança do titular, **art. 11, II, g** — **VALIDAR** (a leitura anterior, obrigação legal do art. 7º, II c/c arts. 37 e 46, fica como alternativa para o advogado) |
| O18 | **Registro de consentimento** (`consents`, o ledger) | Provar cada consentimento, com qual texto, e quando foi revogado (art. 8º, §2º: a prova é do controlador) | Usuários | Conta, finalidade, `legal_basis`, versão do texto, `scope`, `proof`, dado em, revogado em, meio, cliente. **Sem dado de saúde** | Pessoal de compliance | Gerado pela API a cada toque | Cumprimento de obrigação legal, **art. 7º, II** (art. 8º, §2º) — **VALIDAR** |
| O19 | **Pedidos de titular** (`data_subject_requests`) | Responder aos direitos do art. 18 | Titulares que pedem | Tipo, mensagem, status, prazos, resposta | Pessoal; pode conter o que a pessoa escreve | A pessoa | Cumprimento de obrigação legal, **art. 7º, II** (arts. 18 e 19) |
| O20 | **Suporte por e-mail** (caixa oi@tumtum.cc) | Responder dúvidas e problemas | Quem escreve | E-mails e anexos | Pessoal; **pode conter saúde** que a pessoa escreva. A TumTum nunca pede série bruta | A pessoa | Execução de contrato, **art. 7º, V**; obrigação legal (art. 7º, II) quando é pedido de titular |
| O21 | **Excluir a conta** e provar a exclusão (`deletion_log`) | Apagar tudo da pessoa e provar que exclusões acontecem | Quem pede | Tudo da conta, na exclusão; no `deletion_log` só a data | O `deletion_log` **não é dado pessoal** | A pessoa | Cumprimento de obrigação legal, **art. 7º, II** (arts. 16 e 18, VI) |
| O22 | **Monitoramento de erros** (Sentry) | Achar e corrigir falhas técnicas | Quem estava usando a API quando o erro aconteceu | Stack trace, rota, ambiente. **Sem** corpo de requisição, variáveis locais, cookies, `Authorization`, PII (`send_default_pii=False`, `before_send`) | Técnico; **nenhum sensível por configuração** | Gerado pela API | Legítimo interesse, **art. 7º, IX**, só para erro técnico sem dado sensível — possível **só** porque corpos e locais estão desligados. **LIA a documentar** [PENDENTE] |
| O23 | **Registro de incidentes** (`docs/incident-register.md` + pasta fora do repositório) | Registrar todo incidente, comunicado ou não, com a justificativa da avaliação | Titulares afetados (só em número e categoria) | Descrição, categorias e número de afetados, avaliação, comunicações, causa, correção | Pessoal de segurança | A TumTum | Cumprimento de obrigação regulatória, **art. 7º, II** (Res. CD/ANPD 15/2024) |
| O24 | **Piloto**: termo em papel, planilha de controle, ZIPs de operador | Rodar o piloto com pessoas reais | Participantes 18+ | Nome, data de nascimento, escolhas do termo; série crua de uma noite (ZIP) | **Saúde** (ZIP) | Participante; celular de operador | Consentimento, **art. 11, I**, por finalidade no termo (`docs/pilot-consent-template.md`) |

### 1B. Onde, por quanto tempo, como

| # | Sistemas | Destinatários (operadores) | Países | Retenção | Segurança | Direitos | Owner | RIPD |
|---|---|---|---|---|---|---|---|---|
| O1 | App / site → API → Postgres (`users`) | Railway; Vercel (site) | [PENDENTE] | Enquanto a conta existir | bcrypt; TLS; 18+ recusado no servidor | Acesso, correção, exportação, exclusão | Felipe | D1, D7 |
| O2 | API → `signup_codes`; e-mail pelo Resend | Railway, Resend | [PENDENTE] | Não confirmado: 1 dia. Usado ou expirado: 24 h | Código só como hash com chave; limite de tentativas | Exclusão (dura 1 dia) | Felipe | D1 |
| O3 | API → `refresh_tokens`; app (tokens criptografados); navegador: **refresh token em cookie `httpOnly`** desde 26/09 (noite), fora do alcance de script da página | Railway, Vercel | [PENDENTE] | 90 dias do último uso; expirados saem 24 h depois | Rotação a cada uso; reuso revoga a família; hash; a API não sobe com `SECRET_KEY` fraca | Sair; exclusão | Felipe | D1, D6 |
| O4 | API → `email_changes`; código pelo Resend ao novo endereço | Railway, Resend | [PENDENTE] | 24 h depois de expirar | Código só como hash | Correção | Felipe | D1 |
| O5 | API → `password_reset_tokens`; e-mail pelo Resend | Railway, Resend | [PENDENTE] | 24 h depois de expirar | Token só como hash | — | Felipe | D1 |
| O6 | Sensor → app (Room com SQLCipher) → detector no aparelho. **Nada sobe nesta operação** | Nenhum | Não sai do celular | Log bruto da captura (R-R, movimento, amostras BLE): apagado quando a noite é guardada. A noite no aparelho: até a pessoa apagar a noite ou a conta | SQLCipher (chave no `EncryptedSharedPreferences`); `allowBackup="false"`; notificação sem bpm e, **desde 26/09 (noite), sem o nome do evento** — *"Sua noite abriu"* | Apagar a noite; revogar em Configurações → Privacidade | Felipe | D1, D2 |
| O7 | App / site → API → Postgres (TimescaleDB) → detector no servidor → card gerado na requisição → Redis | Railway; Vercel (importação pelo site) | [PENDENTE] | **Série bruta (`hr_data`): 7 dias** após a análise (`analyzed_at`) — `RAW_READINGS_RETENTION_DAYS`. Momentos, resumo e cards: enquanto a conta existir e `keep_night` estiver ativo — **revogado, saem 24 h depois**. Imagem do card no Redis: 7 dias | TLS; dono checado em toda leitura; `data_access_log`; R-R e movimento recusados pelo servidor | Ver, exportar (JSON e CSV), apagar a noite, revogar | Felipe | D1, D2, D6 |
| O8 | App / site → API → Redis (imagem) → página pública em tumtum.cc com `noindex`; a rede que a pessoa escolhe | Railway, Vercel. A rede social recebe por ato da pessoa: não é operador da TumTum | [PENDENTE] | Público enquanto publicado; despublicar tira do ar. Imagem: 7 dias. Arquivo temporário no celular: 1 h | 404 sem `published_at`; URL por UUID, não sequencial; vídeo sem metadados (GPS); prévia diz o que o card mostra | Despublicar; apagar o card | Felipe | D5, D8 |
| O9 | Calculado na leitura, na API; nada guardado à parte | Railway | [PENDENTE] | Nada guardado à parte | ≥ 100 noites no evento, ≥ 10 pessoas por momento, faixas (`10+`…`250+`), nunca contagem exata; visível só a quem tem noite no evento; **nenhuma saída B2B** | Revogar `crowd_stats` tira a noite das próximas leituras | Felipe | D4 |
| O10 | — | — | — | — | — | — | Felipe | — |
| O11 | — (nenhum processo) | — | — | Quando existir: só noites com o consentimento ativo, e esta linha é revista antes | Dados sintéticos em desenvolvimento, testes e demos | Revogar | Felipe | D2 |
| O12 | API → Resend | Railway, Resend | [PENDENTE] | Até revogar | — | Revogar em Configurações | Felipe | — |
| O13 | App / site → API → Postgres | Railway, Vercel | [PENDENTE] | Até a pessoa apagar o post ou a conta. **Post apagado fica com `deleted_at`** [PENDENTE — remoção física, item 78] | Visível só a quem tem noite medida no evento; denúncia e bloqueio | Apagar o post; exclusão | Felipe | D4, D5, D8 |
| O14 | App / site → API → Postgres; admin | Railway | [PENDENTE] | Enquanto a conta de quem denunciou/bloqueou existir | Leitura só por admin, registrada | Acesso; exclusão | Felipe | D6, D8 |
| O15 | Site → API → Postgres (`waitlist_entries`) | Vercel, Railway | [PENDENTE] | Até a pessoa pedir para sair; sai também com a conta do mesmo e-mail | — | Sair da lista (oi@tumtum.cc) | Felipe | — |
| O16 | API → `access_log` | Railway | [PENDENTE] | **180 dias**. Não sai com a conta | Acesso restrito | Acesso (limitado ao que a lei permite) | Felipe | D6 |
| O17 | API → `data_access_log` | Railway | [PENDENTE] | Enquanto a conta do titular existir [PENDENTE — prazo máximo, item 78] | Sem bpm; acesso restrito | Acesso | Felipe | D6 |
| O18 | API → `consents` (mesmo banco hoje) | Railway | [PENDENTE] | **Hoje sai com a conta.** Alvo: prazo probatório, separado do dado de saúde [PENDENTE — jurídico, item 82] | Só acrescenta; `proof` por hash da versão do texto | Ver e revogar cada finalidade em Configurações → Privacidade | Felipe | — |
| O19 | Site → API → `data_subject_requests`; admin | Railway | [PENDENTE] | Enquanto a conta existir | Leitura do admin registrada; resposta só ao e-mail da conta | O próprio canal | Felipe | D6 |
| O20 | Caixa de e-mail | [PENDENTE — provedor da caixa] | [PENDENTE] | [PENDENTE — Felipe: prazo] | Nunca pedir série bruta nem print com bpm e identidade | Acesso; exclusão | Felipe | D6 |
| O21 | API → todas as tabelas, Redis, lista de espera; `deletion_log` | Railway | [PENDENTE] | `deletion_log`: indefinido. **Backups: [PENDENTE — prazo do Railway]** e sem tombstone (P1, `docs/data-retention-policy.md`) | Senha pedida para excluir | — | Felipe | D1 |
| O22 | API → Sentry, se `SENTRY_DSN` estiver definido | Sentry | [PENDENTE — US ou EU] | [PENDENTE — conferir no Sentry] | Sem PII, sem corpo, sem locais, `before_send` | — | Felipe | D1 |
| O23 | `docs/incident-register.md` (sem dado pessoal) + pasta fora do repositório | — | — | **No mínimo 5 anos** | Repositório público: nada que identifique pessoa nem detalhe explorável antes da correção | — | Felipe | todos |
| O24 | Papel; planilha; celular de operador → ZIP | [PENDENTE — onde os ZIPs ficam] | [PENDENTE] | Dados do piloto: 30 dias após o evento (`docs/pilot-data-retention.md`). Termo: 5 anos após o descarte. **A série crua dos ZIPs fica 30 dias contra 7 no servidor** [PENDENTE — decisão] | Export sem MAC do sensor nem identificador no nome do arquivo | Os do termo, §7 | Felipe | D1, D6 |

## 2. Operadores

Nenhum dado individual vai para parceiro comercial — clube, artista,
organizador, patrocinador, anunciante. **Fornecedores contratados processam
dados em nosso nome**, só para operar e proteger o serviço, sob instrução.
O mapa completo, com o papel de cada um, a região, o mecanismo de
transferência e as perguntas do parecer §14, está no Transfer Register de
`docs/dpa-checklist.md`; esta tabela é o resumo.

| Operador | O que faz | Operações | Região | Contrato (DPA) |
|---|---|---|---|---|
| **Railway** | API (FastAPI), Postgres, Redis | O1–O5, O7–O9, O12–O19, O21 | [PENDENTE] | [PENDENTE] |
| **Vercel** | Site tumtum.cc e web app | O1, O3, O7, O8, O13, O15 | [PENDENTE] | [PENDENTE] |
| **Resend** | E-mails de código, senha, troca de e-mail, marketing | O2, O4, O5, O12 | [PENDENTE] | [PENDENTE] |
| **Sentry** | Erros do backend | O22 | [PENDENTE] | [PENDENTE] |
| Provedor da caixa oi@tumtum.cc | Suporte, pedidos | O19, O20 | [PENDENTE] | [PENDENTE] |
| Cloudflare | DNS; CDN/proxy se ligado | Todas as que passam pelo site, se o proxy estiver ligado | [PENDENTE] | [PENDENTE] |

**Não são operadores de dado pessoal** (conferido no código, 26/09):
API-Football (a TumTum pergunta pelo jogo, nada sobre a pessoa); Google
Health Connect (roda no celular da pessoa, a TumTum lê dele e nada volta ao
Google por nós); Google Play (distribui o app); GitHub (hospeda o código, que
não carrega dado de titular — o job `privacy-scan` garante); as sessões do
Claude Code que trabalham no repositório (**só enquanto não recebem dado real**:
a regra é nunca colar linha de produção, ver `docs/incident-response.md`).
Nenhum SDK de anúncio ou de analytics existe em nenhum dos três códigos.

## 3. Medidas de segurança

- **Em trânsito:** HTTPS em produção (app, site, API). Conexão da API ao banco
  com TLS quando `DATABASE_SSL=true` ([PENDENTE — Felipe: confirmar no
  Railway]).
- **Chave de assinatura dos tokens:** a API não sobe com `SECRET_KEY` de
  exemplo ou com menos de 32 caracteres. Access token de 1 h; refresh token de
  90 dias, rotacionado, com detecção de reuso (a família inteira é revogada).
  **No site, o refresh token vive em cookie `httpOnly`** (26/09 noite), não em
  `localStorage`.
- **Senhas e códigos** só como hash.
- **Controle de acesso:** toda leitura de noite, momento e card checa o dono;
  card público só depois de compartilhado (`published_at`), com `noindex`;
  `/api/demo` não existe em produção.
- **Registro:** `data_access_log` para cada leitura de dado de saúde e
  exportação; `access_log` de toda requisição, 180 dias; ledger de
  consentimento com base legal, escopo e prova.
- **Minimização:** R-R e movimento nunca sobem; **série bruta apagada em 7
  dias** após a análise; o nome do titular não é copiado dentro do card; export
  de operador sem MAC do sensor e sem identificador no nome do arquivo.
- **No celular:** banco Room criptografado (SQLCipher, chave no
  `EncryptedSharedPreferences`), tokens criptografados, `allowBackup="false"`,
  arquivos temporários de card apagados após o compartilhamento e na exclusão
  da conta, vídeo sem metadados (GPS) antes de ir para um Story, **notificação
  neutra: sem bpm e sem o nome do evento**.
- **Monitoramento de erros** sem corpo de requisição nem PII.
- **Repositório público** sem dado pessoal nem segredo (`privacy-scan` no CI).
- **Resposta a incidente:** `docs/incident-response.md`; registro em
  `docs/incident-register.md`.
- **O que ainda não existe** (P1, antes do lançamento público): MFA nos painéis
  de produção, cofre de segredos, pentest, identidade em schema separado,
  tombstone de backup. Ver `docs/ripd.md` §7.

## 4. Pendências deste registro

- [PENDENTE — Felipe] controlador (razão social, CNPJ).
- [PENDENTE — região de cada operador] e o mecanismo de transferência
  internacional quando for fora do Brasil (Transfer Register,
  `docs/dpa-checklist.md`).
- [PENDENTE — Felipe] DPAs assinados/baixados.
- [PENDENTE — jurídico] as bases marcadas **VALIDAR**: O1 (texto contratual e
  data de nascimento), O3 e O17 (art. 11, II, g), O8 e O13 (o ato de publicar
  como prova do consentimento), O14, O18.
- [PENDENTE — Felipe] **LIA** do Sentry (O22): finalidade, necessidade,
  balanceamento, salvaguardas. Sem ela, desligar o `SENTRY_DSN`.
- [PENDENTE — jurídico] prazo probatório do ledger de consentimento (O18,
  item 82).
- ~~[PENDENTE — decisão] revogar `keep_night` apaga as noites já guardadas?~~
  **Respondido pelo código de 26/09** (`services/maintenance.py`): sim — 24 h
  depois da revogação sem nova concessão (`REVOCATION_GRACE`, a folga para um
  toque por engano), as noites saem com tudo o que foi feito delas. Contas
  anteriores a 26/09, que nunca responderam, não são tocadas. As telas e o
  termo do piloto precisam dizer isso (conferido 26/09 noite: o termo dizia o
  contrário e foi corrigido).
- [PENDENTE — decisão] post do feed apagado continua na tabela com
  `deleted_at` (bpm incluso). Definir remoção física (sugestão: no ciclo de
  manutenção, 30 dias depois).
- [PENDENTE — decisão] prazo máximo do `data_access_log` enquanto a conta
  existe (sugestão: 180 dias, como o `access_log`).
- [PENDENTE — decisão] noite **nunca guardada**: o log bruto da captura (R-R,
  movimento) fica no celular até a pessoa apagar a noite. Alinhar aos 7 dias do
  servidor?
- [PENDENTE — decisão] série crua nos ZIPs de operador do piloto: 30 dias
  contra 7 no servidor (O24).
