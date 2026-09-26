# Relatório de auditoria de conformidade — TumTum × parecer jurídico (LGPD e dados de batimento cardíaco)

Versão 1.0 — 26 de setembro de 2026. Auditoria técnica, somente leitura, sobre o repositório `felipezanucci/tumtum` no commit `3c80873` (branch `claude/tumtum-lgpd-audit-15wo2g`). Nenhum arquivo de código foi alterado; nenhum dado real de participante foi aberto, copiado ou impresso. Onde o texto cita um arquivo de dados, cita só o caminho.

Escopo auditado: `android/` (`cc.tumtum.app`, o app atual), `android-capture/` (`cc.tumtum.capture`, o app de referência ainda publicado), `backend/` (FastAPI), `frontend/` (site tumtum.cc e web app), `.github/workflows`, `docker-compose.yml`, `docs/`, `README.md`, `CLAUDE.md`. O diretório `hardware/jstyle-spike` é o SDK de fornecedor de um spike de hardware e foi olhado só para manifesto e segredos.

Convenção: caminhos são relativos à raiz do repositório; `arquivo:linha` aponta a evidência. Quando a evidência é uma busca vazia, o comando está registrado.

---

## 1. Resumo executivo

| Status | Itens |
|---|---|
| ATENDE | 16 |
| PARCIAL | 31 |
| NÃO ATENDE | 26 |
| NÃO VERIFICÁVEL | 1 |
| NÃO SE APLICA | 2 |
| **Total** | **76** |

A TumTum tem uma base técnica melhor do que a média para um MVP: a detecção de momentos é puramente relativa ao próprio usuário (F1), não há SDK de anúncios nem de analytics ativo (H1–H3), não há IA externa (F5), o compartilhamento é sempre por intent do sistema iniciado pelo usuário (G3), os endpoints de sessão checam propriedade (C5) e a exclusão de conta existe no app e apaga todas as tabelas com chave para o usuário (D3/D4). A política de privacidade e os termos existem, em dois idiomas, e dizem "não é saúde" (J3).

O que falta é a camada jurídica que o parecer exige, e ela falta inteira: **não existe consentimento específico por finalidade nem registro dele em lugar nenhum** (A2/A3), **não existe verificação de idade** (I1/I2), **não há termo de consentimento nem plano de descarte para o piloto** (A8/D6), e **o código faz três coisas que a própria política promete não fazer** (lê 24 h do relógio fora de evento, sobe a noite ao servidor sem ato do usuário, e faz backup automático do banco local no Google).

**Achados CRÍTICOS (bloqueiam o piloto):**

1. **Sem consentimento por finalidade e sem registro de consentimento** — A2, A3. O único "consentimento" é o diálogo de permissão do Health Connect; nada é gravado no banco (`backend/alembic/versions/001…013`, nenhuma tabela de consentimento).
2. **Upload automático da noite, contra a promessa escrita do app** — A2(b). `android/.../data/repo/EndNightFlow.kt:35` sobe a noite ao terminar; `strings.xml:20` diz "Nada deixa seu aparelho sem você mandar".
3. **Sem termo de consentimento do piloto e sem plano de descarte dos dados do piloto** — A8, D6.
4. **Sem verificação de idade de nenhum tipo** — I1, I2. Não há data de nascimento, checkbox, nem aceite de termos no cadastro (`backend/app/schemas/auth.py:7-13`).
5. **`SECRET_KEY` com valor padrão conhecido e sem guarda de inicialização** — C4. `backend/app/config.py:11`. Se a variável não estiver no Railway, qualquer pessoa forja um token e lê qualquer conta. Não verificável no repositório.
6. **Dados reais do fundador em fixtures de teste, e e-mails de terceiros em docs** — C8. `frontend/lib/health/import-parsers.polar.test.ts:5-11` (nome completo + amostras de um export Polar), `android/app/src/test/.../BeatFilterTest.kt:15-21` (68 leituras reais da "night 18"), `docs/pilot-event-options.md:379-382` (contatos de fã-clubes). Crítico pela regra do parecer; a gravidade prática é menor porque o dado cardíaco é do próprio fundador.

---

## 2. Tabela geral

