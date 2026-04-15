# Tumtum — Diagrama de Fluxo UX Completo

> Foco: Futebol & Experiencia de Torcida
> Todas as telas e transicoes do app, do primeiro acesso ao duelo de torcidas

---

## Fluxo Geral (Visao Macro)

```mermaid
flowchart TD
    subgraph PRIMEIRO_ACESSO["PRIMEIRO ACESSO"]
        SPLASH([Splash Screen])
        ON1[Onboarding 1/3<br/>'Sinta o jogo']
        ON2[Onboarding 2/3<br/>'Sua torcida unida']
        ON3[Onboarding 3/3<br/>'Quem vibra mais?']
    end

    subgraph AUTH["AUTENTICACAO"]
        LOGIN[Login / Cadastro]
        SOCIAL{Social Login<br/>ou Email?}
        OAUTH_GOOGLE[OAuth Google]
        OAUTH_APPLE[OAuth Apple]
        SIGNUP[Cadastro Email]
    end

    subgraph SETUP["SETUP TORCEDOR"]
        TIME[Escolha do Time]
        CONFIRMA_TIME[Confirma Time<br/>cores + escudo]
        PRIMING[Wearable Priming]
        PROVIDER[Selecao Provider]
        OS_PERM[Permissao OS]
        SUCESSO_CONN[Sucesso Conexao<br/>BPM ao vivo]
    end

    subgraph CORE["APP PRINCIPAL"]
        HOME[Home]
        JOGOS[Lista de Jogos]
        DETALHE_JOGO[Detalhe do Jogo]
        PERFIL[Perfil Torcedor]
        CONFIG[Configuracoes]
    end

    subgraph JOGO["CICLO DO JOGO"]
        PRE_JOGO[Pre-Jogo<br/>Checklist]
        LIVE[Modo Live<br/>Background / Tela]
        NOTIF_POS[Notificacao<br/>Pos-Jogo]
        REVELACAO[Revelacao<br/>Cinematica]
        EXPERIENCIA[Experiencia<br/>Curva HR + Picos]
    end

    subgraph TORCIDA["EXPERIENCIA COLETIVA"]
        TORCIDOMETRO[Torcidometro<br/>Pulso da Torcida]
        DUELO[Duelo de Torcidas<br/>Comparacao rival]
        RANKING_TORC[Ranking Torcidas<br/>por rodada]
        RANKING_IND[Meu Ranking<br/>dentro da torcida]
    end

    subgraph SHARE["COMPARTILHAMENTO"]
        EDITOR[Editor de Card<br/>4 tipos]
        SHARE_SHEET[Share Sheet]
        POS_SHARE[Pos-Share]
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
    OAUTH_GOOGLE --> TIME
    OAUTH_APPLE --> TIME
    SIGNUP --> TIME

    %% SETUP
    TIME --> CONFIRMA_TIME
    CONFIRMA_TIME --> PRIMING
    PRIMING --> PROVIDER
    PRIMING -.->|Fazer depois| HOME
    PROVIDER --> OS_PERM
    OS_PERM -->|Permitiu| SUCESSO_CONN
    OS_PERM -.->|Negou| HOME
    SUCESSO_CONN --> HOME

    %% CORE NAV
    HOME --> JOGOS
    HOME --> PERFIL
    HOME --> RANKING_TORC
    HOME --> DETALHE_JOGO
    JOGOS --> DETALHE_JOGO
    PERFIL --> CONFIG

    %% CICLO DO JOGO
    DETALHE_JOGO -->|Confirmar presenca| PRE_JOGO
    PRE_JOGO -->|Dia do jogo| LIVE
    LIVE -->|Jogo termina| NOTIF_POS
    NOTIF_POS --> REVELACAO
    REVELACAO --> EXPERIENCIA

    %% TORCIDA
    EXPERIENCIA --> TORCIDOMETRO
    TORCIDOMETRO --> DUELO
    DUELO --> RANKING_TORC
    RANKING_TORC --> RANKING_IND
    HOME -->|Torcidometro resumido| RANKING_TORC

    %% SHARE
    EXPERIENCIA --> EDITOR
    TORCIDOMETRO --> EDITOR
    DUELO --> EDITOR
    EDITOR --> SHARE_SHEET
    SHARE_SHEET --> POS_SHARE
    POS_SHARE --> PERFIL

    %% ATALHOS
    HOME -->|Ultima experiencia| EXPERIENCIA
    NOTIF_POS -.->|Deep link| REVELACAO
```

---

