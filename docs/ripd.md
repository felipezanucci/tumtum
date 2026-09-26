# Relatório de Impacto à Proteção de Dados Pessoais (RIPD) — rascunho

Art. 5º, XVII, e art. 38 da LGPD. **Rascunho de 26/09/2026 (noite)**,
reestruturado pelo conteúdo mínimo do parecer v1.1 (§5.3). A versão da manhã
nasceu da remediação da auditoria (`RELATORIO-AUDITORIA-LGPD.md`, item L2);
os riscos R1–R20 e os residuais dela continuam aqui (§7). Não foi revisado por
advogado: [PENDENTE — Felipe: revisão jurídica].

**O que este documento é, e o que não é.** A ANPD descreve o RIPD como a
documentação dos tratamentos que podem gerar alto risco, recomenda fazê-lo
nesses casos e pode requisitá-lo nas hipóteses da lei; a regulamentação
específica ainda está em elaboração (Agenda Regulatória 2025–2026). **Não
afirmamos que o art. 38 obriga um RIPD antes de todo piloto.** A TumTum adota
o RIPD pré-piloto como **GATE interno** (parecer §5.2): nenhum participante que
não seja o fundador grava uma noite antes deste relatório estar assinado com o
risco residual aceito. Afrouxar o gate pede decisão escrita no
`docs/decision-log.md`.

- **Controlador:** [PENDENTE — Felipe: razão social e CNPJ]
- **Encarregado e responsável por este relatório:** Felipe Zanucci
  (oi@tumtum.cc, assunto "Privacidade") — provisório e em conflito de
  interesse (`docs/dpo.md`; item 80)
- **Revisão:** antes do primeiro participante do piloto que não seja o
  fundador; depois, a cada 6 meses (**26/03/2027**) ou antes, a cada gatilho do
  §10. Data do piloto: **[PENDENTE]**.

## 1. Descrição do produto, do fluxo e dos agentes

A TumTum lê o batimento cardíaco de uma pessoa durante um show ou um jogo —
do relógio dela (Health Connect, no celular) ou de uma cinta Bluetooth —,
encontra os momentos em que ele subiu em relação ao próprio batimento da
pessoa naquela noite, liga cada momento ao que estava acontecendo (a música, o
gol) e gera um card que ela pode compartilhar.

- **Natureza:** dado pessoal sensível (saúde, art. 5º, II), em série
  temporal, ligado a identidade (nome, e-mail) e a presença em um evento
  (lugar e hora). Os derivados (momento, curva, pico) seguem o mesmo padrão.
- **Escala:** MVP. Algumas dezenas de contas; piloto de 3 a 5 pessoas.
- **Contexto:** entretenimento, não saúde. A pessoa espera ver "o que ela
  sentiu", não uma avaliação do coração.
- **Ciclo:** captura no celular → (só com "guardar a noite") envio ao
  servidor → detecção de momentos → card → (só por toque) compartilhamento ou
  post no feed do evento → **série bruta apagada em 7 dias** → momentos e
  cards até a pessoa apagar ou revogar.
- **Agentes:** a TumTum é controladora. Operadores: Railway, Vercel, Resend,
  Sentry, o provedor da caixa de e-mail e, se o proxy estiver ligado,
  Cloudflare (`docs/dpa-checklist.md`). Não são operadores: Google (Health
  Connect roda no celular da pessoa; Play distribui o app), API-Football,
  GitHub.

## 2. Mapa de dados