| Item | Status | Gravidade | Evidência | Observação |
|---|---|---|---|---|
| A1 | PARCIAL | ALTO | `android/.../ui/screens/permission/PermissionScreen.kt:50-143`; `strings.xml:54-63` | Tela existe e é separada, mas o consentimento é só o diálogo do Health Connect; não existe aceite de termos em lugar nenhum (site: `frontend/app/(auth)/signup/page.tsx` sem link nem checkbox) |
| A2 | NÃO ATENDE | CRÍTICO | (a) `PermissionScreen.kt:124`; (b) `EndNightFlow.kt:34-35`, `TumTumApp.kt:62-63`; (c) `backend/app/api/feed.py:425-470`; (d) `card_generator.py:559-571`; (e)/(f) grep sem resultado | Só (a) existe, via SO. (b) é automático. (c) roda sobre todos os presentes sem opt-in. (d)(e)(f) não existem |
| A3 | NÃO ATENDE | CRÍTICO | `grep -rn -i "consent\|terms_accepted\|accepted_at\|privacy_version" backend/app backend/alembic android/app/src/main` → só docstrings | Nenhuma tabela, coluna ou preferência registra consentimento |
| A4 | ATENDE | — | `grep -rn "mutableStateOf(true)\|Switch(\|Checkbox(" android/` → 0; frontend `useState(true)` só em flags de loading | Vazio por ausência: não há toggles de consentimento |
| A5 | PARCIAL | ALTO | `SettingsScreen.kt:103-120` (abre Health Connect); `feed.py:346-378` (apaga post); `privacy-copy.ts:90` | Revogar HC bloqueia nova captura mas não apaga o que já subiu nem tira da estatística coletiva; não há apagar-uma-noite (item 35 do decision log) |
| A6 | PARCIAL | ALTO | `strings.xml:54-63,476`; `SetupScreen.kt:348-351` | Diz "só nas janelas dos eventos" e o setup lê 24 h; não diz que é dado de saúde, não diz prazo, não cita clubes/artistas, não linka a política |
| A7 | ATENDE | — | `PermissionScreen.kt:132-141` ("Agora não"); `strings.xml:436` | Nada principal depende de consentimento opcional; ressalva: o upload não é opcional |
| A8 | NÃO ATENDE | CRÍTICO | `grep -rn -i "termo de consentimento\|TCLE\|consent form" docs/ README.md` → 0; `docs/pilot-event-options.md:206-207` só anota "consent, in writing" | Nenhum termo, modelo ou registro |
| B1 | PARCIAL | ALTO | `HealthConnectSource.kt:56-81`; `NightRepository.kt:79,179,192,219-222` (±30 min); **`SetupScreen.kt:349-351` (24 h)**; `frontend/app/(app)/import/page.tsx:79-85,111-114` (arquivo inteiro) | Captura de evento é limitada a ±30 min. Setup lê 24 h fora de evento; import web sobe o arquivo todo se nenhum evento for escolhido |
| B2 | ATENDE | — (spike: MÉDIO) | `android/app/src/main/AndroidManifest.xml:7-34`; `android-capture/.../AndroidManifest.xml:9-31`; spike `hardware/jstyle-spike/v8test/app/src/main/AndroidManifest.xml:7-32` | Apps: só `health.READ_HEART_RATE` + Bluetooth + foreground + notificação + wake lock + boot + bateria. Spike pede localização, câmera, BODY_SENSORS, storage; seu APK é publicado em release público |
| B3 | ATENDE | — | `BlePermissions.kt:15-28`; manifesto l.11-26 | Só BLUETOOTH_SCAN/CONNECT (≥31), FINE_LOCATION só até API 30; sem BODY_SENSORS |
| B4 | ATENDE | — | `grep -rn "READ_HEALTH_DATA_IN_BACKGROUND\|READ_HEALTH_DATA_HISTORY" .` → 0 | — |
| B5 | ATENDE | — | Só `AndroidManifest.xml:24-26` (maxSdk 30) e `BlePermissions.kt:19` | Localização só para scan BLE legado; sem GPS, sem lat/long em lugar nenhum |
| B6 | ATENDE | — | `schemas/auth.py:7-13`; `models/user.py:17-26`; `CreateAccountScreen.kt:85-89` | E-mail, nome, senha; avatar opcional (fica no celular); username/tribos só locais. **Não coleta data de nascimento** (ver I1) |
| B7 | PARCIAL | MÉDIO | `CaptureEntities.kt:23-42`; `CaptureService.kt:178-188,209-221`; `SessionPayload.kt:37`; `schemas/health.py:31-32`; `models/hr_data.py:21-22`; `SessionExporter.kt:36-60` | RR e acelerômetro gravados no celular e exportáveis pelo operador; o app não os envia, mas o servidor aceita e guarda. Sem finalidade declarada nem consentimento; `strings.xml:349` anuncia "com HRV" |
| C1 | NÃO ATENDE | ALTO | `models/hr_data.py:17-19` → `hr_session.py:44-46` → `user.py:17-20`; alembic `001:25-26,81,97` | FK direta; identidade e série no mesmo banco/schema; nome copiado em `cards.metadata` (`cards.py:167`). `grep -i "pseudonym\|anonym" backend/app` → 0 |
| C2 | NÃO VERIFICÁVEL | — | `grep -rn -i "encrypt\|pgcrypto\|sslmode\|kms" backend docker-compose.yml .github` → 0 | Conferir no Railway (seção 5) |
| C3 | PARCIAL | MÉDIO | `TumtumApi.kt:354` (https); `main.py:128`, `frontend/lib/api.ts:1`, `docker-compose.yml:56` (http só dev); `core/database.py:6` sem `ssl` | Produção é HTTPS; TLS app→DB não configurado no código |
| C4 | PARCIAL | CRÍTICO (condicional) / ALTO | `config.py:11` `secret_key="your-secret-key"`; `android/app/debug.keystore`, `android-capture/debug.keystore` + `build.gradle.kts:34-37`; `.github/workflows/build-app.yml:97,132-134` (secrets corretos) | Nenhum segredo de produção commitado. Risco: default do SECRET_KEY sem guarda; keystore de debug commitado assina APKs publicados em release público |
| C5 | PARCIAL | ALTO | Checagens ok: `health.py:175,189`; `experience.py:39,152`; `cards.py:39,201,213,309,335`. Falhas: `cards.py:223-299` (público), `demo.py:368-372` (qualquer usuário), `users.py:109-127` (público), `feed.py:62-92` (presença falsificável) | Nenhum IDOR direto na série. Cards públicos por UUID sem flag de publicação; `/demo/simulate` apaga sessões reais do chamador e forja presença |
| C6 | NÃO ATENDE | ALTO | `grep -rn -i "audit" backend/` → 0; `grep -n "logger\|logging" backend/app/api/{health,experience,cards}.py` → só `cards.py:190` | Nenhum log de quem leu o quê |
| C7 | PARCIAL | MÉDIO | Backend em memória (`card_generator.py:365-366,437-438`) + Redis 7 dias (`cards.py:184-186`). Android `cacheDir/cards` (`ShareTargets.kt:249-250,381-393`, `VideoCard.kt:106-108`, `CardRenderer.kt:416-417`), `cacheDir/exports` (`SessionExporter.kt:41-43`) | Arquivos de cache não são apagados após o share nem na exclusão de conta; `CardPhotoStore.deleteAll` nunca é chamado |
| C8 | NÃO ATENDE | CRÍTICO (regra) | `frontend/lib/health/import-parsers.polar.test.ts:5-11`; `android/app/src/test/.../BeatFilterTest.kt:15-21`; `docs/decision-log.md:170,1019,1107-1108,1797,5459`; `docs/handoff-2026-09-18.md:108`; `docs/pilot-event-options.md:379-382` | Dado cardíaco real do fundador em dois testes; e-mails pessoais do fundador, da conta de revisão do Play e de contatos de fã-clube em docs. Nenhum CSV/dump de participante |
| C9 | PARCIAL | MÉDIO | `scripts/simulate_moment_detection.py`; `demo.py:269-365`; `demo.py:368` sem flag de ambiente; `main.py:148` | Gerador sintético existe; `/simulate` aberto em produção |
| C10 | NÃO ATENDE | ALTO | `TumTumDatabase.kt:11-18`; `grep -rn "SQLCipher\|EncryptedSharedPreferences\|MasterKey" android android-capture` → 0; `UserPrefs.kt:16,192-194,247-250`; `Daos.kt:101` | Room sem criptografia com bpm/RR/movimento; tokens em DataStore em claro; leituras não apagadas após upload (só na exclusão de conta) |
| C11 | NÃO ATENDE | ALTO | `android/app/src/main/AndroidManifest.xml:56` `allowBackup="true"`, sem `fullBackupContent`/`dataExtractionRules`; `android-capture/...:47` `false` | Banco local, tokens e fotos vão para o backup do Google; `delete-account-copy.ts:56` diz "não há backup" |
| D1 | NÃO ATENDE | ALTO | `grep -rn -i "retention\|purge\|older_than\|timedelta(days" backend/app` → só tokens/códigos; sem beat/cron (`card_tasks.py:12-22`, `docker-compose.yml:58-60`) | Série bruta fica enquanto a conta existir (`privacy-copy.ts:83`) |
| D2 | NÃO ATENDE | ALTO | `models/peak.py:19`; `account_deletion.py:88`; sem DELETE em `api/health.py` | Momento só morre com a conta; sem consentimento de coleção ao qual se vincular |
| D3 | PARCIAL | MÉDIO | `users.py:100-106`; `SettingsScreen.kt:319-418`; `TumtumApi.kt:101-104`; web: `grep -n -i delete frontend/lib/api.ts` → sem `/users/me` | Existe no Android em 3 toques, sem reautenticação; web app não tem (`apagar-conta` é só texto) |
| D4 | PARCIAL | ALTO | Apaga: `account_deletion.py:66-97` (14 tabelas). Sobra: Redis `card:image:*` (`cards.py:184-186,262-270`), `signup_codes` (`auth.py:131-135`), `waitlist_entries`, avatar `filesDir` (`AvatarStore.kt:33`), `cacheDir/cards`, `cacheDir/exports`, DCIM | Imagem do card apagado continua pública até 7 dias, porque `/image` lê o Redis antes do banco |
| D5 | NÃO ATENDE | MÉDIO | `grep -rn -i backup docs/ README.md backend/` → nada sobre servidor; `delete-account-copy.ts:56` | Sem política de backup; backup Android ligado (C11) |
| D6 | NÃO ATENDE | CRÍTICO | `grep -rn -i "pilot\|piloto" docs/*.md | grep -i "apagar\|delet\|purge"` → 0 | Sem rotina nem plano de descarte do piloto |
| E1 | PARCIAL | MÉDIO | `users.py:56-70`; `health.py:168-203`; `experience.py:143`; `cards.py:195`; `users.py:136` | Acesso fragmentado; posts, reações, denúncias e shares não são consultáveis pelo titular |
| E2 | NÃO ATENDE | MÉDIO | `frontend/lib/utils/csv.ts:9-13` só na waitlist admin; `SessionExporter.kt:29-80` só operador (`UserPrefs.kt:145-151`) | Nenhuma exportação para o titular |
| E3 | PARCIAL | MÉDIO | `users.py:73-97`; `schemas/user.py:22-24` (só nome e avatar); `SettingsScreen.kt:160-190` → `UserPrefs.kt:364-366` (só local) | E-mail não corrigível; edição de nome no Android nunca chega ao servidor |
| E4 | NÃO ATENDE | ALTO | `grep -rn -i "encarregado\|DPO\|dpo@\|privacidade@" android frontend backend docs` → 0; só `oi@tumtum.cc` (`privacy-copy.ts:94-97`) | Sem encarregado, sem controlador nomeado, sem CNPJ (`terms-copy.ts:9-10`) |
| E5 | NÃO ATENDE | MÉDIO | `grep -rln -i "dsar\|titular\|subject_request\|audit_log"` → só docs não relacionados | Nenhum registro de pedidos |
| F1 | ATENDE | — | `peak_detection.py:44-51,87-115,170-207` (mediana 1200 s, IQR, z, +10 bpm); `event_correlator.py:48-93` (só tempo); `NightAnalyzer.kt:78-100`; `BeatFilter.kt:32-41` (30–250 validade) | Só variação relativa ao próprio usuário; limites absolutos são filtros de validade fisiológica |
| F2 | ATENDE | — | grep de normal/anormal/perigoso/alerta → só comentários e disclaimers; `CaptureService.kt:345-380` (notificação sem bpm); `Reminders.kt:68-82` (por horário) | Ressalva em J4: o app **antigo** mostra bpm ao vivo na notificação |
| F3 | PARCIAL | ALTO | `strings.xml:154` "EU TAVA TRANQUILO. AÍ VEIO ISSO." usado em `CardScreen.kt:183` (todo card Android); `strings.xml:615` "UMA NOITE TRANQUILA."; `frontend/app/(app)/experience/page.tsx:172` "Seus Picos de Emoção"; `[username]/page.tsx:123`, `manifest.json:4` "Compartilhe a emoção" | O backend removeu essa mesma frase por afirmar "baseline calmo que ninguém mediu" (`card_generator.py:108-115`); o Android ainda imprime |
| F4 | ATENDE | — | `HealthConnectSource.kt:39,63` só HeartRateRecord; grep de sono/calorias/VO2 → só rótulos de fonte e um cabeçalho de fixture | — |
| F5 | ATENDE | — | grep de openai/anthropic/tflite/onnx em backend, android, frontend → 0; `requirements.txt` sem SDK de IA | Nenhum modelo |
| F6 | ATENDE | — | `models/event_timeline.py:22-24`; `event_correlator.py:23-42,95-96`; `ServerEvents.kt:151-172` | Rótulos factuais (música, gol, apito); ressalva F3 é o título, não o rótulo |
| G1 | PARCIAL | MÉDIO | `CardScreen.kt:405-420` (preview); `strings.xml:171-172,316,570-592` (sem aviso); `strings.xml:276` (aviso existe só para o feed) | Preview sim, aviso do que o card revela não |
| G2 | NÃO ATENDE | MÉDIO | `grep -rn -i "toggle\|hide\|ocultar\|showBpm" android/.../ui/screens/card` → 0; `schemas/card.py:7-11`; `card_generator.py:306-362` | Só skin e foto; bpm, hora, evento e nome sempre impressos |
| G3 | ATENDE | — | `ShareTargets.kt:95-134,150-193,268-317`; `CardRenderer.kt:424-432`; `share.ts:42-134`; grep `graph.facebook\|open.tiktok` → 0 | Só intents/share sheet por toque; sem API de publicação; sem agendamento |
| G4 | PARCIAL | MÉDIO | `card_generator.py:366,438,555` (PNG sem pnginfo/EXIF); `ShareTargets.kt:350-358` (MediaStore: nome `tumtum-<nightId>-<ms>.png`, sem GPS); `ShareTargets.kt:380-388` (`copyVideo` byte a byte); `VideoCard.kt:97-130` (Media3 Transformer) | Nome completo impresso no card. Vídeo do usuário copiado com metadados originais (GPS possível); Transformer NÃO VERIFICÁVEL |
| G5 | NÃO ATENDE | ALTO | `frontend/app/(public)/cards/[id]/page.tsx:26-55` (OG com bpm e nome); `cards.py:223-247`; grep `noindex\|robots` frontend → 0 | Página pública desde a criação, sem consentimento próprio, sem expiração, sem noindex; revogação só apagando o card (e o Redis sobrevive 7 dias) |
| G6 | PARCIAL | ALTO | `cards.py:223-299` sem auth; `models/card.py:14-16` uuid4 | Segurança só pelo UUID; todo card é público, compartilhado ou não |
| G7 | PARCIAL | ALTO | `crowd.py:29` `MIN_CROWD=4`; `crowd.py:38,111` piso por célula `max(2, round(n*0.3))`; `feed.py:439-443` conta toda `hr_session` do evento | Parecer sugere N≥100; código publica células de 2 pessoas; sem opt-in; métrica é contagem de pessoas por balde de 2 min |
| G8 | PARCIAL | ALTO | Posts: `feed.py:286-343`, `schemas/feed.py:22-99` (nome, bpm, hora exata — consentido). Agregado: `schemas/feed.py:163-195` | Payload individual só de quem postou; agregado inclui quem nunca consentiu |
| G9 | NÃO SE APLICA | — | `card_generator.py:559-671` (placeholder); `ComparisonCard.tsx:5-56` (compara BPM absoluto, não importado por nenhuma página); sem modelo de artista | Quando existir: precisará de modelo de consentimento do artista e score de similaridade, não BPM absoluto |
| G10 | NÃO SE APLICA | — | grep `licen\|licensor` backend/app frontend/lib android → só direitos do consumidor; `models/card.py:36` `video_url` sem uso | Licença dos vídeos da landing (`frontend/public/site/*.mp4`) não documentada |
| G11 | ATENDE | — | `cards.py:195-203` (só do próprio usuário); `SiteLanding.tsx:305-316` (mockups estáticos); `site-copy.ts:150` (exemplos ilustrativos) | `site-copy.ts:196` "REALNESS FEST 116" reaproveita um número de noite real do fundador; capturas `public/site/shots/*.png` não abertas |
| H1 | PARCIAL | BAIXO | `requirements.txt:17` + `main.py:22-30` (Sentry se `SENTRY_DSN`); `frontend/lib/utils/analytics.tsx:5-41` (PostHog sem callers e sem pacote); Android: grep → 0 | Só Sentry pode ligar; PostHog é código morto |
| H2 | PARCIAL | ALTO | `main.py:26-30` sem `send_default_pii`/`max_request_body_size`/`before_send`; `grep -rn "trackEvent\|identifyUser" frontend` → 0 callers | Nenhum tracking envia bpm. Sentry, se ligado, captura corpo de requisição até ~10 KB e variáveis locais em erro: um upload pequeno ou um post do feed pode ir inteiro |
| H3 | ATENDE | — | grep `admob\|advertising_id\|AD_ID\|gms.ads` → 0; `ShareTargets.kt:57-62` evita SDK do TikTok por causa do AD_ID | — |
| H4 | PARCIAL | MÉDIO | `main.py:114`, `cards.py:147`, `auth.py:170,389` (`traceback`); Android app: `grep -rn "Log\.[dievw](" android/app/src/main/java` → 0; **`android-capture/.../CaptureService.kt:162-164`** (bpm ao vivo no texto da notificação) | Sem log de bpm no servidor; exceções de banco podem carregar e-mail; app antigo expõe bpm na tela de bloqueio |
| H5 | PARCIAL | MÉDIO | grep `webhook\|partner\|export` backend/app/api → 0; admin: `waitlist.py:88-109` + `admin/waitlist/page.tsx:64-80` (CSV de e-mails); `moderation.py:28-79` (nome + bpm dos posts denunciados); `SessionExporter.kt:40-95` (ZIP bruto com RR, movimento, MAC do sensor) | Nada sai para parceiro. O ZIP do operador sai pelo share sheet para qualquer destino |
| H6 | PARCIAL | ALTO | Railway, Vercel, Resend (`email.py:12,28-41`), API-Football, Sentry, Redis; `grep -rn -i "DPA\|data processing\|suboperador" docs/` → 0 | Nenhum contrato de operador referenciado; regiões não visíveis; R2/boto3 e Setlist.fm declarados e não usados |
| I1 | NÃO ATENDE | CRÍTICO | `schemas/auth.py:7-13`; `models/user.py:14-30`; grep `birth\|nascimento\|idade` em signup → 0 | Não pergunta data de nascimento |
| I2 | NÃO ATENDE | CRÍTICO | Único hit: `terms-copy.ts:29` "Você precisa ter 18 anos ou mais"; sem checkbox, sem aceite, sem CPF/documento/ingresso | Nem autodeclaração existe |
| I3 | PARCIAL | MÉDIO | `docs/handoff-2026-09-18.md:109-112`; `handoff-2026-09-24.md:66-68`; sem `fastlane/`/`metadata/` | 18+ com restrição a menores declarado no Console, só em doc. Categoria "Health apps: Atividade e condicionamento físicos" declarada ao Google contradiz a marca (ver J1) |
| I4 | ATENDE | — | grep `streak\|reward\|mascot\|troféu` → `GalleryScreen.kt:64` "A temporada, não o streak"; `Reminders.kt:16` | Sem gamificação nem mascote |
| J1 | PARCIAL | ALTO | Visíveis: `experience/page.tsx:172` "Picos de Emoção"; `[username]/page.tsx:123` e `manifest.json:4` "Compartilhe a emoção"; `strings.xml:349` "com HRV"; `live/page.tsx:605` "Intervalos R-R"; `android-capture strings.xml:168` "inicie um treino"; `README.md:3` "monitoring… emotional"; `site-copy.ts:152,184` "MEU PSICOLÓGICO", "GALERIA DE SENTIMENTOS" | Zero hits de estresse/ansiedade/zona/recuperação/diagnóstico/risco visíveis (só negados em disclaimers); "frequência cardíaca" só em texto legal/permissão |
| J2 | PARCIAL | MÉDIO | `strings.xml:58`; `privacy-copy.ts:49`; `terms-copy.ts:23` | Frase equivalente ("não é serviço de saúde… nada é diagnóstico") existe; "não é dispositivo médico" literal não; no app só na tela de permissão |
| J3 | PARCIAL | ALTO | Páginas: `frontend/app/(public)/{privacidade,termos,en/privacy,en/terms}`; conteúdo em `privacy-copy.ts`, `terms-copy.ts`. Link só no rodapé da landing (`SiteLanding.tsx:444-450`); zero links no app Android e no cadastro | Faltam: base legal, "dado sensível", controlador/CNPJ, encarregado, RR/movimento/imports na lista de dados, estatística coletiva, upload automático, operadores nomeados, "não vai para clube/artista", prazos por categoria |
| J4 | PARCIAL | ALTO | HC: `strings.xml:54-63,476` preciso mas contradito por `SetupScreen.kt:350`; `MainActivity.kt` não trata `ACTION_SHOW_PERMISSIONS_RATIONALE` (manifesto l.74,126 apontam para ele). BLE: `strings.xml:358-361` omite notificação, localização (≤API 30), acelerômetro e RR. FGS: `CaptureService.kt:355-378` sem bpm | App antigo: rationale correto (`PrivacyRationaleActivity`), mas notificação com bpm |
| K1 | NÃO ATENDE | ALTO | grep `request.client\|x-forwarded-for\|ip_address\|user_agent` backend/app → 0; `refresh_token.py:33-61` sem IP; só access log stdout do uvicorn (`Dockerfile:17`) | Retenção de 6 meses depende só do Railway (NÃO VERIFICÁVEL) |
| K2 | NÃO ATENDE | ALTO | grep `incident\|runbook\|breach\|vazamento\|ANPD` docs/ backend/ .github/ → nada aplicável | Sem plano |
| K3 | NÃO ATENDE | MÉDIO | grep `pgaudit\|anomal\|prometheus\|grafana` → 0 | Só detecção de reuso de refresh token (`refresh_token.py:20-25`) |
| L1 | NÃO ATENDE | ALTO | grep `base legal\|ROPA\|registro de operações\|inventário` docs/ → 0 | O mais próximo é a resposta de Data Safety do Play (`handoff-2026-09-18.md:111`) |
| L2 | NÃO ATENDE | ALTO | grep `RIPD\|DPIA\|relatório de impacto` → 0 | — |
| L3 | NÃO ATENDE | ALTO | grep `encarregado\|DPO` docs/ → 0 | — |
| L4 | PARCIAL | MÉDIO | `privacy-copy.ts:38,83,89`; `signup_codes.py:38-40` (24 h); `cards.py:184-186` (Redis 7 d) | Só três prazos; nada para waitlist, denúncias, tokens, logs, Sentry |
| L5 | NÃO ATENDE | MÉDIO | `CLAUDE.md:114` (OAuth — `auth.py` só e-mail), `:115` (Celery — `card_tasks.py` nunca despachado), `:120` (R2 — sem boto3 em uso), `:126` (PostHog morto), `:263-266` (waitlist "só e-mail" vs `models/waitlist_entry.py:37-38`), `:494` (HC "OAuth REST" vs SDK on-device) | A política de privacidade repete a inexatidão da waitlist (`privacy-copy.ts:37`) |