## Fluxo Detalhado: Primeiro Acesso (Happy Path)

```mermaid
flowchart LR
    A([Abrir app]) --> B[Splash<br/>1.5s]
    B --> C[Onboarding<br/>3 telas swipe]
    C --> D[Auth<br/>1 toque social]
    D --> E[Escolha<br/>do Time]
    E --> F[Wearable<br/>Priming]
    F --> G[Permissao OS]
    G --> H[BPM ao vivo]
    H --> I([Home])

    style A fill:#C0392B,color:#fff
    style I fill:#C0392B,color:#fff
```

**Tempo estimado: < 2 minutos** do primeiro toque ate a Home (inclui escolha do time)

---

## Fluxo Detalhado: Ciclo Completo do Jogo

```mermaid
flowchart TD
    subgraph DESCOBERTA["DESCOBERTA"]
        B1[Home ou Jogos]
        B2[Detalhe do Jogo]
        B3{Confirmar<br/>presenca?}
        B4{Onde?}
    end

    subgraph PREPARACAO["PREPARACAO"]
        P1[Jogo salvo<br/>Estadio ou Casa]
        P2[Push 2h antes]
        P3[Pre-Jogo<br/>Checklist]
        P4{Tudo OK?}
        P5[Resolver problema]
    end

    subgraph AO_VIVO["AO VIVO"]
        L1[Monitoramento<br/>automatico inicia]
        L2[Background<br/>coleta HR silenciosa]
        L3[Modo Live<br/>se abrir o app]
        L4[Jogo termina<br/>sync dados]
    end

    subgraph POS_JOGO["POS-JOGO"]
        R1[Push: experiencia pronta]
        R2[Revelacao cinematica<br/>placar + pico + gol]
        R3[Experiencia individual<br/>curva + picos + curiosidades]
    end

    subgraph COLETIVO["EXPERIENCIA COLETIVA"]
        T1[Torcidometro<br/>curva coletiva + ranking]
        T2[Duelo de Torcidas<br/>4 categorias]
    end

    subgraph COMPARTILHAR["COMPARTILHAR"]
        S1[Escolher tipo de card]
        S2[Meu Jogo / Torcida /<br/>Duelo / Momento]
        S3[Preview + estilo]
        S4[Share sheet]
        S5[Card salvo + colecao]
    end

    B1 --> B2
    B2 --> B3
    B3 -->|Sim| B4
    B3 -.->|Nao| B1
    B4 -->|Estadio| P1
    B4 -->|Casa| P1

    P1 -->|Dia do jogo| P2
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

    R3 --> T1
    T1 --> T2

    R3 --> S1
    T1 --> S1
    T2 --> S1
    S1 --> S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
```

---

## Fluxo Detalhado: Duelo de Torcidas

```mermaid
flowchart TD
    START([Jogo terminou<br/>com dados de<br/>ambas torcidas])

    VALIDA{Min. 50<br/>torcedores<br/>por lado?}

    CALC[Calcula metricas<br/>Intensidade / Explosao<br/>Reacao / Sofrimento<br/>Fidelidade / Sintonia]

    VEREDITO[Gera veredito<br/>Tumtum oficial]

    TELA[Tela Duelo<br/>barras comparativas<br/>+ veredito]

    CARD_DUELO[Card tipo Duelo<br/>side-by-side]

    SHARE[Share Sheet]

    DEBATE([Torcidas debatem<br/>nas redes sociais<br/>= viralidade])

    INDISPONIVEL[Mensagem:<br/>'Precisamos de mais<br/>torcedores para o<br/>proximo duelo.<br/>Convide amigos!']

    START --> VALIDA
    VALIDA -->|Sim| CALC
    VALIDA -->|Nao| INDISPONIVEL
    CALC --> VEREDITO
    VEREDITO --> TELA
    TELA --> CARD_DUELO
    CARD_DUELO --> SHARE
    SHARE --> DEBATE

    INDISPONIVEL -->|Convite| DEBATE

    style START fill:#C0392B,color:#fff
    style DEBATE fill:#C0392B,color:#fff
```

---

## Fluxo Detalhado: Compartilhamento (4 tipos de card)

