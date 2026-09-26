# Contratos com operadores (DPA) — checklist

26/09/2026. A auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, item H6) achou
quatro operadores recebendo dado pessoal sem nenhum contrato de tratamento
referenciado, e sem região conhecida. Os quatro publicam um DPA (acordo de
tratamento de dados) padrão; na maioria dos planos ele já vale por referência
nos termos de uso, mas **é preciso baixar a versão vigente, guardar, e anotar
a região** — sem isso o `docs/ropa.md` não fecha.

**Ampliado em 26/09 (noite)** pelo parecer v1.1 (§13, §14, §14.1): além de
baixar os DPAs, a TumTum mantém um **Transfer Register** (quem recebe o quê,
de onde para onde, com qual mecanismo), responde às perguntas obrigatórias por
tipo de fornecedor e confere, em cada DPA, as cláusulas mínimas. Hospedar no
Brasil **não** é exigência da LGPD, e região brasileira não prova que não há
transferência: suporte, observabilidade, CDN e e-mail podem acessar de fora.

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

## Transfer Register (parecer §13)

Um fornecedor pode ser operador para uma finalidade e controlador
independente para outra; cada linha é o papel **real** dele na TumTum. Pela
Resolução CD/ANPD nº 19/2024, toda transferência para fora do Brasil precisa de
uma base legal (art. 7º ou 11) **e** de um mecanismo válido do art. 33 —
decisão de adequação quando houver, cláusulas-padrão da ANPD, normas
corporativas globais ou outra hipótese cabível; as cláusulas-padrão não são a
única saída — e de transparência específica na política.

| Fornecedor / suboperador | Papel | Dados | Origem / destino (país e acesso remoto) | Finalidade | Base legal | Mecanismo (art. 33) | Transferência posterior | Segurança | Transparência | Owner / revisão |
|---|---|---|---|---|---|---|---|---|---|---|
| **Railway** | Operador | Tudo o que sobe: conta, ledger, noites guardadas (saúde), momentos, cards, feed, logs | Brasil → [PENDENTE — região do serviço, do Postgres e dos backups; suporte com acesso remoto de onde?] | Hospedar a API, o banco e o cache | As das operações O1–O21 (`docs/ropa.md`) | [PENDENTE] | [PENDENTE — suboperadores (nuvem por baixo)] | TLS [PENDENTE — `DATABASE_SSL`]; criptografia em repouso [PENDENTE]; MFA no painel [PENDENTE] | [PENDENTE — a política diz país e mecanismo?] | Felipe / [PENDENTE] |
| **Vercel** | Operador | Requisições do navegador (IP), páginas, arquivo Polar na importação, cookie de sessão | Brasil → [PENDENTE — região das funções; `gru1` existe] | Servir o site e o web app | O1, O3, O7, O8, O13, O15 | [PENDENTE] | [PENDENTE] | TLS; MFA [PENDENTE] | [PENDENTE] | Felipe / [PENDENTE] |
| **Resend** | Operador | E-mail, nome, código; aviso de denúncia aos operadores (nome do evento) | Brasil → [PENDENTE — região do domínio `mail.tumtum.cc`] | Enviar e-mails de código, senha, troca de e-mail | O2, O4, O5, O12 | [PENDENTE] | [PENDENTE] | TLS; nenhum bpm em template | [PENDENTE] | Felipe / [PENDENTE] |
| **Sentry** | Operador | Stack trace, rota, ambiente; **sem PII, corpo ou locais** | Brasil → [PENDENTE — US ou EU, escolhido na criação] | Erros do backend | O22 (legítimo interesse, LIA pendente) | [PENDENTE] | [PENDENTE] | `send_default_pii=False`, `before_send` | [PENDENTE] | Felipe / [PENDENTE] |
| **Cloudflare** | Operador, se o proxy estiver ligado; senão só DNS | IP e requisição, se proxy | [PENDENTE — proxy ligado?] | DNS; CDN | — | [PENDENTE] | [PENDENTE] | [PENDENTE] | [PENDENTE] | Felipe / [PENDENTE] |
| **Provedor da caixa oi@tumtum.cc** | Operador | E-mails dos titulares; pedidos; o que a pessoa escrever | [PENDENTE — qual provedor, qual região] | Suporte e pedidos de titular | O19, O20 | [PENDENTE] | [PENDENTE] | MFA [PENDENTE] | [PENDENTE] | Felipe / [PENDENTE] |
| Google (Health Connect, Play) | **Não é operador da TumTum.** Health Connect roda no celular da pessoa; Play distribui o app | Nada enviado pela TumTum | — | — | — | Não se aplica | — | — | A política diz de onde o dado vem | Felipe |
| GitHub | Não é operador: hospeda código **sem dado de titular** (`privacy-scan`) | Nenhum dado pessoal | — | Código, CI, releases | — | Não se aplica enquanto não houver dado | — | `privacy-scan`; MFA [PENDENTE] | — | Felipe |
| Anthropic (sessões do Claude Code) | Não é operador **enquanto não receber dado real** — ferramenta de desenvolvimento | Código e docs; **nunca linhas de produção** | — | Desenvolvimento | — | Se um dia receber dado pessoal: vira operador, e entra aqui com tudo antes | — | Regra de `docs/incident-response.md` §1 | — | Felipe |
| API-Football | Não é operador: a TumTum pergunta pelo jogo | Nenhum dado pessoal | — | Linha do tempo do jogo | — | Não se aplica | — | — | — | Felipe |