---

## 3. Achados críticos detalhados

### CR-1 — Não existe consentimento por finalidade nem registro de consentimento (A2, A3, A6)

**Encontrado.** O único ato de consentimento do produto é o diálogo de permissão do Health Connect, disparado em `android/app/src/main/java/cc/tumtum/app/ui/screens/permission/PermissionScreen.kt:124`. Nada é gravado do lado da TumTum: nenhuma tabela nas 13 migrações de `backend/alembic/versions/`, nenhuma coluna em `backend/app/models/user.py:11-39`, nenhuma chave em `android/.../data/prefs/UserPrefs.kt`. Não há aceite de termos no cadastro (`frontend/app/(auth)/signup/page.tsx` sem link nem checkbox; `CreateAccountScreen.kt` idem). As finalidades (b) guardar a noite, (c) estatística coletiva, (d) artista, (e) melhoria do detector e (f) marketing não têm consentimento porque o código nem oferece a escolha.

**Por que viola.** O parecer fixa consentimento específico, destacado, por finalidade, registrado e revogável como a única base legal viável para dado de saúde. Sem registro não há como provar quem consentiu com o quê, quando, e com qual texto.

**Correção recomendada.**
- Tabela `consents (id, user_id FK, purpose ENUM, text_version, granted_at, revoked_at, means ENUM[tap,button,form], client, created_at)` com migração Alembic; índice em `(user_id, purpose)`; o estado vigente é a última linha por finalidade.
- Endpoints `POST /api/consents` e `DELETE /api/consents/{purpose}`; toda leitura/processamento que dependa de uma finalidade consulta o consentimento ativo (`require_consent(purpose)` como dependência FastAPI, ao lado de `get_current_user`).
- Tela de consentimento própria no Android, antes do diálogo do Health Connect, com um toggle por finalidade, todos desmarcados, e a versão do texto embarcada no build. A tela grava no servidor (ou enfileira offline) antes de prosseguir.
- Aceite de termos e política no cadastro (web e Android), gravado na mesma tabela como finalidade `terms`.

