# Relatório de Impacto à Proteção de Dados Pessoais (RIPD) — rascunho

Art. 5º, XVII, e art. 38 da LGPD. **Rascunho de 26/09/2026**, escrito com a
remediação da auditoria (`RELATORIO-AUDITORIA-LGPD.md`, item L2) e
descrevendo o sistema como ele fica com ela. Não foi revisado por advogado:
[PENDENTE — Felipe: revisão jurídica].

- **Controlador:** [PENDENTE — Felipe: razão social e CNPJ]
- **Encarregado e responsável por este relatório:** Felipe Zanucci
  (oi@tumtum.cc, assunto "Privacidade")
- **Próxima revisão:** antes do primeiro participante do piloto que não seja o
  fundador, e depois a cada 6 meses, ou antes, a cada finalidade nova
  (a comparação com artista, "Ver o momento", um app de relógio) ou operador
  novo. Data prevista: **[PENDENTE — data do piloto]**; em seguida
  **26/03/2027**.

## 1. Descrição do tratamento

A TumTum lê o batimento cardíaco de uma pessoa durante um show ou um jogo —
do relógio dela (Health Connect, no celular) ou de uma cinta Bluetooth —,
encontra os momentos em que ele subiu em relação ao próprio batimento da
pessoa naquela noite, liga cada momento ao que estava acontecendo (a música, o
gol) e gera um card que ela pode compartilhar.

- **Natureza:** dado pessoal sensível (saúde, art. 5º, II), em série
  temporal, ligado a identidade (nome, e-mail) e a presença em um evento
  (lugar e hora).
- **Escala:** MVP. Algumas dezenas de contas; piloto de 3 a 5 pessoas.
- **Contexto:** entretenimento, não saúde. A pessoa espera ver "o que ela
  sentiu", não uma avaliação do coração.
- **Ciclo:** captura no celular → (só com "guardar a noite") envio ao
  servidor → detecção de momentos → card → (só por toque) compartilhamento ou
  post no feed do evento → série bruta apagada em 30 dias → momentos e cards
  até a pessoa apagar ou revogar.
- **Finalidades e bases:** `docs/ropa.md`.

## 2. Necessidade e proporcionalidade

| Pergunta | Resposta |
|---|---|
| O batimento é necessário? | Sim: sem ele não há produto. Mas só **bpm com hora**; nenhuma outra medida de saúde é lida (sem sono, calorias, VO2, SpO2) |
| A janela é mínima? | Relógio: evento ±30 min. Setup: últimos 60 minutos, uma vez, para medir a densidade do relógio, sem enviar nada. Cinta: do "Começar" ao fim |
| R-R e movimento são necessários no servidor? | Não. Servem ao filtro de "fora da pele" no celular e ficam lá; o servidor não aceita nem devolve mais |
| A série bruta precisa ficar para sempre? | Não. Momentos e resumo bastam para o card; a série bruta sai 30 dias após a análise |
| O card precisa revelar tudo? | Não. Antes de compartilhar, o app diz o que o card mostra (*"Esse card mostra seu pico, a hora e o evento."*), e hora, evento e bpm exato têm interruptor (bpm arredondado à dezena quando desligado). O nome não é mais copiado dentro do card no servidor |
| A estatística coletiva precisa de contagem exata? | Não: faixas (`10+`…`250+`), mínimo de 100 noites e 10 pessoas por momento, só de quem consentiu |
| A detecção interpreta saúde? | Não. É relativa ao próprio batimento da pessoa na noite (mediana de 20 min, IQR); limites absolutos só filtram leituras impossíveis (30–250). Nenhum rótulo clínico |
| Há IA externa, anúncio, analytics? | Não, em nenhum dos três códigos |

## 3. Riscos identificados

Da auditoria de 26/09 (críticos e altos), com a situação depois da
remediação.

