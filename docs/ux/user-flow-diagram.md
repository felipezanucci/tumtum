# Tumtum — Diagrama de Fluxo UX Completo

> Todas as telas e transicoes do app, do primeiro acesso ao compartilhamento

---

## Fluxo Geral (Visao Macro)

```mermaid
flowchart TD
    subgraph PRIMEIRO_ACESSO["🟡 PRIMEIRO ACESSO"]
        SPLASH([Splash Screen])
        ON1[Onboarding 1/3<br/>'Sinta cada momento']
        ON2[Onboarding 2/3<br/>'Descubra seus picos']
        ON3[Onboarding 3/3<br/>'Compartilhe a emocao']
    end

    subgraph AUTH["🔐 AUTENTICACAO"]
        LOGIN[Login / Cadastro]
        SOCIAL{Social Login<br/>ou Email?}
        OAUTH_GOOGLE[OAuth Google]
        OAUTH_APPLE[OAuth Apple]
        SIGNUP[Cadastro Email]
    end

    subgraph WEARABLE["⌚ CONEXAO WEARABLE"]
        PRIMING[Tela de Priming<br/>'Conecte seu relogio']
        PROVIDER[Selecao de Provider]
        OS_PERM[Permissao do OS<br/>HealthKit / Health Connect]
        SUCESSO_CONN[Sucesso Conexao<br/>BPM ao vivo]
    end

    subgraph CORE["🏠 APP PRINCIPAL"]
        HOME[Home]
        BUSCA[Busca de Eventos]
        DETALHE_EVT[Detalhe do Evento]
        PERFIL[Meu Perfil]
        COLECAO[Colecao de Cards]
        CONFIG[Configuracoes]
    end

    subgraph EVENTO["🎵 CICLO DO EVENTO"]
        PRE_EVT[Pre-Evento<br/>Checklist]
        LIVE[Modo Live<br/>Background / Tela]
        NOTIF_POS[Notificacao<br/>Pos-Evento]
        REVELACAO[Revelacao<br/>Cinematica]
        EXPERIENCIA[Experiencia<br/>Curva HR + Picos]
    end

    subgraph SHARE["📤 COMPARTILHAMENTO"]
        EDITOR[Editor de Card]
        PREVIEW[Preview Card]
        SHARE_SHEET[Share Sheet<br/>Redes Sociais]
        POS_SHARE[Pos-Share<br/>Celebracao]
    end

    %% PRIMEIRO ACESSO
    SPLASH --> ON1
    ON1 --> ON2
    ON2 --> ON3
    ON1 -.->|Pular| LOGIN
    ON2 -.->|Pular| LOGIN
    ON3 --> LOGIN

    %% AUTH
    LOGIN --> SOCIAL
    SOCIAL -->|Google| OAUTH_GOOGLE
    SOCIAL -->|Apple| OAUTH_APPLE
    SOCIAL -->|Email| SIGNUP
    OAUTH_GOOGLE --> PRIMING
    OAUTH_APPLE --> PRIMING
    SIGNUP --> PRIMING

    %% WEARABLE
    PRIMING --> PROVIDER
    PRIMING -.->|Fazer depois| HOME
    PROVIDER --> OS_PERM
    OS_PERM -->|Permitiu| SUCESSO_CONN
    OS_PERM -.->|Negou| HOME
    SUCESSO_CONN --> HOME

    %% CORE NAV
    HOME --> BUSCA
    HOME --> PERFIL
    HOME --> COLECAO
    HOME --> DETALHE_EVT
    BUSCA --> DETALHE_EVT
    PERFIL --> CONFIG
    PERFIL --> COLECAO
    COLECAO --> EDITOR

    %% CICLO DO EVENTO
    DETALHE_EVT -->|'Eu vou!'| PRE_EVT
    PRE_EVT -->|Dia do evento| LIVE
    LIVE -->|Evento termina| NOTIF_POS
    NOTIF_POS --> REVELACAO
    REVELACAO --> EXPERIENCIA

    %% SHARE
    EXPERIENCIA --> EDITOR
    EDITOR --> PREVIEW
    PREVIEW --> SHARE_SHEET
    SHARE_SHEET --> POS_SHARE
    POS_SHARE --> COLECAO
    POS_SHARE -.-> EXPERIENCIA

    %% ATALHOS
    HOME -->|Ultima experiencia| EXPERIENCIA
    NOTIF_POS -.->|Deep link| REVELACAO

    %% ESTILOS
    classDef primeiro fill:#2d2d3d,stroke:#C0392B,color:#F0F0F5
    classDef auth fill:#1a1a2e,stroke:#E74C3C,color:#F0F0F5
    classDef wearable fill:#1a1a2e,stroke:#00D2FF,color:#F0F0F5
    classDef core fill:#111118,stroke:#6B6B80,color:#F0F0F5
    classDef evento fill:#1a1a2e,stroke:#C0392B,color:#F0F0F5
    classDef share fill:#2d1a1a,stroke:#E74C3C,color:#F0F0F5

    class SPLASH,ON1,ON2,ON3 primeiro
    class LOGIN,SOCIAL,OAUTH_GOOGLE,OAUTH_APPLE,SIGNUP auth
    class PRIMING,PROVIDER,OS_PERM,SUCESSO_CONN wearable
    class HOME,BUSCA,DETALHE_EVT,PERFIL,COLECAO,CONFIG core
    class PRE_EVT,LIVE,NOTIF_POS,REVELACAO,EXPERIENCIA evento
    class EDITOR,PREVIEW,SHARE_SHEET,POS_SHARE share
```