### CR-2 — A noite sobe ao servidor sem ato do usuário, contra a promessa escrita do app (A2-b, A6, J3)

**Encontrado.** `android/.../data/repo/EndNightFlow.kt:35` chama `sync.uploadLater(nightId)` ao encerrar a captura; `TumTumApp.kt:62-63` retenta a cada abertura do app; `RevealScreen.kt:90` retenta ao abrir a noite. Ao mesmo tempo, `strings.xml:20` diz "Nada deixa seu aparelho sem você mandar" e `frontend/lib/privacy-copy.ts:67` diz que o dado do sensor "só sobe para a TumTum quando você manda".

**Por que viola.** A finalidade (b) precisa de consentimento próprio; e o app afirma sobre si mesmo algo falso, exatamente a classe de bug que o decision log persegue.

**Correção recomendada.** Ou o upload passa a depender de um toggle de consentimento gravado (CR-1) e de um toque explícito na tela de fim de noite ("Guardar na minha coleção"), ou a promessa muda de texto nas duas fontes. A primeira opção é a que o parecer pede.

### CR-3 — Sem termo de consentimento nem plano de descarte para o piloto (A8, D6)

**Encontrado.** `grep -rn -i "termo de consentimento\|TCLE\|consent form" docs/ README.md` → nada. `docs/pilot-event-options.md:206-207` só anota "Consent. Health data, five people, in writing, before the day". Nenhuma rotina ou documento apaga dados do piloto ao fim (`grep -rn -i "pilot\|piloto" docs/*.md | grep -i -E "apagar|delet|purge|reten"` → 0). O export do operador ainda nomeia arquivos com `participantId` (`SessionExporter.kt:41-43`) sem rastrear para onde vão.