| Camada | O que existe | Dado pessoal que passa | Onde fica |
|---|---|---|---|
| **Dispositivo (sensor)** | Relógio (via Health Connect) ou cinta Polar H10 (BLE) | bpm por instante; R-R e contato com a pele (cinta) | No sensor e no Health Connect, fora do controle da TumTum |
| **App** (`cc.tumtum.app`) | Captura, detector local, noite, card, compartilhamento, notificação da revelação | bpm, R-R, movimento do celular, noite, momentos, tokens, avatar | Room com SQLCipher, fora do backup do Android; tokens criptografados; cards temporários em `cacheDir` (1 h) |
| **Site / web app** (tumtum.cc) | Cadastro, perfil, importação Polar, noite, card público, pedidos | Conta, noites, arquivo Polar enviado | Vercel. No navegador: access token; **refresh token em cookie `httpOnly`** (26/09 noite). Tem `manifest.json`, mas **nenhum service worker registrado** (conferido 26/09): nenhuma resposta com batimento fica em cache offline |
| **API** (FastAPI) | Todas as operações do servidor | Tudo o que sobe | Railway |
| **Backend** | Postgres com TimescaleDB; Redis | Conta, ledger, noites guardadas, momentos, cards, feed, logs | Railway, mesmo banco e mesma credencial (RR1) |
| **Storage** | Nenhum armazenamento de objetos (o R2 nunca foi usado) | Imagem do card no Redis, 7 dias | Railway |
| **Renderização** | Card gerado na requisição, no servidor; no celular, card em Compose e vídeo remuxado sem metadados | Pico, hora, evento, curva | Redis (7 dias); `cacheDir` do celular (1 h) |
| **Analytics** | **Nenhum.** Nenhum SDK de analytics ou anúncio nos três códigos | — | — |
| **Observabilidade** | Sentry (se `SENTRY_DSN` estiver definido); `access_log`; `data_access_log` | Stack trace sem PII; IP e rota; quem leu quem | Sentry [PENDENTE — região]; Railway |
| **Suporte** | Caixa oi@tumtum.cc; `data_subject_requests`; admin | O que a pessoa escreve | [PENDENTE — provedor da caixa]; Railway |
| **Fornecedores** | Railway, Vercel, Resend, Sentry, Cloudflare, provedor da caixa | Ver `docs/ropa.md` §2 | Transfer Register, `docs/dpa-checklist.md` |
| **Desenvolvimento e IA** | Repositório público; sessões do Claude Code | **Nenhum dado real** — fixtures sintéticos, `privacy-scan` no CI | GitHub; a regra está em `docs/incident-response.md` |

## 3. Finalidades e bases por operação

Uma linha por operação em `docs/ropa.md` §1 (O1–O24), com a base de cada uma.
O resumo: **conta, códigos, troca de e-mail e senha** por execução de contrato
(art. 7º, V); **gerar o momento, guardar a noite, A galera, melhorar o
detector e cada post** por consentimento específico (art. 11, I), cada um com
opt-in próprio; **marketing** e **lista de espera** por consentimento (art. 7º,
I); **access log, ledger, pedidos de titular, exclusão e registro de
incidentes** por obrigação legal (art. 7º, II); **sessão e registro de quem leu
dado de saúde** por prevenção à fraude e segurança do titular (art. 11, II, g —
VALIDAR); **moderação** por contrato e exercício regular de direitos;
**Sentry** por legítimo interesse (art. 7º, IX) só porque nada sensível chega
lá — LIA a documentar.

## 4. Necessidade e proporcionalidade de cada dado, e o prazo

| Dado | Por que é necessário | O que seria menos | Prazo |
|---|---|---|---|
| **bpm com hora** | Sem ele não há produto. Nenhuma outra medida de saúde é lida (sem sono, calorias, VO2, SpO2) | — | No celular: até apagar a noite. No servidor: 7 dias (bruto) |
| **Janela da leitura** | Relógio: evento ±30 min. Setup: últimos 60 min, uma vez, para medir a densidade do relógio, sem enviar nada. Cinta: do "Começar" ao fim | A janela exata do evento, sem margem — perde o começo atrasado de um show | A da captura |
| **R-R e movimento** | Só o filtro "fora da pele" no celular | Não aceitos pelo servidor | Apagados quando a noite é guardada |
| **Série bruta no servidor** | O detector do servidor roda sobre ela; reanalisar depois de uma correção do detector ("Procurar meus momentos"); conferência da noite pela pessoa e, no piloto, pela equipe | Não guardar (§8) | **7 dias** após a análise. A análise acontece na chegada; sete dias cobrem uma reanálise depois de um conserto e a semana de conferência do piloto. **Os 30 dias anteriores não tinham necessidade que os justificasse** (parecer §11) |
| **Momentos, resumo, cards** | A coleção é a razão de guardar a noite | — | Enquanto a conta e `keep_night` existirem |
| **O que o card revela** | A pessoa escolhe: antes de compartilhar, o app diz o que o card mostra, e hora, evento e bpm exato têm interruptor (bpm arredondado à dezena quando desligado) | — | Público enquanto publicado |
| **Contagem coletiva** | Faixas (`10+`…`250+`), ≥ 100 noites, ≥ 10 pessoas por momento, só de quem consentiu | Não existir (bloqueada como produto até o teste formal, §8) | Calculada na leitura |
| **Data de nascimento** | A regra 18+ | Um sinal 18+ de um provedor, sem a data (P1) | Enquanto a conta existir |
| **IP e rota** | Marco Civil, art. 15 | — | 180 dias |
| **Quem leu dado de saúde** | Detectar abuso, responder a incidente | — | Enquanto a conta do titular existir [PENDENTE — prazo máximo] |
| **Ledger de consentimento** | Provar cada consentimento (art. 8º, §2º) | — | [PENDENTE — jurídico: prazo probatório] |
| **Interpretação** | A detecção é relativa ao próprio batimento da pessoa na noite (mediana de 20 min, IQR); limites absolutos só filtram leituras impossíveis (30–250). Nenhum rótulo clínico, nenhuma zona | — | — |