## Perguntas por tipo de fornecedor (parecer §14)

| Tipo | Quem, na TumTum | Perguntas obrigatórias | Respostas |
|---|---|---|---|
| **Nuvem / banco** | Railway; Vercel | Onde ficam os dados e os backups? Quem acessa? Quais suboperadores? Suporte internacional com acesso aos dados? | [PENDENTE] (`docs/backups.md`) |
| **E-mail / CRM** | Resend; provedor da caixa | Recebe dado sensível? Templates incluem BPM ou evento? Usa o conteúdo para treinar modelo? | Resend: códigos, senha e troca de e-mail **sem bpm nem evento**; o aviso de denúncia aos operadores leva o **nome do evento**, sem bpm nem autor (conferido no código, 26/09). Nenhum e-mail de marketing sai hoje. Treinamento: [PENDENTE]. Caixa: [PENDENTE] |
| **Analytics / crash** | Sentry. **Nenhum analytics** | Payloads automáticos? IP ou device ID? Session replay? Redaction? | Sem PII, sem corpo, sem locais, `before_send`; sem replay (é o SDK de backend). IP: [PENDENTE — conferir `send_default_pii=False` no painel] |
| **Age assurance** | **Nenhum hoje** (data de nascimento autodeclarada). Entra no lançamento público | Recebe documento? Retém? Reutiliza? Devolve só 18+? Em que país? | Critério para escolher: só o sinal 18+ volta; nenhum documento na TumTum |
| **Wearable / API** | Health Connect (no celular); Polar (cinta BLE; arquivo Polar Flow importado pela pessoa) | Quem controla a origem? Termos? Permissões e revogação? | Health Connect: a pessoa, pela permissão do sistema, revogável lá. Polar: a TumTum não fala com a nuvem da Polar; o arquivo vem da pessoa |
| **Render / CDN** | Card gerado no Railway, imagem no Redis; página pública no Vercel; Cloudflare se proxy | Arquivos públicos? TTL? Purge de cache? Logs de URL? | Página só com `published_at`, `noindex`; imagem 7 dias, apagada com o card. Cache de CDN e purge ao despublicar: [PENDENTE — conferir Vercel e Cloudflare] |
| **IA / dev** | Sessões do Claude Code; GitHub | Dado real bloqueado? Retenção? Treinamento? Controles empresariais? | Dado real bloqueado por regra (P0.12) e por `privacy-scan`. Retenção e treinamento do plano usado: [PENDENTE — Felipe] |
| **Suporte** | Felipe, pela caixa e pelo admin | Acesso a produção? Screenshots? Exportação? Confidencialidade? | Uma pessoa; nenhum print com bpm e identidade fora da TumTum; nunca pedir série bruta |

## Cláusulas mínimas do DPA (parecer §14.1)

Conferir em cada DPA baixado. `S` = tem; `N` = não tem; `?` = não conferido.

| Cláusula | Railway | Vercel | Resend | Sentry | Caixa |
|---|---|---|---|---|---|
| Instruções documentadas e finalidade | ? | ? | ? | ? | ? |
| Categorias de dados e titulares, e duração | ? | ? | ? | ? | ? |
| Confidencialidade e acesso mínimo | ? | ? | ? | ? | ? |
| Medidas técnicas e administrativas | ? | ? | ? | ? | ? |
| Suboperadores: aprovação ou aviso de mudança | ? | ? | ? | ? | ? |
| Transferência internacional e transferências posteriores | ? | ? | ? | ? | ? |
| SLA de aviso de incidente e cooperação (precisamos caber nos 3 dias úteis) | ? | ? | ? | ? | ? |
| Assistência a direitos do titular e ao RIPD | ? | ? | ? | ? | ? |
| Devolução ou exclusão ao fim, com evidência | ? | ? | ? | ? | ? |
| Auditoria / relatórios de conformidade | ? | ? | ? | ? | ? |
| **Proibição de uso próprio, publicidade, enriquecimento, venda e treinamento de modelos** | ? | ? | ? | ? | ? |

Um `N` numa linha não fecha a decisão sozinho: entra no
`docs/decision-log.md` com o que se fez (aditivo, outro fornecedor, ou o risco
aceito no `docs/ripd.md`).