---

## Fluxo Detalhado: Primeiro Acesso (Happy Path)

```mermaid
flowchart LR
    A([Abrir app]) --> B[Splash<br/>1.5s]
    B --> C[Onboarding<br/>3 telas swipe]
    C --> D[Auth<br/>1 toque social]
    D --> E[Priming<br/>wearable]
    E --> F[Permissao OS]
    F --> G[BPM ao vivo<br/>confirmacao]
    G --> H([Home])

    style A fill:#C0392B,color:#fff
    style H fill:#C0392B,color:#fff
```

**Tempo estimado: < 90 segundos** do primeiro toque ate a Home

---

## Fluxo Detalhado: Ciclo Completo do Evento

```mermaid
flowchart TD
    subgraph DESCOBERTA["🔍 DESCOBERTA"]
        B1[Home ou Busca]
        B2[Detalhe Evento]
        B3{Confirmar<br/>presenca?}
    end

    subgraph PREPARACAO["⏱ PREPARACAO"]
        P1[Evento salvo]
        P2[Push 3h antes]
        P3[Pre-Evento<br/>Checklist]
        P4{Tudo OK?}
        P5[Resolver problema<br/>wearable/bateria]
    end

    subgraph AO_VIVO["🎵 AO VIVO"]
        L1[Monitoramento<br/>automatico inicia]
        L2[Background<br/>coleta HR silenciosa]
        L3[Modo Live<br/>se abrir o app]
        L4[Evento termina<br/>sync dados]
    end

    subgraph REVELACAO_FLOW["✨ REVELACAO"]
        R1[Push: experiencia pronta]
        R2[Tela de revelacao<br/>cinematica]
        R3[Experiencia completa<br/>curva + picos]
    end

    subgraph COMPARTILHAR["📤 COMPARTILHAR"]
        S1[Escolher pico]
        S2[Editor de card]
        S3[Preview]
        S4[Share sheet]
        S5[Pos-share]
        S6[Card salvo<br/>na colecao]
    end

    B1 --> B2
    B2 --> B3
    B3 -->|Sim| P1
    B3 -.->|Nao| B1

    P1 -->|Dia do evento| P2
    P2 --> P3
    P3 --> P4
    P4 -->|Sim| L1
    P4 -->|Nao| P5
    P5 --> P4

    L1 --> L2
    L2 -.->|Opcional| L3
    L3 -.-> L2
    L2 --> L4

    L4 -->|30min depois| R1
    R1 --> R2
    R2 --> R3

    R3 --> S1
    S1 --> S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
    S5 --> S6
    S6 -.->|Novo card| S1
```