**Correção recomendada.** Termo escrito (modelo em `docs/pilot-consent-template.md`), com as seis finalidades separadas, prazo de retenção do piloto, nome do responsável, e registro assinado por participante guardado fora do repositório. Documento `docs/pilot-data-retention.md` com data de descarte, script de apagamento por `event_id` (`DELETE hr_data/peaks/hr_sessions/cards/event_posts WHERE event_id = …`, mais `FLUSHDB` seletivo do Redis `card:image:*`), e inventário dos ZIPs de operador com destino e data de exclusão.

### CR-4 — Nenhuma verificação de idade (I1, I2)

**Encontrado.** Cadastro coleta e-mail, nome e senha (`backend/app/schemas/auth.py:7-13`; `models/signup_code.py:32-36`). Não há data de nascimento, checkbox de 18+, aceite de termos nem verificação externa. A única menção é `frontend/lib/terms-copy.ts:29`, em página que o cadastro não linka.

**Correção recomendada.** Mínimo para o piloto: data de nascimento no cadastro (picker, nunca digitada, conforme regra de produto), recusa no servidor abaixo de 18 (`schemas/auth.py` + validação em `api/auth.py`), gravação em `users.birth_date`, aceite explícito de termos gravado (CR-1). Para o lançamento público: avaliar vínculo com ingresso nominal ou provedor de verificação, já que o parecer trata autodeclaração isolada como insuficiente.

