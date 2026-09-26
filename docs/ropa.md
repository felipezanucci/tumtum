# Registro das operações de tratamento (ROPA)

Art. 37 da LGPD. Versão de 26/09/2026, escrita junto com a remediação da
auditoria (`RELATORIO-AUDITORIA-LGPD.md`, item L1) e descrevendo o sistema
**como ele fica com essa remediação** (o contrato de 26/09: consentimento por
finalidade, "guardar a noite" como ato, 18+, retenção, logs). Revisar a cada
finalidade nova, operador novo ou mudança de prazo — e registrar a revisão no
`docs/decision-log.md`.

- **Controlador:** [PENDENTE — Felipe: razão social e CNPJ]
- **Encarregado:** Felipe Zanucci — oi@tumtum.cc, assunto "Privacidade"
  (`docs/dpo.md`)
- **Titulares:** pessoas com conta na TumTum (18+), participantes do piloto,
  pessoas na lista de espera do site.
- **Dado sensível:** batimento cardíaco é dado de saúde (art. 5º, II). Toda
  operação que o toca tem como base o **consentimento específico e destacado
  do art. 11, I**, dado por finalidade e gravado na tabela `consents` com a
  versão do texto (`CONSENT_TEXT_VERSION = "2026-09-26"`).

## 1. As operações