---

## Fluxo Detalhado: Compartilhamento

```mermaid
flowchart TD
    START([Trigger:<br/>toque em pico<br/>ou 'Gerar card'])

    TEMPLATE{Tipo de card}
    SOLO[Template Solo]
    COMP[Template Comparacao<br/>futuro]

    STYLE[Escolher estilo<br/>cor / tema]
    FORMAT{Formato}
    F_STORY[Stories 9:16]
    F_FEED[Feed 1:1]
    F_POST[Post 4:5]

    PREVIEW[Preview em<br/>tela cheia]
    EDIT{Satisfeito?}

    SHARE{Destino}
    INSTA[Instagram Stories]
    TIKTOK[TikTok]
    X_SHARE[X / Twitter]
    WHATS[WhatsApp]
    LINK[Copiar Link]
    SAVE[Salvar Imagem]

    DONE([Card salvo<br/>na colecao])

    START --> TEMPLATE
    TEMPLATE --> SOLO
    TEMPLATE -.->|v2| COMP
    SOLO --> STYLE
    COMP --> STYLE
    STYLE --> FORMAT
    FORMAT --> F_STORY
    FORMAT --> F_FEED
    FORMAT --> F_POST
    F_STORY --> PREVIEW
    F_FEED --> PREVIEW
    F_POST --> PREVIEW
    PREVIEW --> EDIT
    EDIT -->|Nao| STYLE
    EDIT -->|Sim| SHARE
    SHARE --> INSTA
    SHARE --> TIKTOK
    SHARE --> X_SHARE
    SHARE --> WHATS
    SHARE --> LINK
    SHARE --> SAVE
    INSTA --> DONE
    TIKTOK --> DONE
    X_SHARE --> DONE
    WHATS --> DONE
    LINK --> DONE
    SAVE --> DONE

    style START fill:#C0392B,color:#fff
    style DONE fill:#C0392B,color:#fff
```

---

## Navegacao por Tab Bar

```mermaid
flowchart LR
    subgraph TABS["Tab Bar — Navegacao Principal"]
        direction TB
        T1["🏠 Home"]
        T2["🔍 Busca"]
        T3["♥ Cards"]
        T4["👤 Perfil"]
    end

    subgraph HOME_STACK["Stack: Home"]
        direction TB
        H1[Home] --> H2[Detalhe Evento]
        H1 --> H3[Experiencia]
        H3 --> H4[Editor Card]
    end

    subgraph BUSCA_STACK["Stack: Busca"]
        direction TB
        B1[Lista Eventos] --> B2[Detalhe Evento]
        B2 --> B3[Pre-Evento]
    end

    subgraph CARDS_STACK["Stack: Cards"]
        direction TB
        C1[Grid de Cards] --> C2[Detalhe Card]
        C2 --> C3[Re-compartilhar]
    end

    subgraph PERFIL_STACK["Stack: Perfil"]
        direction TB
        P1[Meu Perfil] --> P2[Configuracoes]
        P1 --> P3[Experiencia antiga]
        P3 --> P4[Novo card]
    end

    T1 --- HOME_STACK
    T2 --- BUSCA_STACK
    T3 --- CARDS_STACK
    T4 --- PERFIL_STACK
```

---

## Mapa de Estados da Tela Home