## 5. Análise de alto risco — o teste cumulativo

A Resolução CD/ANPD nº 2/2022 (art. 4º) chama de alto risco o tratamento que
atende **cumulativamente a pelo menos um critério geral e um critério
específico**. A v1.0 do parecer afirmava o alto risco sem o teste; aqui ele
está escrito.

**Critério específico — atendido.** Uso de dado pessoal sensível: frequência
cardíaca é dado referente à saúde (art. 5º, II), e os derivados também,
enquanto ligados à pessoa. Um segundo critério específico é defensável —
tecnologia emergente ou inovadora (leitura de wearable em multidão, detecção
de momentos) —, mas não é necessário para o resultado.

**Critério geral — atendido pela alínea "afetar significativamente
interesses e direitos fundamentais".** Larga escala **não** se aplica hoje
(dezenas de contas, piloto de 3 a 5); volta a ser avaliada no lançamento
público. O critério que se aplica é o potencial de afetar significativamente
direitos fundamentais, por quatro razões:

1. **É dado de saúde de pessoas identificáveis.** A série está ligada a nome e
   e-mail no mesmo banco, sem pseudonimização (RR1). Um vazamento expõe o
   coração de uma pessoa nomeada, minuto a minuto.
2. **Está amarrado a lugar e hora.** A noite diz onde a pessoa estava e quando.
   O contexto do evento pode, ele próprio, revelar outra categoria sensível
   (parecer §2.2) — um ato político, um culto, um evento de uma comunidade —,
   por isso o catálogo é limitado a shows e jogos.
3. **Derivados de saúde são publicados.** Cards e posts do feed tornam público
   um pico, uma hora e um evento. Depois de compartilhado, o card sai do
   controle da TumTum: apagar aqui não apaga o repost de ninguém.
4. **A galera agrega uma multidão no mesmo lugar e no mesmo minuto.** É
   exatamente o formato em que a reidentificação por diferença e por dado
   auxiliar (lista de ingressos, setor) é possível.

**Conclusão:** alto risco. A TumTum se trata como agente que faz tratamento de
alto risco e **não usa as flexibilizações de pequeno porte** da Resolução
2/2022 (por exemplo, a dispensa de encarregado). O Guia de tratamento de alto
risco submetido ao Conselho Diretor não foi aprovado em 21/09/2026, segundo o
parecer; não é usado aqui como orientação consolidada [PENDENTE — conferir a
fonte, item 81].

## 6. Cenários de dano