| # | Operação (chave em `consents`) | Dados | Base legal | Retenção | Onde |
|---|---|---|---|---|---|
| 1 | **Conta**: cadastro, login, sessão, troca de e-mail, recuperação de senha (`terms` registra o aceite) | Nome, e-mail, senha (hash bcrypt), data de nascimento, código de 6 dígitos (hash), refresh token (hash), avatar (**só no celular**) | Execução de contrato (art. 7º, V). A data de nascimento serve à regra 18+ dos Termos | Enquanto a conta existir. Códigos e tokens: apagados 24 h após expirar. Cadastro não confirmado: 1 dia | Railway (Postgres) |
| 2 | **Ler o batimento** e gerar o momento (`read_heart_rate`) | bpm com data e hora, na janela do evento ±30 min (relógio, via Health Connect) ou do "Começar" ao fim (cinta BLE); R-R e movimento do celular durante a captura | Consentimento, art. 11, I | No celular: a noite (bpm e momentos) até a pessoa apagar a noite ou a conta. R-R, movimento e o log bruto da cinta: apagados quando a noite é guardada. Banco local criptografado (SQLCipher), fora do backup do Android | Só no celular |
| 3 | **Guardar a noite** na coleção (`keep_night`), incluindo a importação de arquivo Polar pelo site | `hr_sessions` (evento, início, fim, média/máx/mín, qualidade, fonte), `hr_data` (bpm por instante), `peaks`, `cards` | Consentimento, art. 11, I — e só por um toque em "Guardar minha noite na TumTum" | Série bruta (`hr_data`): **30 dias** após os momentos serem encontrados (`analyzed_at`). Momentos, resumo e cards: enquanto a conta existir e `keep_night` estiver ativo. Imagem do card em cache: 7 dias | Railway (Postgres, Redis) |
| 4 | **Estatística coletiva** "A galera" (`crowd_stats`) | Momentos de quem consentiu, agregados por evento | Consentimento, art. 11, I | Nada guardado à parte: calculado na leitura. Publicado só com ≥ 100 noites no evento e ≥ 10 pessoas por momento, em faixas (`10+`…`250+`), nunca contagem exata | Railway |
| 5 | **Comparar com artista/atleta** (`artist_compare`) | — (não existe ainda) | Consentimento, art. 11, I | O consentimento é colhido; nenhum tratamento acontece até a função existir e este registro ser revisto | — |
| 6 | **Melhorar o detector** (`improve_detection`) | Noites de quem consentiu | Consentimento, art. 11, I | Nenhum processo usa hoje. Quando existir: só noites com o consentimento ativo, e a revisão entra aqui antes | — |
| 7 | **Comunicações por e-mail** (`marketing`) | E-mail, nome | Consentimento, art. 7º, I | Até revogar | Resend |
| 8 | **Feed do evento**: posts, SENTI, turnê | `event_posts` (bpm, hora do momento, rótulo, frase, skin), `event_post_reactions`, `series_posts` | Consentimento, art. 11, I — cada post é um ato explícito, visto só por quem tem noite medida no evento | Até a pessoa apagar o post ou a conta. **Post apagado hoje fica com `deleted_at`** (ver pendências) | Railway |
| 9 | **Moderação**: denúncias e bloqueios | `post_reports` (motivo, resolução), `user_blocks` | Execução de contrato (art. 7º, V — regras de convivência dos Termos) | Enquanto a conta de quem denunciou/bloqueou existir | Railway |
| 10 | **Lista de espera** do site | E-mail, nome e sobrenome (opcionais), página de origem | Consentimento, art. 7º, I | Até a pessoa pedir para sair; sai também com a conta do mesmo e-mail | Railway |
| 11 | **Registro de acesso a aplicação** (`access_log`) | IP, método, rota, status, horário, id da conta | Cumprimento de obrigação legal, art. 7º, II (Marco Civil, art. 15: 6 meses) | **180 dias** | Railway |
| 12 | **Registro de quem leu dado de saúde** (`data_access_log`) | Quem leu, de quem, qual recurso, ação, IP, horário. **Sem bpm** | Cumprimento de obrigação legal, art. 7º, II, c/c arts. 37 e 46 | Enquanto a conta do titular existir; sai na exclusão | Railway |
| 13 | **Pedidos de titular e suporte** (`data_subject_requests`, caixa oi@tumtum.cc) | Tipo do pedido, mensagem, resposta, prazos; e-mails recebidos | Cumprimento de obrigação legal, art. 7º, II (arts. 18 e 19) | Tabela: enquanto a conta existir. Caixa de e-mail: [PENDENTE — Felipe: prazo] | Railway; [PENDENTE — provedor da caixa oi@tumtum.cc] |
| 14 | **Prova de exclusão** (`deletion_log`) | Só a data. **Nenhum identificador** | Não é dado pessoal | Indefinida | Railway |
| 15 | **Monitoramento de erros** | Stack trace, rota, ambiente. Configurado sem PII: `send_default_pii=False`, sem corpo de requisição, sem cookies, sem `Authorization`, sem variáveis locais | Legítimo interesse, art. 7º, IX — possível **só** porque a configuração exclui o dado de saúde | Retenção do plano: [PENDENTE — conferir no Sentry] | Sentry, se `SENTRY_DSN` estiver definido |
| 16 | **Piloto**: termo em papel, planilha de controle, ZIPs de operador | Nome, data de nascimento, escolhas; série crua de uma noite (ZIP) | Consentimento, art. 11, I (termo) | Dados do piloto: 30 dias após o evento (`docs/pilot-data-retention.md`). Termo: 5 anos após o descarte | Fora do repositório |

## 2. Operadores

Nenhum dado sai para clube, artista, organizador, patrocinador ou anunciante.
Os únicos que recebem dado pessoal são os operadores abaixo, e só para fazer o
serviço funcionar.

