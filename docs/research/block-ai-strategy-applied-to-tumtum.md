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

Criar o equivalente dos "world models" da Block para a TumTum:

**World Model Interno** (documentacao viva):
- CLAUDE.md ja e o inicio disso — manter atualizado como fonte de verdade
- Todo decision log registrado em `/docs/decisions/` (ADRs - Architecture Decision Records)
- Metricas de desenvolvimento trackeadas (PRs/semana, cobertura de testes, velocidade de deploy)

**World Model do Usuario**:
- Desde o beta, trackear: quais momentos as pessoas compartilham mais? Quais cards viralizam? Quais eventos geram mais engajamento?
- PostHog como sistema central de customer signal
- Feedback loops curtos: beta users no WhatsApp, dados quantitativos no PostHog

#### Principio 4: Ciclos de 90 dias com DRIs

Adaptar o modelo Block de 90-day cycles:

| Ciclo | Periodo | DRI | Entregavel |
|-------|---------|-----|------------|
| **Ciclo 0** | Semanas 1-12 | Felipe | MVP funcional: auth + sync HR + visualizacao + 1 card shareable |
| **Ciclo 1** | Semanas 13-24 | TBD | Product-market fit: 1.000 usuarios, 100 cards compartilhados |
| **Ciclo 2** | Semanas 25-36 | TBD | Growth engine: viralidade organica via cards, onboarding <2min |

Cada ciclo tem **um DRI unico** responsavel pelo resultado, nao por tarefas.

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