| # | Cenário | Como aconteceria na TumTum | Probabilidade hoje | Gravidade | Controles (§7) |
|---|---|---|---|---|---|
| **D1** | **Vazamento** | Banco do Railway ou um backup exposto; token forjado; `SECRET_KEY` vazada; celular do piloto perdido; ZIP de operador para o destino errado | Baixa–média | Muito alta: saúde + identidade + lugar | R5, R8, R13, R23; RR1, RR5 |
| **D2** | **Inferência** | Da série se tira mais do que o momento: ritmo em repouso, sinais que alguém leia como condição de saúde, consumo de substâncias numa noite | Baixa (ninguém fora da TumTum lê a série) | Alta | Série bruta 7 dias; nenhum rótulo clínico; nada a terceiros; R11 |
| **D3** | **Discriminação** | Dado vazado ou vendido usado por seguradora, empregador, clube (acesso a evento, prioridade de fã) | Baixa | Muito alta | Nenhuma saída a parceiro comercial (regra de produto 26/09); proibição contratual em qualquer B2B futuro (P2) |
| **D4** | **Reidentificação** | A galera em evento pequeno; diferença entre duas leituras; post no feed com bpm e hora cruzado com quem estava no setor | Média se A galera abrir | Alta | N ≥ 100, célula ≥ 10, faixas; **A galera bloqueada até o teste formal** (singling-out, linkability, inference, differencing); R10 |
| **D5** | **Publicação involuntária** | Card público antes do share; prévia social (OpenGraph) com bpm; notificação na tela de bloqueio com bpm ou evento; share que sai antes de a pessoa confirmar | Baixa depois de 26/09 | Média–alta | `published_at`, `noindex`, prévia com interruptores, notificação neutra (R9, R19, R22) |
| **D6** | **Acesso interno** | Admin lendo noites; o fundador consultando o banco; uma sessão de IA recebendo linhas reais ao investigar; celular de operador com noites de participantes | Média (um só administrador, sem MFA verificado) | Alta | `data_access_log`; leitura só do dono; regra de IA sem dado real; MFA pendente (RR15) |
| **D7** | **Menor de idade** | Um adolescente que mente a data de nascimento | Média | Alta | 18+ recusado no servidor; autodeclaração falsificável (RR2) |
| **D8** | **Terceiros em UGC** | O vídeo atrás do card mostra outras pessoas; a frase de um post cita alguém | Média | Média | Denúncia e bloqueio; vídeo sem GPS; Termos. [PENDENTE — aviso na tela de vídeo] |

## 7. Controles existentes e risco residual

### 7.1 Riscos tratados

Da auditoria de 26/09 (R1–R20) e do parecer v1.1 (R21–R26), com a situação
depois das correções do dia.

| # | Risco | Origem | Probabilidade antes | Impacto | Situação após 26/09 |
|---|---|---|---|---|---|
| R1 | Tratar dado de saúde **sem base legal** (sem consentimento por finalidade nem prova dele) | CR-1 | Certa | Alto | Mitigado: `consents`, sete finalidades, tela própria, histórico por versão |
| R2 | Noite enviada ao servidor **sem ato da pessoa** | CR-2 | Certa | Alto | Mitigado: só por "Guardar minha noite" com `keep_night`; 403 `consent_required` no servidor |
| R3 | Piloto **sem termo** e **sem descarte** | CR-3 | Certa | Alto | Mitigado no papel: `docs/pilot-consent-template.md`, `docs/pilot-data-retention.md`, `purge_event_data.py`. Depende de assinatura e execução |
| R4 | **Menor de idade** usando o app | CR-4 | Média | Alto | Reduzido: data de nascimento pelo seletor, recusa no servidor abaixo de 18 |
| R5 | **Token forjado** por `SECRET_KEY` padrão | CR-5 | Baixa–média | Muito alto | Guarda na inicialização. **Conferir o valor no Railway** |
| R6 | **Dado real** no repositório público | CR-6 | Certa | Médio | Fixtures sintéticos, contatos retirados, `privacy-scan` no CI. O histórico do git ainda os contém |
| R7 | Setup lendo **24 h** do relógio fora de evento | AL-1 | Certa | Médio | Mitigado: 60 min, dito na tela |
| R8 | **Backup automático** do Android levando banco de saúde e tokens | AL-2 | Certa | Alto | Mitigado: `allowBackup="false"`, SQLCipher, tokens criptografados |
| R9 | **Card público** desde a criação, indexável, sobrevivendo à exclusão | AL-3 | Certa | Médio | Mitigado: `published_at`, 404 sem ele, `noindex`, Redis apagado com o card |
| R10 | **Estatística coletiva** reidentificável e sem opt-in | AL-4 | Alta | Médio | Mitigado: `crowd_stats`, N ≥ 100, célula ≥ 10, faixas; bloqueada como produto até o teste formal |
| R11 | Card e telas **afirmando estado emocional** que ninguém mediu | AL-5 | Certa | Baixo–médio | Mitigado: título pelos números da noite |
| R12 | **Sentry** recebendo corpo de requisição com bpm | AL-6 | Média | Médio | Mitigado: sem PII, sem corpo, sem locais, `before_send`. Falta DPA e LIA |
| R13 | Identidade e série no **mesmo banco**, sem pseudonimização | AL-7 | — | Alto em vazamento | **Aberto** (RR1) |
| R14 | **Sem log**, sem plano de incidente, sem encarregado, sem ROPA/RIPD, sem DPA | AL-8 | Certa | Médio | Mitigado: logs, `docs/incident-response.md`, `docs/dpo.md`, `docs/ropa.md`, este documento. DPAs pendentes |
| R15 | **Retenção indefinida** e sem apagar uma noite | AL-9 | Certa | Médio | Mitigado: série bruta **7 dias** (era 30 de manhã); "Apagar esta noite" |
| R16 | `/api/demo/simulate` apagando noites reais e **falsificando presença** | AL-10 | Média | Médio | Mitigado: fora de produção, só admin |
| R17 | **APK de debug** com chave pública instalado por participantes | AL-11 | Média | Alto | Mitigado por regra: participante só pelo Play |
| R18 | Política e rationale **incompletos** e não linkados no app | AL-12 | Certa | Médio | Mitigado: política completa e linkada |
| R19 | App antigo mostrando **bpm na tela de bloqueio** | AL-13 | Baixa | Baixo | Mitigado: sem bpm, `VISIBILITY_PRIVATE` |
| R20 | **Vídeo com GPS** indo para o Story | AL-14 | Média | Médio | Mitigado: remux sem metadados |
| R21 | Série bruta guardada **mais do que a necessidade** | Parecer §11 | Certa | Médio | Mitigado: 30 → **7 dias** |
| R22 | Notificação da revelação **nomeando o evento** na tela de bloqueio | Parecer §17.1, §18 | Certa | Baixo–médio | Mitigado: *"Sua noite abriu"*, sem evento nem bpm |
| R23 | **Refresh token do site em `localStorage`**, legível por qualquer script da página (90 dias de acesso a dado de saúde por um XSS) | Parecer §10, §18 | Baixa | Alto | Mitigado: cookie `httpOnly` |
| R24 | Ledger **sem base legal, escopo nem prova** por linha | Parecer §4.2 | Certa | Médio | Mitigado: `legal_basis`, `scope`, `proof`. Falta separar do banco de saúde e o prazo probatório |
| R25 | Incidente não comunicado **sem registro** | Parecer §12, Res. 15/2024 | Certa | Médio | Mitigado: `docs/incident-register.md`, 5 anos |
| R26 | **Encarregado em conflito de interesse** | Parecer §6, Res. 18/2024 | Certa | Médio | **Aberto** (RR10) |