| Operador | O que faz | Dados que passam | Região | Transferência internacional | Contrato (DPA) |
|---|---|---|---|---|---|
| **Railway** | API (FastAPI), Postgres, Redis | Todos os das operações 1, 3, 4, 8–14 | [PENDENTE — região de cada operador] | [PENDENTE — se fora do Brasil: mecanismo do art. 33 (cláusulas-padrão da ANPD, Res. CD/ANPD nº 19/2024)] | [PENDENTE] (`docs/dpa-checklist.md`) |
| **Vercel** | Site tumtum.cc e web app | Requisições do navegador (IP), páginas públicas de card | [PENDENTE — região de cada operador] | [PENDENTE] | [PENDENTE] |
| **Resend** | E-mails de código, senha, troca de e-mail | E-mail, nome, código | [PENDENTE — região de cada operador] | [PENDENTE] | [PENDENTE] |
| **Sentry** | Erros do backend | Operação 15 | [PENDENTE — região de cada operador] | [PENDENTE] | [PENDENTE] |
| **Redis (no Railway)** | Cache de imagem de card | Imagem do card (nome, bpm, hora, evento), 7 dias | A do Railway | A do Railway | Coberto pelo DPA do Railway |
| Provedor da caixa oi@tumtum.cc | Suporte, pedidos | E-mails dos titulares | [PENDENTE — Felipe: qual provedor e região] | [PENDENTE] | [PENDENTE] |
| Cloudflare | DNS; CDN/proxy se ligado | IP, se o proxy estiver ligado | [PENDENTE — conferir se o tráfego de tumtum.cc passa pelo proxy] | [PENDENTE] | [PENDENTE] |

**Não são operadores de dado pessoal** (conferido no código, 26/09):
API-Football (a TumTum pergunta pelo jogo, nada sobre a pessoa); Google
Health Connect (roda no celular da pessoa, a TumTum lê dele e nada volta ao
Google por nós); Google Play (distribui o app); GitHub (hospeda o código, que
não carrega dado de titular — o job `privacy-scan` garante). Nenhum SDK de
anúncio ou de analytics existe em nenhum dos três códigos.

## 3. Medidas de segurança

- **Em trânsito:** HTTPS em produção (app, site, API). Conexão da API ao banco
  com TLS quando `DATABASE_SSL=true` ([PENDENTE — Felipe: confirmar no
  Railway]).
- **Chave de assinatura dos tokens:** a API não sobe com `SECRET_KEY` de
  exemplo ou com menos de 32 caracteres. Access token de 1 h; refresh token de
  90 dias, rotacionado, com detecção de reuso (a família inteira é revogada).
- **Senhas e códigos** só como hash.
- **Controle de acesso:** toda leitura de noite, momento e card checa o dono;
  card público só depois de compartilhado (`published_at`), com `noindex`;
  `/api/demo` não existe em produção.
- **Registro:** `data_access_log` para cada leitura de dado de saúde e
  exportação; `access_log` de toda requisição, 180 dias.
- **Minimização:** R-R e movimento nunca sobem; série bruta apagada em 30 dias;
  o nome do titular não é copiado dentro do card; export de operador sem MAC
  do sensor e sem identificador no nome do arquivo.
- **No celular:** banco Room criptografado (SQLCipher, chave no
  `EncryptedSharedPreferences`), tokens criptografados, `allowBackup="false"`,
  arquivos temporários de card apagados após o compartilhamento e na exclusão
  da conta, vídeo sem metadados (GPS) antes de ir para um Story, notificação
  sem bpm.
- **Monitoramento de erros** sem corpo de requisição nem PII.
- **Repositório público** sem dado pessoal nem segredo (`privacy-scan` no CI).
- **Resposta a incidente:** `docs/incident-response.md`.

## 4. Pendências deste registro

- [PENDENTE — Felipe] controlador (razão social, CNPJ).
- [PENDENTE — região de cada operador] e o mecanismo de transferência
  internacional quando for fora do Brasil.
- [PENDENTE — Felipe] DPAs assinados/baixados (`docs/dpa-checklist.md`).
- [PENDENTE — decisão] revogar `keep_night` apaga as noites já guardadas, ou
  só impede as próximas? Hoje o contrato só impede as próximas; a pessoa apaga
  as antigas com "Apagar esta noite". O art. 16 pede a eliminação ao fim do
  tratamento: a leitura mais segura é apagar.
- [PENDENTE — decisão] post do feed apagado continua na tabela com
  `deleted_at` (bpm incluso). Definir remoção física (sugestão: no ciclo de
  manutenção, 30 dias depois).
- [PENDENTE — decisão] prazo máximo do `data_access_log` enquanto a conta
  existe (sugestão: 180 dias, como o `access_log`).