### CR-5 — `SECRET_KEY` com padrão conhecido e sem guarda (C4; condicional a configuração fora do repositório)

**Encontrado.** `backend/app/config.py:11` `secret_key: str = "your-secret-key"`. `grep -rn "secret_key\|your-secret-key" backend/app` mostra uso em `core/auth.py:28,33` e `api/auth.py:191,235`, e nenhuma verificação na inicialização (`main.py:33-85`). Se `SECRET_KEY` não estiver no ambiente do Railway, qualquer pessoa assina um JWT válido para qualquer `sub` e lê sessões, séries e cards de qualquer conta.

**Correção recomendada.** Em `main.py` (lifespan) ou em `config.py` (validator Pydantic): se `environment != "development"` e `secret_key in {"your-secret-key", "dev-secret-key-change-in-production", ""}` ou `len < 32`, abortar o start com mensagem clara. Conferir agora no Railway (seção 5).

### CR-6 — Dados reais do fundador em fixtures e e-mails de terceiros em docs (C8)

**Encontrado.** Não há CSV, dump ou log BLE de participante no repositório nem no histórico git (`git log --all --diff-filter=A --name-only | grep -E '\.(csv|json|sql|dump|log|txt)$'` → só configs). Mas:
- `frontend/lib/health/import-parsers.polar.test.ts:5-11` — cabeçalho de export Polar com o nome completo do fundador, data, hora e amostras de bpm.
- `android/app/src/test/java/cc/tumtum/app/domain/BeatFilterTest.kt:15-21` — 68 leituras reais de uma noite do fundador (o próprio comentário do teste diz que é o export bruto da "night 18").
- `docs/decision-log.md:170,1019,1107-1108,1797,5459` — e-mails pessoais/profissionais do fundador; `docs/handoff-2026-09-18.md:108` — conta de login do revisor do Play; `docs/pilot-event-options.md:379-382` — contatos de fã-clubes (terceiros).
O repositório é público (comentário em `.github/workflows/build-app.yml` e instrução do CLAUDE.md sobre release assets).

**Correção recomendada.** Substituir os dois fixtures por séries sintéticas com a mesma forma (o `BeatFilterTest` precisa só do padrão de RR ausente; o parser Polar precisa só do formato de cabeçalho, com nome fictício). Remover os e-mails de terceiros dos docs e reescrever o histórico desses arquivos, ou aceitar o histórico e tratar como decisão registrada. Adicionar um check de CI (`gitleaks` ou regex de e-mail/nome) que bloqueie novos casos.

---

## 4. Achados altos detalhados

### AL-1 — O setup lê 24 horas do Health Connect fora de qualquer evento (B1, A6, J3, J4)
`android/.../ui/screens/sources/SetupScreen.kt:349-351` chama `readWindowBySource(now − 24 h, now)` para escolher a fonte do relógio. `strings.xml:56`, `strings.xml:329`, `privacy-copy.ts:57,66` prometem leitura só na janela do evento; o KDoc de `HealthConnectSource.kt:54` afirma o mesmo. O dado não é enviado, mas é lido, e a promessa é falsa. **Correção:** trocar a medição de densidade por uma janela curta (os últimos 30–60 minutos, ou o intervalo do próximo evento ativado), e dizer isso na tela. Alternativa: manter 24 h e declarar na permissão e na política.

### AL-2 — Backup automático do Android ligado sobre banco de saúde não criptografado (C10, C11, D5)
`android/app/src/main/AndroidManifest.xml:56` `allowBackup="true"`, sem regras. O Room (`samples`, `ble_samples`, `rr_intervals`, `motion`, `moments`) e o DataStore com o refresh token de 90 dias vão para o backup do Google e para transferência entre aparelhos. `delete-account-copy.ts:56` diz "Não há backup com prazo". **Correção:** `android:allowBackup="false"` (ou `dataExtractionRules` excluindo o banco e o DataStore); avaliar SQLCipher e `EncryptedSharedPreferences`/DataStore criptografado para os tokens; apagar as leituras brutas locais após upload confirmado ou dar ao usuário o controle.

### AL-3 — Todo card é público por URL desde a criação, sem consentimento de publicação, sem noindex, sem expiração (G5, G6, C5, D4)
`backend/app/api/cards.py:223-247` (`/public`) e `:250-299` (`/image`) não exigem autenticação nem checam se houve share; o docstring diz "sharing is an explicit act", mas o código não verifica. `frontend/app/(public)/cards/[id]/page.tsx:26-55` gera OpenGraph com bpm e nome. Sem `noindex`/robots. O cache Redis (`cards.py:184-186`) sobrevive à exclusão do card e da conta, e `/image` lê o Redis antes do banco (`:262-270`), então um card apagado segue servido por até 7 dias, contra `delete-account-copy.ts:42` ("sai na hora"). **Correção:** coluna `cards.published_at` gravada só pelo endpoint de share; `/public` e `/image` retornam 404 sem ela; `<meta name="robots" content="noindex">` na página; `redis.delete(card:image:*)` em `DELETE /cards/{id}` e em `delete_account`; inverter a ordem (banco antes do cache).

### AL-4 — Estatística coletiva sem opt-in e com piso de 2 pessoas por célula (G7, G8, A2-c)
`backend/app/api/feed.py:439-443` conta toda `hr_session` do evento; `services/crowd.py:29` `MIN_CROWD=4`; `:111` piso por célula `max(2, round(n*0.3))`. Publica `people` exato por balde de 2 minutos. O parecer sugere N≥100 e supressão abaixo do mínimo. **Correção:** só sessões com consentimento (c) ativo; `MIN_CROWD` e piso por célula configuráveis, com o valor do parecer como padrão; publicar faixas ("mais de 10") em vez de contagens exatas em coortes pequenas; documentar a métrica na política.