```mermaid
flowchart TD
    START([Trigger:<br/>botao em qualquer<br/>tela pos-jogo])

    TIPO{Tipo de card}
    C1[Meu Jogo<br/>curva individual<br/>+ pico + gol]
    C2[Torcida<br/>curva coletiva<br/>+ stats da torcida]
    C3[Duelo<br/>barras lado a lado<br/>+ veredito]
    C4[Momento<br/>zoom no pico<br/>de um gol]

    STYLE[Estilo<br/>cores time / preto<br/>branco / neon]

    FORMAT{Formato}
    F_STORY[Stories 9:16]
    F_FEED[Feed 1:1]
    F_POST[Post 4:5]

    PREVIEW[Preview tela cheia]
    EDIT{OK?}

    SHARE{Destino}
    INSTA[Instagram]
    TIKTOK[TikTok]
    X_SHARE[X]
    WHATS[WhatsApp]
    SAVE[Salvar]

    DONE([Card salvo<br/>na colecao])

    START --> TIPO
    TIPO --> C1
    TIPO --> C2
    TIPO --> C3
    TIPO --> C4
    C1 --> STYLE
    C2 --> STYLE
    C3 --> STYLE
    C4 --> STYLE
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
    SHARE --> SAVE
    INSTA --> DONE
    TIKTOK --> DONE
    X_SHARE --> DONE
    WHATS --> DONE
    SAVE --> DONE

    style START fill:#C0392B,color:#fff
    style DONE fill:#C0392B,color:#fff
```

---

## Navegacao por Tab Bar

```mermaid
flowchart LR
    subgraph TABS["Tab Bar"]
        direction TB
        T1["Home"]
        T2["Jogos"]
        T3["Torcida"]
        T4["Ranking"]
        T5["Perfil"]
    end

    subgraph HOME_STACK["Stack: Home"]
        direction TB
        H1[Home] --> H2[Detalhe Jogo]
        H1 --> H3[Experiencia]
        H3 --> H4[Editor Card]
        H1 --> H5[Ranking resumido]
    end

    subgraph JOGOS_STACK["Stack: Jogos"]
        direction TB
        J1[Lista Jogos] --> J2[Detalhe Jogo]
        J2 --> J3[Pre-Jogo]
    end

    subgraph TORCIDA_STACK["Stack: Torcida"]
        direction TB
        TO1[Torcidometro] --> TO2[Duelo de Torcidas]
        TO1 --> TO3[Historico]
        TO2 --> TO4[Card Duelo]
    end

    subgraph RANKING_STACK["Stack: Ranking"]
        direction TB
        R1[Ranking Torcidas] --> R2[Meu Ranking]
        R1 --> R3[Conquistas]
        R1 --> R4[Desafios]
    end

    subgraph PERFIL_STACK["Stack: Perfil"]
        direction TB
        P1[Perfil Torcedor] --> P2[Configuracoes]
        P1 --> P3[Colecao Cards]
        P1 --> P4[Jogos anteriores]
    end

    T1 --- HOME_STACK
    T2 --- JOGOS_STACK
    T3 --- TORCIDA_STACK
    T4 --- RANKING_STACK
    T5 --- PERFIL_STACK
```

---

## Mapa de Estados da Tela Home

```mermaid
stateDiagram-v2
    [*] --> SemTime: Primeiro acesso

    SemTime --> SemWearable: Escolheu time

    SemWearable --> SemJogos: Conectou wearable
    SemWearable --> SemJogos: Pulou conexao

    SemJogos --> ComJogo: Confirmou presenca

    ComJogo --> DiaDeJogo: Dia do jogo

    DiaDeJogo --> AoVivo: Jogo comecou

    AoVivo --> PosJogo: Jogo acabou + dados processados

    PosJogo --> ComHistorico: Visualizou experiencia

    ComHistorico --> ComJogo: Confirma proximo jogo

    state SemTime {
        [*] --> TelaEscolhaTime
        TelaEscolhaTime: Redireciona para<br/>escolha do time
    }

    state SemWearable {
        [*] --> BannerConectar
        BannerConectar: Banner persistente<br/>'Conecte seu relogio'<br/>+ proximo jogo do time
    }

    state SemJogos {
        [*] --> EmptyState
        EmptyState: Proximo jogo do time<br/>+ CTA confirmar presenca
    }

    state ComJogo {
        [*] --> CardProximoJogo
        CardProximoJogo: Card jogo com countdown<br/>+ torcedores confirmados
    }

    state DiaDeJogo {
        [*] --> ChecklistPreJogo
        ChecklistPreJogo: Checklist automatico<br/>+ clima do jogo<br/>+ placar torcedores
    }

    state AoVivo {
        [*] --> StatusLive
        StatusLive: Indicador AO VIVO<br/>Placar + BPM + Torcida
    }

    state PosJogo {
        [*] --> CardExperiencia
        CardExperiencia: Card experiencia<br/>+ curva HR + torcidometro
    }

    state ComHistorico {
        [*] --> HistoricoCompleto
        HistoricoCompleto: Proximo jogo +<br/>historico + ranking
    }
```