### 7.2 Riscos residuais

| # | Risco residual | Por que fica | O que o reduziria | Quando |
|---|---|---|---|---|
| RR1 | Vazamento do banco expõe identidade **e** série juntas | Sem pseudonimização; mesmo schema, mesma credencial | Identidade em schema separado, role distinta, `subject_id` opaco nas tabelas de saúde | P1 |
| RR2 | Menor que mente a data de nascimento | Autodeclaração | Provedor que devolva só o sinal 18+, sem documento guardado pela TumTum (parecer §8) | P1 |
| RR3 | Chave de debug pública permite "update" malicioso de um APK de debug | A chave está no repositório | Só Play para participantes (regra); tirar a chave quando o APK de debug deixar de ser necessário | — |
| RR4 | Dado real no **histórico** do git | Retirado da árvore, não do histórico | `git filter-repo` ou aceitar e registrar [PENDENTE — Felipe] | item 78 |
| RR5 | **Backup ressuscita** o que foi apagado | Prazo do Railway não conferido; e, numa restauração, **não há como saber quem refazer**: o `deletion_log` não tem identificador e os pedidos do titular saem com a conta | TTL curto e documentado + tombstone de exclusão (`docs/data-retention-policy.md`) | P1 |
| RR6 | Operadores sem contrato e região desconhecida | DPAs não assinados | `docs/dpa-checklist.md` | Antes do piloto |
| RR7 | Post apagado fica com `deleted_at` (bpm incluso). *(Revogar `keep_night` já apaga as noites, 24 h depois — `services/maintenance.py`.)* | Decisão aberta | `docs/ropa.md` §4 | item 78 |
| RR8 | Categoria "Atividade e condicionamento físicos" declarada ao Google | Resposta do Play Console | `docs/play-console-answers.md`, revisão jurídica | item 76 |
| RR9 | Noites de antes de 26/09 sem `keep_night` e sem `analyzed_at` | Produção não roda Alembic | `backend/alembic/README-migrations.md` | item 74 |
| RR10 | Encarregado que decide o que ele mesmo fiscaliza | Uma pessoa só na empresa | Encarregado externo ou pessoa sem poder decisório (`docs/dpo.md`) | item 80 |
| RR11 | Transferência internacional sem mecanismo | Regiões e suboperadores não mapeados | Transfer Register completo | P1 |
| RR12 | Ledger no mesmo banco que o dado de saúde, apagado com a conta | Desenho de 26/09 | Ledger separado, com prazo probatório | item 82 |
| RR13 | Sem MFA verificado nos painéis de produção (Railway, Vercel, GitHub, Sentry, Resend) nem cofre de segredos | Não conferido | MFA em todos; segredos só no cofre do provedor | P1 |
| RR14 | Sem pentest | Nunca feito | Pentest antes do público | P1 |
| RR15 | Um único administrador, sem revisão de acesso | Tamanho da empresa | Contas individuais, revisão trimestral quando houver mais de uma pessoa | P1 |