### AL-5 — Card e telas ainda afirmam estado emocional que ninguém mediu (F3, J1)
`android/.../res/values/strings.xml:154` "EU TAVA TRANQUILO. AÍ VEIO ISSO." é o título de todo card gerado no Android (`CardScreen.kt:183`); `strings.xml:615` "UMA NOITE TRANQUILA." quando não há momentos; `frontend/app/(app)/experience/page.tsx:172` "Seus Picos de Emoção"; `[username]/page.tsx:123` e `manifest.json:4` "Compartilhe a emoção". O backend já removeu a primeira frase exatamente por esse motivo (`card_generator.py:108-115`). **Correção:** portar `moment_copy()` do backend para o Android (título derivado dos números da noite), trocar "Picos de Emoção" por "Seus momentos", e revisar a tagline.

### AL-6 — Sentry com padrões que podem enviar corpo de requisição com bpm (H2, H6)
`backend/app/main.py:26-30` inicializa `sentry_sdk` só com `dsn`, `environment` e `traces_sample_rate=0.2`. Com o SDK 1.39 os padrões capturam corpo de requisição até ~10 KB e variáveis locais em exceções; um upload curto (`POST /api/health/sessions`) ou um post do feed (bpm, nome) pode ir inteiro para um operador sem contrato. `privacy-copy.ts:82` promete o contrário. **Correção:** `max_request_body_size="never"`, `include_local_variables=False`, `send_default_pii=False` explícito, `before_send` que remova `request.data`; DPA com o Sentry ou desligar o DSN até tê-lo.

### AL-7 — Pseudonimização inexistente; identidade e série no mesmo banco (C1)
FK direta `hr_data.session_id → hr_sessions.user_id → users.id` (`models/hr_data.py:17-19`, `hr_session.py:44-46`), nome e e-mail em `users` no mesmo schema. Nome copiado em `cards.metadata` (`cards.py:167`). **Correção:** ao menos separar identidade em schema próprio com role de banco distinta e usar um `subject_id` opaco nas tabelas de saúde; a curto prazo, não duplicar nome dentro de `cards.metadata` (resolver na leitura).

### AL-8 — Sem log de acesso a dado cardíaco, sem retenção de logs, sem plano de incidente, sem encarregado, sem ROPA/RIPD, sem DPA (C6, K1, K2, E4, L1, L2, L3, H6)
Todos com busca vazia (tabela geral). **Correção:** tabela `data_access_log (actor_id, subject_id, resource, action, at, ip)` escrita pelas rotas de `health.py`, `experience.py`, `cards.py`, `moderation.py`; middleware de access log com IP real (`--proxy-headers`), retido 6 meses; `docs/incident-response.md` com responsáveis, prazo de 3 dias úteis e modelo de comunicação; nomeação do encarregado com canal próprio na política e no app; `docs/ropa.md` e `docs/ripd.md`; contratos de operador com Railway, Vercel, Resend, Sentry e registro de região.

### AL-9 — Retenção indefinida da série bruta e do momento; sem apagar-uma-noite (D1, D2, A5)
Nenhum job apaga `hr_data`/`peaks`; sem Celery beat; sem `DELETE /api/health/sessions/{id}`. **Correção:** job diário (Celery beat ou cron do Railway) que apaga `hr_data` com `time < now − 30 d` de sessões já analisadas, mantendo `peaks` enquanto o consentimento (b) estiver ativo; endpoint e tela para apagar uma noite (fecha o item 35).

### AL-10 — `/api/demo/simulate` aberto a qualquer usuário autenticado e presença falsificável (C5, C9)
`backend/app/api/demo.py:368-372` só exige `get_current_user`; apaga sessões, picos e série reais do chamador para o evento (`:406-425`) e cria uma sessão sintética que conta como presença (`feed.py:62-92`), abrindo o feed de outros usuários e inflando `measured_nights`. `POST /api/health/sessions` também aceita `event_id` do cliente sem prova de presença (`schemas/health.py:40`). **Correção:** `require_admin` em `/simulate` e `environment != "production"` para montar o router; marcar sessões sintéticas e excluí-las de `require_attendance` e do crowd.

### AL-11 — Keystore de debug commitado assina APKs publicados em release público (C4)
`android/app/debug.keystore` + `build.gradle.kts:34-37` (senha padrão) assinam os APKs que `.github/workflows/build-app.yml` publica como release asset. Qualquer pessoa com o repositório gera um "update" instalável por cima do app de um participante (mesma assinatura) e lê o Room sem criptografia. **Correção:** para o piloto, distribuir só o build de release assinado pela upload key (já existe no workflow quando os secrets estão presentes) ou pelo Play internal testing; não publicar APK de debug como release.

### AL-12 — Política e rationale incompletos e não linkados no app (J3, J4, A6)
Sem base legal, sem "dado sensível", sem controlador/CNPJ, sem encarregado, sem RR/movimento/imports na lista de dados, sem estatística coletiva, sem operadores nomeados. Zero links para a política no app Android (`grep -rn -i "privacidade\|privacy" android/app/src/main` → só um comentário) e no cadastro web. O intent `ACTION_SHOW_PERMISSIONS_RATIONALE` aponta para `MainActivity` (manifesto l.74,126), que não o trata — o Health Connect abre o app normal em vez de uma tela de rationale. **Correção:** tela de rationale própria (o app antigo tem uma em `PrivacyRationaleActivity.kt` para copiar), link para política e termos em Configurações e no cadastro, e a lista de itens faltantes acima na política.

### AL-13 — App antigo (`cc.tumtum.capture`) mostra bpm ao vivo na notificação (H4, J4)
`android-capture/app/src/main/java/cc/tumtum/capture/CaptureService.kt:162-164` escreve `"$bpm bpm · N leituras"` no texto da notificação, sem `setVisibility(VISIBILITY_PRIVATE)`; aparece na tela de bloqueio. **Correção:** remover o bpm do texto (o app novo já faz isso, `CaptureService.kt:347-348`) ou retirar o app antigo de circulação.

### AL-14 — Vídeo do usuário copiado com metadados originais para o Story (G4)
`ShareTargets.kt:380-388` copia o vídeo escolhido byte a byte (GPS e data originais preservados) e o entrega ao Instagram/Facebook/Snap como fundo. **Correção:** reencodar sem metadados (o Media3 Transformer já está no projeto) ou avisar na tela.

---

## 5. Itens não verificáveis (conferir fora do repositório)