---

## Ciclo de Engajamento Semanal

```mermaid
flowchart LR
    subgraph SEG_QUA["Seg-Qua: Entre jogos"]
        A1[Ranking da rodada<br/>atualizado]
        A2[Desafio semanal<br/>lancado]
        A3[Push: Ranking<br/>do seu time]
    end

    subgraph QUI_SEX["Qui-Sex: Pre-jogo"]
        B1[Proximo jogo<br/>aparece na Home]
        B2[Confirmar presenca]
        B3[Push: X torcedores<br/>ja confirmaram]
    end

    subgraph SAB_DOM["Sab-Dom: Dia do jogo"]
        C1[Pre-jogo checklist]
        C2[Monitoramento live]
        C3[Pos-jogo: revelacao]
        C4[Experiencia + torcida]
        C5[Duelo + cards]
    end

    SEG_QUA --> QUI_SEX
    QUI_SEX --> SAB_DOM
    SAB_DOM --> SEG_QUA

    style SEG_QUA fill:#f5f5f8,stroke:#C0392B
    style QUI_SEX fill:#f5f5f8,stroke:#E74C3C
    style SAB_DOM fill:#C0392B,color:#fff
```

**O app tem motivo para ser aberto TODOS OS DIAS, nao so em dia de jogo.**

---

## Fluxo de Permissoes (Progressive)

```mermaid
flowchart TD
    subgraph MOMENTO_1["Momento 1: Apos Auth"]
        W1[Escolha do time<br/>identidade do torcedor]
    end

    subgraph MOMENTO_2["Momento 2: Setup"]
        W2[Priming: saude<br/>'medir como seu coracao<br/>reage a cada lance']
        W3[Permissao OS:<br/>HealthKit / Health Connect]
    end

    subgraph MOMENTO_3["Momento 3: Primeiro jogo"]
        N1[Priming: localizacao<br/>'Encontrar jogos<br/>perto de voce']
        N2[Permissao OS:<br/>Localizacao]
    end

    subgraph MOMENTO_4["Momento 4: Pos primeiro jogo"]
        P1[Priming: notificacoes<br/>'Avisamos quando o<br/>resultado da torcida<br/>estiver pronto']
        P2[Permissao OS:<br/>Notificacoes]
    end

    AUTH_OK([Auth completo]) --> W1
    W1 --> W2
    W2 --> W3

    PRIMEIRO_JOGO([Busca jogo]) --> N1
    N1 --> N2

    POS_JOGO([Jogo terminou]) --> P1
    P1 --> P2

    style AUTH_OK fill:#C0392B,color:#fff
    style PRIMEIRO_JOGO fill:#C0392B,color:#fff
    style POS_JOGO fill:#C0392B,color:#fff
```

---

## Inventario de Telas

| # | Tela | Fluxo | Prioridade |
|---|------|-------|------------|
| 0 | Splash | Primeiro acesso | P0 |
| 1 | Onboarding 1/3 | Primeiro acesso | P0 |
| 2 | Onboarding 2/3 | Primeiro acesso | P0 |
| 3 | Onboarding 3/3 | Primeiro acesso | P0 |
| 4 | Login/Cadastro | Auth | P0 |
| 5 | Escolha do Time | Setup | P0 |
| 5b | Confirma Time | Setup | P0 |
| 6 | Wearable Priming | Setup | P0 |
| 7 | Selecao Provider | Setup | P0 |
| 8 | Sucesso Conexao | Setup | P0 |
| 9 | Home | Core | P0 |
| 10 | Jogos (lista) | Core | P0 |
| 11 | Detalhe do Jogo | Core | P0 |
| 12 | Pre-Jogo | Jogo | P0 |
| 13 | Modo Live | Jogo | P1 |
| 14 | Revelacao | Pos-jogo | P0 |
| 15 | Experiencia | Pos-jogo | P0 |
| 16 | Torcidometro | Torcida | P0 |
| 17 | Duelo de Torcidas | Torcida | P1 |
| 18 | Editor de Card | Share | P0 |
| 19 | Ranking Torcidas | Ranking | P1 |
| 20 | Meu Ranking | Ranking | P1 |
| 21 | Perfil Torcedor | Perfil | P0 |
| 22 | Configuracoes | Settings | P1 |

**23 telas | 17 no MVP (P0) | 6 adicionais na Fase 2 (P1)**