## 8. Alternativas menos invasivas consideradas

| Alternativa | Decisão | Por quê |
|---|---|---|
| **Nenhum servidor**: a noite vive só no celular | **Adotada como padrão**, rejeitada como único modo | Sem "guardar a noite", nada sobe (O6). Como único modo, mataria a coleção entre aparelhos, o feed do evento e a análise do servidor |
| **Não guardar a série bruta no servidor**, só os momentos calculados no celular | Rejeitada por ora; **o prazo caiu para 7 dias** | O detector validado roda no servidor e a reanálise depois de um conserto precisa da série. Revisitar quando o detector do aparelho for o mesmo do servidor |
| **Sem feed** | Rejeitada | O feed é parte da hipótese do produto (sentir junto). Mitigado: cada post é um ato, só quem tem noite medida no evento vê, apagar, denunciar, bloquear. Rever depois do piloto |
| **A galera por N mínimo apenas** | Rejeitada | N não prova anonimato (parecer §15). Bloqueada até o teste formal |
| **Idade por documento ou CPF** | Rejeitada | Criaria uma base de documentos mais arriscada que o problema. Data de nascimento no piloto; sinal 18+ de provedor no lançamento |
| **Página pública de card por padrão** | Rejeitada | Só depois de compartilhado (`published_at`), com `noindex` |
| **SDK de analytics** | Rejeitada | Nenhum dado de uso sai; nenhum BPM em telemetria |
| **R-R e movimento no servidor** | Rejeitada | Só o filtro do celular precisa deles |
| **Contagem exata na A galera** | Rejeitada | Faixas |
| **Série bruta por 30 dias** | Substituída | 7 dias (§4) |

## 9. Transferências internacionais e suboperadores

[PENDENTE] — hospedar no Brasil não é exigência da LGPD, e região brasileira
não prova ausência de transferência: suporte, observabilidade, CDN e e-mail
podem acessar dados no exterior (parecer §13). Cada fornecedor, a região, o
acesso remoto, os suboperadores e o mecanismo do art. 33 (Resolução CD/ANPD
nº 19/2024) entram no **Transfer Register** de `docs/dpa-checklist.md`. Até
ele estar cheio, este item fica aberto e é o RR11.

## 10. Gatilhos de revisão

Este relatório é revisto, **antes** de ir ao ar, quando acontecer qualquer um:

- **Card novo** — "Ver o momento" (mídia licenciada), A galera aberta ao
  público, "Na mesma vibe".
- **País novo** — usuários, operador ou acesso remoto fora do Brasil.
- **Fonte nova** — Apple Watch (HealthKit), app de relógio Wear OS, pulseira
  J-Style, qualquer sensor novo.
- **Parceiro novo** — clube, produtora, artista, qualquer B2B.
- **Algoritmo** — mudança nos parâmetros do detector (os de `CLAUDE.md`), um
  score novo, qualquer decisão automatizada que afete a pessoa.
- **Incidente** — todo incidente com risco relevante (a probabilidade virou
  fato).
- **Mudança regulatória** — o regulamento de RIPD ou de direitos dos titulares
  da Agenda Regulatória, uma resolução nova.
- **Operador novo, prazo novo, dado novo, tipo de evento novo.**
- **Periodicamente**, a cada 6 meses.

## 11. Conclusão (provisória)

Com as correções de 26/09 implantadas e as pendências de configuração do
Railway conferidas (`SECRET_KEY`, `DATABASE_SSL`, Sentry, backups, região), o
risco residual é **aceitável para um piloto de 3 a 5 pessoas com termo
assinado**, desde que os DPAs (RR6) estejam fechados. **Não é aceitável para o
lançamento público** sem RR1, RR2, RR5, RR11, RR13 e RR14.

Assinatura do responsável: ______________________ Data: ____/____/________
