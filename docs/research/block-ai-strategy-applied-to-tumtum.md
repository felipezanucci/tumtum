# De Hierarquia a Inteligencia: Como a Block usa AI e o que a TumTum pode aprender

> Pesquisa baseada no artigo ["From Hierarchy to Intelligence"](https://sequoiacap.com/article/from-hierarchy-to-intelligence/) da Sequoia Capital (Jack Dorsey & Roelof Botha, marco 2026) e fontes complementares.

---

## 1. O que a Block fez: resumo executivo

A Block (empresa-mae do Square, Cash App e Afterpay) executou em 2025-2026 a transformacao corporativa mais radical orientada por AI ate o momento:

| Fato | Detalhe |
|------|---------|
| **Corte de 40% do quadro** | ~4.000 funcionarios demitidos em fev/2026 |
| **Produtividade de engenharia** | +40% de codigo em producao por engenheiro (desde set/2025) |
| **Receita bruta** | +24% YoY no Q4 2025 |
| **Reacao do mercado** | Acao subiu +20% no pre-market apos o anuncio |
| **Ferramenta-chave** | Goose — agente AI open-source (27k+ stars no GitHub) |
| **Produto AI para clientes** | Managerbot — agente proativo para lojistas Square |

A tese central: **velocidade e o melhor preditor de sucesso de startups**, e AI nao deve ser apenas um copilot que torna a estrutura existente "um pouco melhor" — deve **substituir o que a hierarquia faz**.

---

## 2. O modelo "From Hierarchy to Intelligence"

### 2.1 O problema que hierarquias resolvem

Dorsey e Botha comecam com uma analogia historica: o Exercito Romano resolveu o problema de coordenacao ha 2.000 anos com hierarquia aninhada e span of control consistente em cada nivel. Toda estrutura organizacional existe para **rotear informacao** e resolver um **problema de largura de banda**.

Em empresas tradicionais:
- Inteligencia esta **distribuida nas pessoas**
- A hierarquia **roteia** essa inteligencia
- Gerentes existem para **retransmitir informacao** entre camadas

### 2.2 A inversao: inteligencia no centro, pessoas na borda

A Block inverteu o modelo:

```
ANTES (Piramide)              DEPOIS (Circulo)
                              
     CEO                      Pessoas na borda
    / | \                     (onde a acao acontece)
   M   M   M                        |
  /|\ /|\ /|\               [  AI Intelligence  ]
 IC IC IC IC IC               (centro coordenador)
                                     |
                              Pessoas na borda
```

- **Inteligencia vive no sistema** (AI como "camada de inteligencia")
- **Pessoas estao na borda** — onde a acao real acontece
- A empresa funciona como um **mini-AGI**: um sistema que mantem um modelo continuamente atualizado de todo o negocio

### 2.3 Dois "World Models"

A Block opera com dois modelos de mundo mantidos por AI:

1. **World Model Interno**: agrega dados de codigo, decisoes, workflows, metricas de performance — cria uma fotografia continuamente atualizada das operacoes da empresa. Substitui o contexto que gerentes tradicionais carregavam na cabeca.

2. **World Model do Cliente**: mapeia comportamento de clientes e lojistas usando dados de transacoes do Cash App e Square.

### 2.4 Tres unicos papeis

A Block simplificou toda a organizacao em apenas tres papeis:

| Papel | Funcao |
|-------|--------|
| **Individual Contributor (IC)** | Constroem sistemas — engenheiros, designers, etc. |
| **Directly Responsible Individual (DRI)** | Donos de resultados especificos em **ciclos de 90 dias** |
| **Player-Coach** | Mentoram enquanto continuam com as maos na massa tecnica |

Nao ha mais "gerentes puros" cuja funcao principal era coordenar e retransmitir informacao.

---

## 3. As ferramentas AI da Block

### 3.1 Goose (codename goose)

**O que e**: agente AI open-source, extensivel, que vai alem de sugestoes de codigo — instala, executa, edita e testa com qualquer LLM.

**Como funciona**:
- Roda na maquina do desenvolvedor (desktop macOS/Linux/Windows, CLI, ou API)
- Escrito em Rust para performance
- Usa **Model Context Protocol (MCP)** da Anthropic para se conectar a qualquer sistema
- Registra de extensoes MCP com 3.000+ servidores disponiveis
- **Recipes**: workflows YAML multi-step (ex: rodar testes, analisar falhas, corrigir codigo, re-rodar)
- Nao apenas sugere texto — **executa tarefas autonomamente**: le/escreve arquivos, roda codigo, instala dependencias

**Resultados internos**:
- +40% de codigo em producao por engenheiro
- Trabalhos que levavam semanas foram completados por times pequenos em fracao do tempo
- O proprio time do Goose usa Goose para escrever a maioria do codigo novo do Goose
- Jack Dorsey usou Goose para construir a primeira versao do Bitchat

**Ecossistema**:
- 27.000+ GitHub stars, 350+ contribuidores, 100+ releases
- Contribuido para a Linux Foundation's Agentic AI Foundation (dez/2025)
- Goose Grant Program financiando desenvolvedores externos

### 3.2 Managerbot

**O que e**: agente AI proativo para lojistas Square — o produto que materializa a tese "AI como gerente".

**Tres dominios**:

1. **Gestao de Inventario**: monitora niveis de estoque, velocidade de vendas, sinais externos (clima, eventos locais). Alerta quando item vai acabar ou quando deve estocar antes de pico de demanda.

2. **Scheduling de Funcionarios**: analisa previsao de vendas e gera escalas otimizadas balanceando preferencias dos funcionarios com necessidades de cobertura.

3. **Marketing Automatizado**: identifica tendencias de vendas e rascunha campanhas de win-back e promocoes segmentadas.

**Principio de design critico**: Managerbot **nao executa acoes autonomamente**. Toda acao que modifica o negocio (ajustar escala, publicar campanha, modificar inventario) requer **aprovacao explicita do lojista**. AI sugere, humano decide.

---

## 4. Criticas e limitacoes

- Ex-funcionarios reportaram que ~95% do codigo gerado por AI ainda requer modificacao humana
- AI ainda nao consegue liderar em areas altamente reguladas (como banking)
- Alguns analistas questionam se o corte de 40% e genuinamente AI-driven ou corte de custos com narrativa conveniente
- O modelo ainda esta em fase inicial — os resultados de longo prazo sao incertos

---

## 5. Aplicacao direta a TumTum

### 5.1 Principios que a TumTum deve adotar AGORA

#### Principio 1: "AI-First" desde o dia zero

A Block levou 18 meses para integrar AI profundamente. A TumTum tem a vantagem de nascer nessa era — nao precisa transformar, precisa **nascer inteligente**.

**Acao concreta**:
- Toda tarefa de desenvolvimento deve passar por um agente AI (Claude Code, Goose, Cursor)
- Meta: 70%+ do codigo novo gerado/assistido por AI
- Nao contratar para escala — usar AI para **multiplicar** o time minimo

#### Principio 2: Time minimo, maximo impacto

A Block mostrou que times menores + AI > times grandes sem AI.

**Estrutura proposta para TumTum (Phase 0)**:

| Papel | Quem | Funcao |
|-------|------|--------|
| **DRI Produto** | Felipe | Visao, decisoes de produto, customer signal |
| **Player-Coach Eng** | 1 senior dev | Arquitetura + hands-on + mentoria de AI |
| **IC Frontend** | 1 dev (ou AI-augmented) | Next.js, PWA, visualizacoes |
| **IC Backend** | 1 dev (ou AI-augmented) | FastAPI, integracao wearables |
| **AI Layer** | Claude Code + Goose | Gerador de codigo, testes, reviews, automacoes |

Total: **3-4 pessoas + AI** fazendo o trabalho de um time de 10-15.

#### Principio 3: World Model da TumTum

Na Block, o "world model" e o que substitui a memoria coletiva dos gerentes — um sistema vivo que sabe o estado de tudo na empresa a qualquer momento. Para a TumTum, isso se traduz em dois pilares: um **World Model Interno** (como o time opera e decide) e um **World Model do Usuario** (como os usuarios se comportam e o que valorizam).

---

##### 3A. World Model Interno: ADRs (Architecture Decision Records)

**O que sao ADRs**

ADRs sao documentos curtos e padronizados que registram **decisoes tecnicas e de produto** junto com seu contexto e consequencias. Sao a "memoria institucional" da empresa — o equivalente ao que a Block chama de world model interno.

Sem ADRs, o conhecimento vive na cabeca das pessoas. Com 3-4 pessoas no time, se um dev sai ou se Felipe esquece por que uma decisao foi tomada ha 3 meses, o contexto se perde. ADRs evitam isso.

**Formato padrao para a TumTum**

Cada ADR segue uma estrutura fixa, salva em `/docs/decisions/`:

```
docs/decisions/
  NNNN-titulo-curto.md    (ex: 0001-usar-timescaledb-para-hr-data.md)
```

Template:

```markdown
# ADR-NNNN: [Titulo da decisao]

**Status**: proposta | aceita | substituida por ADR-XXXX | descartada
**Data**: YYYY-MM-DD
**DRI**: [Quem tomou a decisao]
**Ciclo**: [Ciclo 0 / Ciclo 1 / ...]

## Contexto

[Qual problema estavamos enfrentando? Que restricoes existiam?
O que motivou essa decisao?]

## Decisao

[O que decidimos fazer. Claro e direto.]

## Alternativas consideradas

| Alternativa | Pros | Contras | Por que descartada |
|-------------|------|---------|-------------------|
| Opcao A | ... | ... | ... |
| Opcao B | ... | ... | ... |

## Consequencias

**Positivas:**
- [O que ganhamos]

**Negativas / Riscos:**
- [O que perdemos ou arriscamos]

**Metricas de validacao:**
- [Como vamos saber se a decisao foi boa]
```

**Exemplos concretos de ADRs para a TumTum**

| ADR | Decisao | Por que importa |
|-----|---------|-----------------|
| `0001-timescaledb-para-hr-data` | Usar TimescaleDB (hypertable) em vez de PostgreSQL puro para dados de HR | Define a arquitetura de dados do core business — irreversivel depois de ter dados em producao |
| `0002-pwa-em-vez-de-app-nativo` | Lancar como PWA mobile-first antes de app nativo | Afeta toda a estrategia de distribuicao e acesso a HealthKit/Health Connect |
| `0003-celery-redis-para-cards` | Usar Celery + Redis para geracao assincrona de cards | Define a arquitetura de processamento do recurso mais viral do produto |
| `0004-dark-mode-only-mvp` | Apenas dark mode no MVP, sem light mode | Reduz escopo de design em 40-50%, mas pode excluir usuarios |
| `0005-jwt-sem-refresh-rotation` | JWT simples sem refresh token rotation no MVP | Trade-off consciente: menos seguro, mas shipping mais rapido |
| `0006-setlistfm-como-fonte-primaria` | Setlist.fm como fonte primaria de timeline de eventos musicais | Dependencia externa critica — precisa de fallback plan |
| `0007-peak-detection-zscore` | Usar z-score com janela de 60s para peak detection | Decisao algoritmica central — validar com dados reais antes de iterar |
| `0008-r2-para-card-storage` | Cloudflare R2 (S3-compatible) para storage de cards gerados | Custo vs. performance vs. vendor lock-in |
| `0009-posthog-para-analytics` | PostHog self-hosted vs. cloud para analytics e customer signal | Define como o world model do usuario vai ser construido |
| `0010-pt-br-default-i18n-ready` | UI em portugues BR como default, com estrutura i18n pronta | Foco no mercado primario sem fechar porta para expansao |

**Quando escrever uma ADR**

Nem toda decisao precisa de ADR. Regra pratica:

- **Escreva ADR** quando: a decisao e dificil de reverter, afeta multiplos componentes, envolve trade-offs significativos, ou voce vai querer lembrar "por que fizemos isso?" daqui a 3 meses
- **Nao precisa de ADR**: escolha de nome de variavel, formatacao de codigo, ajuste de cor CSS, bugfix simples

**Processo de ADR no dia a dia**

1. Qualquer pessoa do time pode propor uma ADR (status: `proposta`)
2. DRI do ciclo revisa e aceita/recusa (status: `aceita` ou `descartada`)
3. ADRs aceitas sao **imutaveis** — se a decisao mudar, cria-se uma nova ADR que referencia a anterior (status da antiga: `substituida por ADR-XXXX`)
4. AI (Claude Code) pode **gerar rascunhos** de ADRs a partir de discussoes no PR ou commits

**ADRs como insumo para AI**

Aqui esta o pulo do gato que conecta ADRs ao world model da Block: quando o CLAUDE.md referencia as ADRs, qualquer agente AI que trabalhe no projeto tem **contexto completo** das decisoes passadas. Isso significa:
- Claude Code nao vai sugerir usar MongoDB se existe uma ADR explicando por que escolhemos TimescaleDB
- Novos devs (humanos ou AI) entendem o "por que" por tras da arquitetura instantaneamente
- Decisoes nao sao re-debatidas — elas estao documentadas com contexto

Adicionar ao CLAUDE.md:
```markdown
## Decision Log
All architectural and product decisions are recorded as ADRs in `/docs/decisions/`.
Before proposing changes to core architecture, read the relevant ADRs.
```

##### 3B. World Model Interno: alem das ADRs

As ADRs sao o pilar principal, mas o world model interno completo inclui:

| Componente | Onde vive | O que captura |
|------------|-----------|---------------|
| **CLAUDE.md** | Raiz do repo | Stack, padroes, regras, contexto de negocio — a "constituicao" |
| **ADRs** | `/docs/decisions/` | Decisoes com contexto e trade-offs |
| **Sprint Log** | `/docs/cycles/ciclo-N.md` | O que foi planejado, o que foi entregue, o que aprendemos |
| **GitHub Issues + PRs** | GitHub | Historico granular de implementacao |
| **PostHog dashboards** | PostHog | Metricas de produto e comportamento |
| **Sentry** | Sentry | Saude tecnica — erros, performance |

Juntos, esses componentes formam o equivalente TumTum do world model interno da Block — qualquer pessoa (ou AI) que entre no projeto consegue entender **o que existe, por que existe, e como esta performando** em minutos.

---

##### 3C. World Model do Usuario (Customer Signal System)

O World Model do Usuario e o equivalente ao segundo world model da Block — o que mapeia comportamento de clientes para informar decisoes de produto. Para a TumTum, isso e **critico** porque o produto inteiro gira em torno de emocao, e emocao e subjetiva. Precisamos de dados para saber o que realmente emociona as pessoas.

**Arquitetura do Customer Signal System**

```
                    FONTES DE DADOS
                    ===============
  [PostHog]   [WhatsApp Beta]   [Social Shares]   [App Usage]
      |              |                |                |
      v              v                v                v
                +-----------------------+
                |   CUSTOMER SIGNAL     |
                |      DATABASE         |
                +-----------------------+
                         |
          +--------------+--------------+
          |              |              |
     [Engagement   [Emotion      [Viral
      Patterns]     Profile]     Signals]
          |              |              |
          v              v              v
      DECISOES DE PRODUTO INFORMADAS POR DADOS
```

**Camada 1: Metricas de Comportamento (PostHog)**

Eventos a trackear desde o dia 1 do beta:

| Evento PostHog | O que revela | Decisao que informa |
|----------------|-------------|---------------------|
| `event_selected` + propriedade `event_type` | Concertos vs. futebol vs. festival — qual domina? | Priorizar integracao (Setlist.fm vs. API-Football) |
| `hr_curve_viewed` + `time_spent_seconds` | Quanto tempo as pessoas olham a curva? Se >30s, e engajante. Se <5s, esta confusa ou irrelevante | Design da experiencia de visualizacao |
| `peak_tapped` + `peak_rank` | Quais picos as pessoas clicam? Os top 3? Ou exploram alem? | Quantos peaks mostrar por padrao |
| `card_generated` + `card_template` | Qual template de card e mais gerado? | Priorizar design de templates |
| `card_shared` + `platform` | Instagram? WhatsApp? TikTok? | Otimizar formato e aspect ratio por plataforma |
| `card_shared` / `card_generated` (ratio) | Taxa de conversao geracao -> compartilhamento | Qualidade dos cards gerados |
| `onboarding_step_completed` + `step_number` | Onde as pessoas desistem no onboarding? | Simplificar passos com maior drop-off |
| `wearable_connected` + `provider` | Apple Watch domina? Ou Fitbit/Garmin sao relevantes? | Priorizar integracao de wearables |
| `session_duration` | Quanto tempo por sessao no app? | Feature prioritization |
| `return_visit` + `days_since_last` | As pessoas voltam? Quando? | Estrategia de retencao/notificacao |

**Camada 2: Perfil Emocional do Usuario**

Dados derivados das sessoes de HR que formam um "perfil emocional" unico:

| Dado derivado | Como calcular | Para que serve |
|---------------|---------------|----------------|
| **Intensidade media** | Media dos z-scores dos peaks ao longo de eventos | Classificar usuarios como "intense reactors" vs. "steady" |
| **Genero/esporte mais emocionante** | Correlacao entre tipo de evento e magnitude de peaks | Event Recommender personalizado |
| **Momento-tipo favorito** | Qual tipo de timeline entry gera mais peaks? (gol, refrão, encore) | Smart Highlights — priorizar momentos do tipo que mais emociona o usuario |
| **Curva de emocao tipica** | O usuario comeca frio e esquenta? Comeca no topo e decai? | Personalizar narrativa do card |
| **Social propensity** | Ratio de cards compartilhados vs. gerados | Identificar "super sharers" para viralidade |

**Camada 3: Sinais de Viralidade**

Os cards sao o motor viral da TumTum. Trackear o que viraliza:

| Sinal | Como medir | Acao derivada |
|-------|-----------|---------------|
| **Cards mais compartilhados** | Contagem de shares por card_id | Analisar o que esses cards tem em comum (template? evento? pico alto?) |
| **Plataforma com mais traction** | Shares por plataforma | Otimizar formato (Stories 9:16, Feed 1:1, TikTok, etc.) |
| **Horario de compartilhamento** | Timestamp dos shares | Sugerir "melhor hora para compartilhar" |
| **Efeito de rede** | Novos signups que vieram de um card compartilhado (UTM tracking) | Identificar loops virais e amplifica-los |
| **Conteudo do card** | BPM mostrado, momento matched, template usado | Entender qual combinacao de elementos gera mais engajamento |

**Camada 4: Feedback Qualitativo (WhatsApp Beta Group)**

Dados quantitativos dizem O QUE. Feedback qualitativo diz POR QUE.

| Metodo | Frequencia | O que perguntar |
|--------|-----------|-----------------|
| **Pesquisa pos-evento** | Apos cada evento trackeado | "O que voce mais curtiu? O que faltou? Voce compartilhou? Por que sim/nao?" |
| **Entrevista 1:1** | Quinzenal com 3-5 usuarios | Deep dive em motivacoes, frustracao, wow moments |
| **Print de share** | Sempre que alguem compartilha | Pedir screenshot do post — como as pessoas apresentam o card? Que texto colocam junto? |
| **NPS simplificado** | Mensal | "De 0-10, quanto voce recomendaria TumTum pra um amigo?" + "Por que essa nota?" |

**Como o World Model do Usuario alimenta decisoes**

O ciclo completo:

```
  Dado coletado (PostHog/WhatsApp)
         |
         v
  Insight gerado (AI pode ajudar a sintetizar)
         |
         v
  Hipotese formulada ("usuarios compartilham mais cards de futebol que de shows")
         |
         v
  Experimento desenhado (A/B test ou feature flag)
         |
         v
  Resultado medido (PostHog)
         |
         v
  ADR registrada (se decisao de produto significativa)
         |
         v
  World Model atualizado (CLAUDE.md, roadmap, prioridades)
```

Isso fecha o loop entre os dois world models — o do usuario informa o interno, que por sua vez guia o que o time (e a AI) constroem a seguir.

#### Principio 4: Ciclos de 90 dias com DRIs e metricas de sucesso

Adaptar o modelo Block de 90-day cycles. Cada ciclo tem **um DRI unico** responsavel pelo **resultado** (nao por tarefas), e um conjunto claro de metricas que definem se o ciclo foi bem-sucedido.

---

##### Ciclo 0 — "Proof of Emotion" (Semanas 1-12)

**DRI**: Felipe
**Resultado esperado**: MVP funcional — uma pessoa consegue ir a um evento, ver sua curva HR sincronizada, e compartilhar um card.

**Metricas de sucesso do Ciclo 0:**

| Categoria | Metrica | Target | Como medir | Por que importa |
|-----------|---------|--------|-----------|-----------------|
| **Engenharia** | Features core entregues | 100% (auth + sync + viz + card) | GitHub milestones | Sem isso, nao existe produto |
| **Engenharia** | % codigo AI-assisted | >60% | Tag nos PRs ou estimativa semanal | Validar a tese AI-first |
| **Engenharia** | Cobertura de testes (services/) | >80% | pytest --cov | Qualidade do core (peak detection, correlator) |
| **Engenharia** | Tempo medio de PR (aberto -> merged) | <24h | GitHub analytics | Velocidade de iteracao |
| **Engenharia** | Uptime do ambiente de staging | >95% | Health check automatizado | Precisa funcionar para testar |
| **Produto** | Fluxo completo funcional (end-to-end) | Sim/Nao | Teste manual: conectar wearable -> evento -> curva -> card | Gate binario — funciona ou nao |
| **Produto** | Tempo do fluxo completo | <5 min | Cronometro em teste | Se demora mais, ninguem vai usar |
| **Produto** | Peak detection accuracy | >70% dos peaks "fazem sentido" | Validacao manual com 5+ sessoes reais | Se os peaks estao errados, o produto perde credibilidade |
| **Infra** | CI/CD pipeline funcional | Sim/Nao | GitHub Actions green | Deploy automatizado desde o inicio |
| **Infra** | Custo mensal de infra | <R$500 | Vercel + Railway + R2 billing | Burn rate controlado |
| **World Model** | ADRs registradas | >5 | Contagem em `/docs/decisions/` | Decisoes documentadas = contexto preservado |
| **World Model** | CLAUDE.md atualizado | Sim/Nao | Revisao no final do ciclo | Fonte de verdade precisa refletir a realidade |

**Checkpoint do Ciclo 0 (semana 12):**
- [ ] Demo funcional: uma sessao real (ou simulada com dados reais) do fluxo completo
- [ ] 3+ pessoas externas testaram o fluxo e deram feedback
- [ ] Todas as ADRs de decisoes de arquitetura registradas
- [ ] Sprint log do ciclo documentado em `/docs/cycles/ciclo-0.md`

**Criterio de "go/no-go" para Ciclo 1**: O fluxo completo funciona end-to-end E pelo menos 1 pessoa externa disse "eu compartilharia isso".

---

##### Ciclo 1 — "First Fans" (Semanas 13-24)

**DRI**: TBD
**Resultado esperado**: Product-market fit inicial — usuarios reais usando o produto em eventos reais e compartilhando cards organicamente.

**Metricas de sucesso do Ciclo 1:**

| Categoria | Metrica | Target | Como medir | Por que importa |
|-----------|---------|--------|-----------|-----------------|
| **Aquisicao** | Usuarios registrados | 500+ | Contagem na tabela `users` | Massa critica para dados significativos |
| **Aquisicao** | Custo por aquisicao (CPA) | <R$5 | Gasto marketing / novos usuarios | Escalabilidade do growth |
| **Aquisicao** | Canal principal de aquisicao | Identificar top 3 | UTM tracking no PostHog | Saber onde dobrar a aposta |
| **Ativacao** | % usuarios que completam onboarding | >60% | Funnel no PostHog (signup -> wearable connected -> first event) | Se <60%, onboarding esta quebrado |
| **Ativacao** | % usuarios que conectam wearable | >70% dos que fizeram signup | PostHog event `wearable_connected` | Sem wearable conectado, sem produto |
| **Ativacao** | Time-to-first-card | <3 min apos evento | Timestamp entre `event_ended` e `card_generated` | Friccao mata conversao |
| **Engajamento** | Sessoes HR registradas / usuario | >1.5 | Contagem na tabela `hr_sessions` por `user_id` | Usuarios voltam para mais de um evento? |
| **Engajamento** | Tempo na tela de experiencia (curva HR) | >45s media | PostHog event `hr_curve_viewed` + `session_duration` | As pessoas acham a curva interessante? |
| **Engajamento** | Peaks interagidos / sessao | >2 | PostHog event `peak_tapped` | Usuarios exploram seus momentos? |
| **Compartilhamento** | Cards gerados / usuario / evento | >1.2 | `cards` table count por sessao | Geracao e facil o suficiente? |
| **Compartilhamento** | Cards compartilhados / cards gerados | >40% | `shares` count / `cards` count | Conversao do motor viral |
| **Compartilhamento** | Plataforma dominante | Identificar | PostHog event `card_shared` + `platform` | Otimizar formato e fluxo |
| **Viralidade** | Coeficiente viral (k-factor) | >0.3 | Novos signups vindos de cards / total shares | Indicador de crescimento organico |
| **Retencao** | % usuarios que usam em 2+ eventos | >30% | `hr_sessions` count per `user_id` >= 2 | Retencao = product-market fit |
| **Retencao** | Retention D7 (voltam em 7 dias) | >25% | PostHog cohort analysis | Habito comecando a se formar |
| **Satisfacao** | NPS | >40 | Pesquisa mensal via WhatsApp/in-app | Satisfacao geral |
| **Satisfacao** | "Voce compartilharia?" (qualitativo) | >70% dizem sim | Pesquisa pos-evento | Validacao do core value prop |
| **Engenharia** | Deploy frequency | >3x/semana | GitHub deployments | Iteracao rapida |
| **Engenharia** | Bug critico (P0) open time | <24h | GitHub Issues | Qualidade de resposta |
| **Financeiro** | Burn rate mensal total | <R$10k | Soma de todos os custos (infra + tools + pessoas) | Sustentabilidade |

**Checkpoint do Ciclo 1 (semana 24):**
- [ ] 500+ usuarios registrados
- [ ] 50+ cards compartilhados organicamente (sem pedir)
- [ ] 3+ eventos reais cobertos com dados de HR
- [ ] Identificados os 2-3 tipos de usuario que mais engajam (persona refinada)
- [ ] World Model do Usuario com dados reais: sabe qual evento tipo, qual plataforma, qual momento gera mais shares

**Criterio de "go/no-go" para Ciclo 2**: Taxa de compartilhamento >40% E pelo menos 30% dos usuarios usam em 2+ eventos. Se nao atingir, o Ciclo 2 vira "Ciclo 1.5" de iteracao sobre engagement, nao growth.

---

##### Ciclo 2 — "Viral Engine" (Semanas 25-36)

**DRI**: TBD
**Resultado esperado**: Crescimento organico funcionando — cada usuario traz pelo menos 0.5 novos usuarios via cards compartilhados.

**Metricas de sucesso do Ciclo 2:**

| Categoria | Metrica | Target | Como medir | Por que importa |
|-----------|---------|--------|-----------|-----------------|
| **Growth** | Usuarios totais | 3.000+ | Tabela `users` | Escala para dados significativos |
| **Growth** | Crescimento MoM (mes a mes) | >30% | Comparacao mensal de signups | Curva de crescimento saudavel |
| **Growth** | K-factor (coeficiente viral) | >0.5 | (Shares * taxa de conversao de share -> signup) / usuarios ativos | Motor viral funcionando |
| **Growth** | % crescimento organico vs. pago | >60% organico | UTM tracking | Viralidade real, nao comprada |
| **Engajamento** | DAU/MAU ratio | >15% | PostHog | Sticky factor — as pessoas usam regularmente? |
| **Engajamento** | Eventos trackeados / usuario / mes | >1 | `hr_sessions` por mes | Uso recorrente |
| **Engajamento** | Narrative Generator engagement | >60% leem a narrativa | PostHog event `narrative_viewed` | AI-generated content funciona? |
| **Compartilhamento** | Cards compartilhados / semana | >200 | `shares` table weekly count | Volume do motor viral |
| **Compartilhamento** | Impressoes estimadas de cards | Trackear | Link tracking / view count na pagina publica do card | Alcance real dos cards |
| **Produto AI** | Smart Highlights satisfaction | >80% dos usuarios acham os highlights "certos" | Pesquisa pos-evento ou thumbs up/down no highlight | AI de highlights funciona? |
| **Produto AI** | Card Auto-Gen acceptance rate | >50% usam card sugerido vs. customizam | PostHog: `card_auto_accepted` vs `card_customized` | AI gera cards bons o suficiente? |
| **Produto AI** | Event Recommender CTR | >15% | Cliques em recomendacao / recomendacoes mostradas | AI de recomendacao e relevante? |
| **Retencao** | Retention D30 | >20% | PostHog cohort | Retencao de medio prazo |
| **Retencao** | Churn rate mensal | <15% | Usuarios que nao abrem o app em 30 dias / total | Perda controlada |
| **Onboarding** | Time-to-first-card (novos usuarios) | <2 min | Timestamp tracking | Onboarding otimizado |
| **Onboarding** | Onboarding completion rate | >75% | PostHog funnel | Friccao minimizada |
| **Financeiro** | Revenue (se aplicavel) | Primeiros testes | Premium features / partnerships | Caminho para monetizacao |
| **Financeiro** | LTV estimado | Calcular | Estimativa baseada em retencao e engagement | Base para decisao de investimento |

**Checkpoint do Ciclo 2 (semana 36):**
- [ ] 3.000+ usuarios, >60% vindos organicamente
- [ ] Motor viral comprovado: k-factor >0.5
- [ ] Features AI (Smart Highlights, Card Auto-Gen) em producao e com dados de satisfacao
- [ ] Modelo de monetizacao testado (mesmo que early)
- [ ] Dados suficientes para pitch deck a investidores (se desejado)
- [ ] Decisao informada: escalar (Phase 1) ou pivotar

---

##### Logica de "cascata" entre ciclos

```
Ciclo 0: FUNCIONA? (produto tecnico)
  |
  |-- Sim --> Ciclo 1: IMPORTA? (product-market fit)
  |              |
  |              |-- Sim --> Ciclo 2: ESCALA? (growth engine)
  |              |              |
  |              |              |-- Sim --> Phase 1 (smart band, investimento)
  |              |              |-- Nao --> Ciclo 2.5 (iterar growth)
  |              |
  |              |-- Nao --> Ciclo 1.5 (iterar engagement, testar novas hipoteses)
  |
  |-- Nao --> Iterar Ciclo 0 (fix core bugs, simplificar fluxo)
```

A chave e: **nenhum ciclo avanca se o anterior nao passou no gate**. Isso evita construir growth em cima de um produto que ninguem quer, ou escalar um produto que nem funciona direito.

### 5.2 AI na stack da TumTum: oportunidades especificas

#### A. Desenvolvimento acelerado (como Goose faz na Block)

| Area | Como usar AI |
|------|-------------|
| **Codigo** | Claude Code / Goose para gerar componentes, endpoints, testes |
| **Code Review** | AI como primeiro reviewer antes do humano |
| **Testes** | Geracao automatica de testes unitarios e de integracao |
| **Migrations** | AI gera Alembic migrations a partir de mudancas no schema |
| **Deploy** | GitHub Actions workflows gerados e mantidos por AI |
| **Docs** | ADRs e docs tecnicas geradas a partir de PRs e commits |

#### B. Produto inteligente (como Managerbot faz na Block)

| Feature | Descricao | Inspiracao Block |
|---------|-----------|------------------|
| **Smart Highlights** | AI analisa a curva HR e automaticamente identifica os 3 momentos mais emocionantes, sem precisar que o usuario escolha | Managerbot's proactive suggestions |
| **Card Auto-Generation** | Ao final de um evento, AI gera 2-3 opcoes de card prontas para compartilhar — o usuario so aprova | Managerbot's "suggest, human decides" |
| **Event Recommender** | Baseado no historico de HR reactions, AI sugere proximos eventos ("Voce reagiu forte a rock alternativo — Foo Fighters toca em SP semana que vem") | World Model do Cliente |
| **Narrative Generator** | AI gera uma micro-narrativa do evento: "Seu coracao disparou para 142bpm exatamente quando o gol do Palmeiras saiu aos 38 do 2o tempo" | Coordenacao AI + dados contextuais |
| **Sync Score AI** | Futuro: comparar HR do fan com HR do artista usando AI para calcular "sync score" de forma mais inteligente que correlacao simples | AI como core differentiator |

#### C. Operacoes lean (filosofia Block de AI substituindo coordenacao)

| Operacao | Abordagem AI-first |
|----------|-------------------|
| **Customer Support** | Chatbot AI como primeira linha (FAQ, troubleshooting de sync) |
| **Content Moderation** | AI valida cards antes de publicacao (nada ofensivo, dados sensiveis) |
| **Event Data** | AI faz scraping + normalizacao de dados de eventos (setlists, lineups) |
| **Analytics** | Dashboards auto-gerados com insights (PostHog + AI summary) |
| **Marketing** | AI gera copy para redes sociais baseado nos cards mais compartilhados |

### 5.3 O que NAO copiar da Block

| Aspecto Block | Por que nao se aplica a TumTum |
|---------------|-------------------------------|
| Corte massivo de pessoal | TumTum ainda nao tem pessoal — o desafio e nao precisar contratar demais |
| World model com milhoes de transacoes | TumTum comeca com dezenas/centenas de usuarios — o world model sera simples inicialmente |
| Infraestrutura AI interna complexa | Usar ferramentas existentes (Claude, Goose, PostHog) em vez de construir plataforma propria |
| Eliminacao total de gerencia | Com 3-4 pessoas, nao ha hierarquia para eliminar — foco em nao criar desnecessariamente |

---

## 6. Roadmap AI-First para TumTum

### Fase Imediata (Semanas 1-4): Foundation AI-Native

- [ ] Configurar Claude Code como ferramenta padrao de desenvolvimento
- [ ] Instalar Goose e configurar recipes para o projeto (lint, test, build)
- [ ] Criar estrutura de ADRs em `/docs/decisions/`
- [ ] Setup PostHog para customer signal desde o primeiro deploy
- [ ] Definir metricas de AI productivity (% codigo AI-assisted, tempo de PR, etc.)

### Fase MVP (Semanas 5-12): Ship with AI leverage

- [ ] AI-assisted peak detection refinement (testar multiplas abordagens rapidamente)
- [ ] Smart Highlights: AI seleciona top 3 momentos automaticamente
- [ ] Card Auto-Generation: 2-3 templates gerados automaticamente por evento
- [ ] Landing page com copy AI-generated, A/B testada

### Fase Growth (Semanas 13-24): AI como diferencial de produto

- [ ] Narrative Generator ("Seu coracao disparou quando...")
- [ ] Event Recommender baseado em perfil emocional
- [ ] Chatbot de suporte AI-first
- [ ] AI content moderation para cards publicos

---

## 7. Metricas de sucesso (inspiradas na Block)

| Metrica | Target TumTum | Benchmark Block |
|---------|---------------|-----------------|
| Codigo AI-assisted | >70% | ~40% mais codigo/eng |
| Time to first card (onboarding) | <3 min | N/A |
| Cards compartilhados / usuario / evento | >1.5 | N/A |
| Ciclo de feature (ideia -> producao) | <2 semanas | Reducao de semanas para dias |
| Custo de desenvolvimento mensal | <R$5k em AI tools | N/A |
| Headcount Phase 0 | 3-4 pessoas | Block reduziu 40% e manteve output |

---

## 8. Conclusao: a vantagem de nascer agora

A Block gastou 18 meses e demitiu 4.000 pessoas para se transformar em uma empresa AI-native. A TumTum tem o luxo de **nascer assim**.

A licao principal nao e sobre ferramentas — e sobre **mentalidade**:

> "A maioria das empresas usa AI como copilot para tornar a estrutura existente um pouco melhor. As que vao vencer usam AI para substituir o que a estrutura faz." — Tese Dorsey/Botha

Para a TumTum, isso significa:
1. **Nao construir hierarquia** que depois vai precisar destruir
2. **AI como membro do time**, nao como ferramenta
3. **World model desde o dia 1** — documentacao viva, customer signal centralizado
4. **Ciclos curtos com DRIs** — 90 dias, uma pessoa responsavel, um resultado mensuravel
5. **Ship fast, learn fast** — AI permite iterar em velocidade que antes exigia 10x mais pessoas

---

## Fontes

- [From Hierarchy to Intelligence — Sequoia Capital (Dorsey & Botha)](https://sequoiacap.com/article/from-hierarchy-to-intelligence/)
- [From Hierarchy to Intelligence — Block.xyz](https://block.xyz/inside/from-hierarchy-to-intelligence)
- [Jack Dorsey: Every Company Can Now Be a Mini-AGI — Sequoia Podcast](https://sequoiacap.com/podcast/jack-dorsey-every-company-can-now-be-a-mini-agi/)
- [Block CFO on 18 months of AI leaps — Fortune](https://fortune.com/2026/03/06/exclusive-block-cfo-ai-leaps-18-months-led-decision-slash-nearly-half-its-workforce/)
- [Dorsey & Botha on AI replacing middle management — Fortune](https://fortune.com/2026/04/02/jack-dorsey-roelof-botha-ai-middle-management/)
- [Block layoffs analysis — Josh Bersin](https://joshbersin.com/2026/03/is-blocks-decision-to-layoff-40-of-its-workforce-a-bellwether-or-not/)
- [Block introduces Managerbot — VentureBeat](https://venturebeat.com/data/block-introduces-managerbot-a-proactive-square-ai-agent-and-the-clearest)
- [Goose — GitHub (Block Open Source)](https://github.com/block/goose)
- [Goose AI Review 2026 — AI Tool Analysis](https://aitoolanalysis.com/goose-ai-review/)
- [Block layoffs signal AI-first future — AI CERTs](https://www.aicerts.ai/news/block-layoffs-signal-ai-first-future-at-square-parent/)
- [Is AI the strategy or the scapegoat? — Darden/UVA](https://news.darden.virginia.edu/2026/03/13/is-ai-the-strategy-or-the-scapegoat-behind-blocks-40-layoff/)