```mermaid
stateDiagram-v2
    [*] --> SemWearable: Primeiro acesso<br/>sem conectar wearable

    SemWearable --> SemEventos: Conectou wearable
    SemWearable --> SemEventos: Pulou conexao

    SemEventos --> ComEvento: Confirmou<br/>presenca em evento

    ComEvento --> EventoHoje: Dia do evento

    EventoHoje --> MonitorandoLive: Evento comecou

    MonitorandoLive --> ExperienciaPronta: Evento acabou +<br/>dados processados

    ExperienciaPronta --> ComHistorico: Visualizou<br/>experiencia

    ComHistorico --> ComEvento: Confirma<br/>proximo evento

    state SemWearable {
        [*] --> BannerConectar
        BannerConectar: Banner persistente<br/>'Conecte seu relogio'
    }

    state SemEventos {
        [*] --> EmptyState
        EmptyState: Ilustracao + CTA<br/>'Encontrar evento'
    }

    state ComEvento {
        [*] --> CardProximoEvento
        CardProximoEvento: Card do proximo evento<br/>com countdown
    }

    state EventoHoje {
        [*] --> ChecklistPreEvento
        ChecklistPreEvento: Checklist automatico<br/>wearable + bateria
    }

    state MonitorandoLive {
        [*] --> StatusLive
        StatusLive: Indicador 'AO VIVO'<br/>BPM em tempo real
    }

    state ExperienciaPronta {
        [*] --> CardExperiencia
        CardExperiencia: Card experiencia<br/>com mini curva HR
    }

    state ComHistorico {
        [*] --> ListaExperiencias
        ListaExperiencias: Proximo evento +<br/>historico de experiencias
    }
```

---

## Fluxo de Permissoes (Progressive)

```mermaid
flowchart TD
    subgraph MOMENTO_1["Momento 1: Apos Auth"]
        W1[Priming: saude]
        W2[Permissao OS:<br/>HealthKit /<br/>Health Connect]
    end

    subgraph MOMENTO_2["Momento 2: Primeiro evento"]
        N1[Priming: localizacao<br/>'Encontrar eventos<br/>perto de voce']
        N2[Permissao OS:<br/>Localizacao]
    end

    subgraph MOMENTO_3["Momento 3: Pos primeiro evento"]
        P1[Priming: notificacoes<br/>'Avisamos quando sua<br/>experiencia estiver pronta']
        P2[Permissao OS:<br/>Notificacoes]
    end

    AUTH_OK([Auth completo]) --> W1
    W1 --> W2

    PRIMEIRO_EVENTO([Busca evento]) --> N1
    N1 --> N2

    POS_EVENTO([Evento terminou]) --> P1
    P1 --> P2

    style AUTH_OK fill:#C0392B,color:#fff
    style PRIMEIRO_EVENTO fill:#C0392B,color:#fff
    style POS_EVENTO fill:#C0392B,color:#fff
```

**Regra de ouro:** Cada permissao e pedida NO MOMENTO em que faz sentido para o usuario, nunca tudo de uma vez.

---

## Inventario de Telas

| # | Tela | Fluxo | Prioridade |
|---|------|-------|------------|
| 0 | Splash | Primeiro acesso | P0 |
| 1 | Onboarding 1/3 | Primeiro acesso | P0 |
| 2 | Onboarding 2/3 | Primeiro acesso | P0 |
| 3 | Onboarding 3/3 | Primeiro acesso | P0 |
| 4 | Login/Cadastro | Auth | P0 |
| 5 | Cadastro Email | Auth | P0 |
| 6 | Wearable Priming | Setup | P0 |
| 7 | Selecao Provider | Setup | P0 |
| 8 | Sucesso Conexao | Setup | P0 |
| 9 | Home | Core | P0 |
| 10 | Busca Eventos | Core | P0 |
| 11 | Detalhe Evento | Core | P0 |
| 12 | Pre-Evento | Evento | P1 |
| 13 | Modo Live | Evento | P1 |
| 14 | Revelacao | Pos-evento | P0 |
| 15 | Experiencia | Pos-evento | P0 |
| 16 | Editor de Card | Share | P0 |
| 17 | Share Sheet | Share | P0 |
| 18 | Pos-Share | Share | P0 |
| 19 | Meu Perfil | Perfil | P0 |
| 20 | Perfil Publico | Social | P2 |
| 21 | Configuracoes | Settings | P1 |

**22 telas | 17 no MVP (P0)**