| Item | O que conferir | Onde |
|---|---|---|
| C2 | Criptografia em repouso do Postgres e do Redis; criptografia e prazo dos snapshots automáticos; região dos serviços | Railway → serviço Postgres/Redis → Settings/Volumes; página de conformidade da Railway |
| C3 | TLS entre o app FastAPI e o banco (asyncpg sem `ssl=`) | Railway → variável `DATABASE_URL`: se for host interno (`*.railway.internal`), o tráfego fica na rede privada; se for host público, exigir `?ssl=require` |
| C4 / CR-5 | `SECRET_KEY` definido e longo | Railway → serviço backend → Variables |
| H1 / H2 | `SENTRY_DSN` está definido? Se sim, retenção de eventos e DPA | Railway Variables; sentry.io → Settings → Legal & Compliance |
| H6 | Contratos de operador e região: Railway, Vercel, Resend, Sentry | Painéis de cada provedor; guardar PDFs fora do repo e referenciar em `docs/ropa.md` |
| K1 | Retenção e controle de acesso dos logs do uvicorn; se o IP logado é o real | Railway → Observability → retenção; testar `--proxy-headers` |
| D5 | Backups do banco: frequência, prazo, quem acessa | Railway → Postgres → Backups |
| I3 | Classificação etária resultante (IARC) e categoria "Health apps" declarada | Play Console → Content rating; Policy → App content → Health apps |
| G4 | Se o Media3 Transformer copia `Mp4LocationData` do vídeo de origem | Testar com um vídeo com GPS e inspecionar o MP4 gerado (`exiftool`) |
| G11 | Se `frontend/public/site/shots/09-feed.png` e `10-feed-evento.png` mostram nome ou bpm de pessoa real | Abrir as duas imagens |
| G10 | Licença dos vídeos `frontend/public/site/cold-play.*` e `torcida.*` | Origem dos arquivos |
| B2 | Se o APK do spike (`spike-apk` release) foi distribuído a alguém | GitHub → Releases → downloads |
| D4 | Se existem ZIPs de operador (`tumtum-<participantId>-night<N>.zip`) em algum celular ou drive | Inventário manual |

---

## 6. Itens não aplicáveis (o que ainda não existe e vai precisar existir)

**Bloco piloto (antes de gravar a primeira pessoa que não seja o fundador):**
- Tabela e telas de consentimento por finalidade (A2/A3) — hoje inexistentes.
- Termo de consentimento escrito do piloto e registro fora do repo (A8).
- Rotina e data de descarte dos dados do piloto (D6).
- Verificação de idade, ao menos data de nascimento com recusa no servidor (I1/I2).
- Guarda de `SECRET_KEY` na inicialização (C4).

**Bloco lançamento público:**
- Encarregado nomeado com canal próprio (E4/L3), ROPA (L1), RIPD (L2), plano de incidente (K2), DPAs (H6).
- Log de acesso a dado cardíaco (C6) e access log com retenção de 6 meses (K1).
- Job de retenção da série bruta (D1) e apagar-uma-noite (D2/A5).
- Exportação CSV/JSON para o titular (E2), acesso consolidado (E1), correção de e-mail (E3), registro de pedidos (E5).
- Consentimento de publicação por card, noindex, expiração e revogação (G5/G6).
- Pseudonimização da identidade (C1).

**Bloco cards avançados (só quando forem construídos):**
- **G9 "Na mesma vibe":** modelo `artist_participations (artist_id, event_id, consent_scope, consented_at, expires_at)`; comparação por score de similaridade, nunca BPM absoluto lado a lado (o `ComparisonCard.tsx` atual faz o contrário e deve ser descartado).
- **G10 "Ver o momento":** tabela `media_licenses (media_id, licensor, scope, valid_from, valid_until, source)`; o gerador recusa mídia sem linha ativa.
- **G7 "A galera":** N mínimo do parecer, opt-in por finalidade (c), métrica documentada e publicável.

---

## 7. Sugestão de ordem de correção

Do mais urgente ao menos urgente, considerando que o próximo marco é o piloto com participantes reais:

1. **Conferir e travar `SECRET_KEY`** no Railway e adicionar a guarda de inicialização (CR-5). Uma tarde.
2. **`allowBackup="false"`** no app principal (AL-2). Uma linha; entra no próximo build.
3. **Trocar os dois fixtures com dado real por sintéticos** e retirar e-mails de terceiros dos docs (CR-6).
4. **Termo de consentimento do piloto** (modelo escrito, seis finalidades, prazo de descarte) e **plano de descarte** com script por `event_id` (CR-3).
5. **Tabela `consents` + tela de consentimento por finalidade + aceite de termos no cadastro** (CR-1). É o maior item e o que o parecer chama de única base legal.
6. **Upload só por ato explícito** ligado ao consentimento (b) (CR-2). Sem isso, o item 5 não vale para a finalidade mais importante.
7. **Data de nascimento no cadastro com recusa abaixo de 18** (CR-4).
8. **Setup lendo janela curta em vez de 24 h** (AL-1).
9. **`/demo/simulate` só admin e fora de produção**; sessões sintéticas fora da presença e do crowd (AL-10).
10. **Cards públicos só após share, noindex, limpeza do Redis na exclusão, banco antes do cache** (AL-3).
11. **Título do card Android derivado dos números; "Picos de Emoção" e "Compartilhe a emoção" reescritos** (AL-5).
12. **Sentry sem corpo de requisição nem locais**, ou DSN desligado até haver DPA (AL-6).
13. **Distribuir ao piloto só builds de release** (upload key ou Play internal testing), nunca o APK de debug (AL-11); retirar o `cc.tumtum.capture` de uso ou tirar o bpm da notificação (AL-13).
14. **Estatística coletiva só com consentimento (c), N mínimo do parecer, faixas em vez de contagens** (AL-4).
15. **Política de privacidade completa e linkada no app e no cadastro; tela de rationale do Health Connect** (AL-12).
16. **Encarregado, ROPA, RIPD, plano de incidente, DPAs** (AL-8).
17. **Job de retenção de 30 dias para a série bruta; apagar-uma-noite** (AL-9).
18. **Log de acesso a dado cardíaco e access log com retenção** (AL-8).
19. **Exportação e acesso consolidado para o titular; correção de e-mail; registro de pedidos** (E1–E3, E5).
20. **Pseudonimização da identidade em schema separado** (AL-7).
21. **Reencodar vídeo sem metadados; limpar `cacheDir` após share e na exclusão; apagar avatar local** (AL-14, C7, D4).
22. **Atualizar CLAUDE.md e README para o que o código faz** (L5) e fechar as inexatidões da política (waitlist com nome, upload automático, estatística coletiva).