| # | Risco | Origem (auditoria) | Probabilidade antes | Impacto | Situação após 26/09 |
|---|---|---|---|---|---|
| R1 | Tratar dado de saúde **sem base legal** (sem consentimento por finalidade nem prova dele) | CR-1 (A2, A3) | Certa | Alto | Mitigado: tabela `consents`, sete finalidades, tela própria, histórico por versão |
| R2 | Noite enviada ao servidor **sem ato da pessoa**, contra a promessa do app | CR-2 | Certa | Alto | Mitigado: só por "Guardar minha noite" com `keep_night`; 403 `consent_required` no servidor |
| R3 | Piloto **sem termo** e **sem descarte** | CR-3 | Certa | Alto | Mitigado no papel: `docs/pilot-consent-template.md`, `docs/pilot-data-retention.md`, `purge_event_data.py`. Depende de assinatura e execução |
| R4 | **Menor de idade** usando o app | CR-4 | Média | Alto | Reduzido: data de nascimento pelo seletor, recusa no servidor abaixo de 18. Autodeclaração continua falsificável |
| R5 | **Token forjado** por `SECRET_KEY` padrão → leitura de qualquer conta | CR-5 | Baixa–média (depende do Railway) | Muito alto | Guarda na inicialização. **Conferir o valor no Railway** |
| R6 | **Dado real** no repositório público | CR-6 | Certa | Médio | Fixtures sintéticos, e-mails e contatos retirados dos docs, `privacy-scan` no CI. O histórico do git ainda os contém |
| R7 | Setup lendo **24 h** do relógio fora de evento | AL-1 | Certa | Médio | Mitigado: 60 min, dito na tela |
| R8 | **Backup automático** do Android levando banco de saúde e tokens | AL-2 | Certa | Alto | Mitigado: `allowBackup="false"`, SQLCipher, tokens criptografados |
| R9 | **Card público** desde a criação, indexável, sobrevivendo à exclusão | AL-3 | Certa | Médio | Mitigado: `published_at`, 404 sem ele, `noindex`, Redis apagado com o card |
| R10 | **Estatística coletiva** reidentificável (células de 2 pessoas) e sem opt-in | AL-4 | Alta | Médio | Mitigado: `crowd_stats`, N ≥ 100, célula ≥ 10, faixas |
| R11 | Card e telas **afirmando estado emocional** que ninguém mediu | AL-5 | Certa | Baixo–médio | Mitigado: título pelos números da noite; "Picos de Emoção" e afins retirados |
| R12 | **Sentry** recebendo corpo de requisição com bpm | AL-6 | Média | Médio | Mitigado: sem PII, sem corpo, sem locais, `before_send`. Falta DPA |
| R13 | Identidade e série no **mesmo banco**, sem pseudonimização | AL-7 | — | Alto em caso de vazamento | **Aberto** (ver residuais) |
| R14 | **Sem log** de acesso, sem plano de incidente, sem encarregado, sem ROPA/RIPD, sem DPA | AL-8 | Certa | Médio | Mitigado: `data_access_log`, `access_log` 180 d, `docs/incident-response.md`, `docs/dpo.md`, `docs/ropa.md`, este documento. DPAs pendentes |
| R15 | **Retenção indefinida** e sem apagar uma noite | AL-9 | Certa | Médio | Mitigado: 30 dias para a série bruta; `DELETE /api/health/sessions/{id}` e "Apagar esta noite" |
| R16 | `/api/demo/simulate` apagando noites reais e **falsificando presença** | AL-10 | Média | Médio | Mitigado: fora de produção, só admin, sessões sintéticas ignoradas |
| R17 | **APK de debug** com chave pública instalado por participantes | AL-11 | Média | Alto | Mitigado por regra: participante só pelo Play; release body diz. A chave continua no repositório |
| R18 | Política e rationale **incompletos** e não linkados no app | AL-12 | Certa | Médio | Mitigado: política completa, links no cadastro e em Configurações, rationale do Health Connect |
| R19 | App antigo mostrando **bpm na tela de bloqueio** | AL-13 | Baixa (app aposentado) | Baixo | Mitigado: sem bpm, `VISIBILITY_PRIVATE`, README "não é para participantes" |
| R20 | **Vídeo com GPS** indo para o Story | AL-14 | Média | Médio | Mitigado: remux sem metadados |

## 4. Medidas adotadas

As do contrato de 26/09, resumidas em `docs/ropa.md` §3. Em uma linha cada:
consentimento por finalidade registrado e revogável; upload só por ato;
18+ no servidor; retenção de 30 dias para a série bruta e de 180 para logs;
apagar uma noite; exportar tudo (JSON e CSV); pedidos de titular com prazo de
15 dias; registro de quem leu dado de saúde; card publicado só por ato;
coletivo com N mínimo e faixas; Sentry sem PII; banco do celular
criptografado e fora de backup; `SECRET_KEY` guardada; documentação de
governança (este relatório, ROPA, retenção, backups, incidente, encarregado,
DPAs, termo e descarte do piloto).

## 5. Riscos residuais

| # | Risco residual | Por que fica | O que o reduziria |
|---|---|---|---|
| RR1 | Vazamento do banco expõe identidade **e** série juntas | Sem pseudonimização (R13); mesmo schema, mesma credencial | Schema separado para identidade, role de banco distinta, `subject_id` opaco nas tabelas de saúde |
| RR2 | Menor que mente a data de nascimento | Autodeclaração | Para o lançamento público: vínculo com ingresso nominal ou verificação externa |
| RR3 | Chave de debug pública permite "update" malicioso de um APK de debug | A chave está no repositório e no histórico | Só Play para participantes (já é regra); tirar a chave do repositório quando o APK de debug deixar de ser necessário |
| RR4 | Dado real no **histórico** do git | Retirado da árvore, não do histórico | Reescrever o histórico (`git filter-repo`) ou aceitar e registrar. [PENDENTE — Felipe: decidir] |
| RR5 | Backups do Railway guardam o que já foi apagado | Prazo e criptografia não verificados | `docs/backups.md` |
| RR6 | Operadores sem contrato e região desconhecida | DPAs não assinados | `docs/dpa-checklist.md` |
| RR7 | Revogar `keep_night` não apaga as noites já guardadas; post apagado fica com `deleted_at` | Decisões ainda abertas | `docs/ropa.md` §4 |
| RR8 | Categoria "Atividade e condicionamento físicos" declarada ao Google contradiz a marca e pode puxar leitura de app de saúde | Resposta do Play Console | `docs/play-console-answers.md`, revisão jurídica |
| RR9 | Noites de antes de 26/09 sem consentimento `keep_night` e sem `analyzed_at` (a retenção de 30 dias não as alcança); colunas de token de wearable ainda no banco | Produção não roda Alembic (decision log, item 16); o startup só **acrescenta** colunas (`core/schema_catchup.py`) | Rodar `backend/alembic/README-migrations.md` (stamp, 022, back-fill de `analyzed_at`) e decidir: pedir consentimento no portão ou apagar essas noites |

## 6. Conclusão (provisória)

Com a remediação implantada e as pendências de configuração do Railway
conferidas (`SECRET_KEY`, `DATABASE_SSL`, Sentry, backups, região), o risco
residual é **aceitável para um piloto de 3 a 5 pessoas com termo assinado**.
Não é aceitável para o lançamento público sem RR1, RR2 e RR6.

Assinatura do responsável: ______________________ Data: ____/____/________
