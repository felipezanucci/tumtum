# Tumtum App — UX Structure & User Flows

> Documento de arquitetura UX — Foco: Futebol & Experiencia de Torcida
> Versao: 2.0 | Data: 2026-04-15

---

## Indice

1. [Mapa Geral de Telas](#1-mapa-geral-de-telas)
2. [Fluxo 1 — Primeiro Acesso e Onboarding](#2-fluxo-1--primeiro-acesso-e-onboarding)
3. [Fluxo 2 — Escolha do Time e Conexao Wearable](#3-fluxo-2--escolha-do-time-e-conexao-wearable)
4. [Fluxo 3 — Home e Proximos Jogos](#4-fluxo-3--home-e-proximos-jogos)
5. [Fluxo 4 — Pre-Jogo](#5-fluxo-4--pre-jogo)
6. [Fluxo 5 — Durante o Jogo (Modo Live)](#6-fluxo-5--durante-o-jogo-modo-live)
7. [Fluxo 6 — Pos-Jogo e Experiencia da Torcida](#7-fluxo-6--pos-jogo-e-experiencia-da-torcida)
8. [Fluxo 7 — Torcidometro (Pulso Coletivo)](#8-fluxo-7--torcidometro-pulso-coletivo)
9. [Fluxo 8 — Duelo de Torcidas](#9-fluxo-8--duelo-de-torcidas)
10. [Fluxo 9 — Geracao e Compartilhamento do Card](#10-fluxo-9--geracao-e-compartilhamento-do-card)
11. [Fluxo 10 — Perfil do Torcedor e Colecao](#11-fluxo-10--perfil-do-torcedor-e-colecao)
12. [Fluxo 11 — Ranking e Competicao](#12-fluxo-11--ranking-e-competicao)
13. [Navegacao Global](#13-navegacao-global)
14. [Principios de UX Aplicados](#14-principios-de-ux-aplicados)

---

## 1. Mapa Geral de Telas

```
SPLASH -> ONBOARDING (3 telas) -> AUTH -> MEU TIME + WEARABLE -> HOME
                                                                  |
                          +---------------------------------------+---------------------------------------+
                          v                                       v                                       v
                       JOGOS                                  TORCIDA                                  PERFIL
                          |                                       |                                       |
                          v                                       v                                       v
                   DETALHE JOGO                           TORCIDOMETRO                             CONFIGURACOES
                          |                                       |
                          v                                       v
                   PRE-JOGO (checklist)                   DUELO DE TORCIDAS
                          |                                       |
                          v                                       v
                   MODO LIVE (durante)                    RANKING TORCIDAS
                          |
                          v
                   EXPERIENCIA (pos-jogo)
                          |
                   +------+------+
                   v             v
             GERAR CARD    TORCIDOMETRO
                   |        (ver coletivo)
                   v
             SHARE SHEET
```

---

## 2. Fluxo 1 — Primeiro Acesso e Onboarding

### Principio: Falar a lingua do torcedor desde o primeiro segundo

```
+-----------------------------+
|        TELA 0: SPLASH       |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |   Logo Tumtum     |    |
|    |   (animacao:      |    |
|    |    coracao pulsa   |    |
|    |    no ritmo de     |    |
|    |    torcida)        |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    Duracao: 1.5s            |
|    Transicao: fade out      |
+-----------------------------+
          |
          v
+-----------------------------+
|    TELA 1: ONBOARDING 1/3   |
|    "Sinta o jogo"           |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |  [Animacao: curva  |    |
|    |   HR da torcida    |    |
|    |   subindo no gol   |    |
|    |   com estadio ao   |    |
|    |   fundo]           |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    "Seu coracao conta a     |
|     historia do jogo"       |
|                             |
|    * o o                    |
|                             |
|    [Proximo ->]             |
|    [Pular]                  |
+-----------------------------+
          |
          v
+-----------------------------+
|    TELA 2: ONBOARDING 2/3   |
|    "Sua torcida unida"      |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |  [Animacao: varias |    |
|    |   curvas HR se     |    |
|    |   alinhando no     |    |
|    |   mesmo pico =     |    |
|    |   GOOOL!]          |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    "Veja como a torcida     |
|     inteira sentiu cada     |
|     momento do jogo"        |
|                             |
|    o * o                    |
|                             |
|    [Proximo ->]             |
|    [Pular]                  |
+-----------------------------+
          |
          v
+-----------------------------+
|    TELA 3: ONBOARDING 3/3   |
|    "Quem vibra mais?"       |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |  [Animacao: duas   |    |
|    |   barras lado a    |    |
|    |   lado - torcidas  |    |
|    |   competindo em    |    |
|    |   intensidade]     |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    "Compare sua torcida     |
|     com a rival e prove     |
|     quem vive mais o jogo"  |
|                             |
|    o o *                    |
|                             |
|    [Comecar ->]             |
+-----------------------------+
```

### Regras UX:
- Linguagem 100% futebol: "jogo", "torcida", "gol" — nunca "evento" generico
- Onboarding mostra os 3 pilares: individual, coletivo (torcida), versus (duelo)
- Swipe horizontal entre as 3 telas
- "Pular" sempre visivel
- Animacoes com estetica de estadio/arquibancada
- Onboarding so aparece no PRIMEIRO acesso

---

## 3. Fluxo Auth — Cadastro / Login

### Principio: Maximo 2 toques para entrar

```
+-----------------------------+
|       TELA 4: AUTH           |
|                             |
|    Logo Tumtum              |
|                             |
|    +-------------------+    |
|    | [G] Entrar com     |    |
|    |     Google          |    |
|    +-------------------+    |
|                             |
|    +-------------------+    |
|    | [] Entrar com     |    |
|    |      Apple          |    |
|    +-------------------+    |
|                             |
|    --- ou ---               |
|                             |
|    +-------------------+    |
|    | Email              |    |
|    +-------------------+    |
|    +-------------------+    |
|    | Senha              |    |
|    +-------------------+    |
|                             |
|    [Entrar]                 |
|                             |
|    Nao tem conta? Cadastrar |
|                             |
|    ___________________      |
|    Ao continuar, voce       |
|    aceita os Termos e       |
|    Politica de Privacidade  |
+-----------------------------+
```

### Regras UX:
- Social login como CTAs primarios
- Email/senha como fallback
- Zero campos desnecessarios
- Apos auth: tela de escolha do time (NOVO)

---

## 4. Fluxo 2 — Escolha do Time e Conexao Wearable

### Principio: O time e a identidade central do torcedor no app

```
+-----------------------------+
|  TELA 5: ESCOLHA DO TIME     |
|                             |
|    "Qual e o seu time?"     |
|                             |
|    +-------------------+    |
|    | [busca] Buscar...  |    |
|    +-------------------+    |
|                             |
|    -- Populares em SP --    |
|                             |
|    +--------+ +--------+   |
|    | [escu] | | [escu] |   |
|    | Corin- | | Palmei-|   |
|    | thians | | ras    |   |
|    +--------+ +--------+   |
|    +--------+ +--------+   |
|    | [escu] | | [escu] |   |
|    | Sao    | | Santos |   |
|    | Paulo  | |        |   |
|    +--------+ +--------+   |
|                             |
|    -- Outros times --       |
|    Flamengo, Vasco,         |
|    Cruzeiro, Atletico...    |
|    [Ver todos]              |
|                             |
+-----------------------------+
          |
          v (apos selecionar)
+-----------------------------+
|  TELA 5b: CONFIRMA TIME     |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |   [Escudo grande   |    |
|    |    do time com     |    |
|    |    animacao de     |    |
|    |    entrada]        |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    "Corinthians"            |
|    "Fiel ate o fim!"        |
|    (frase iconica do time)  |
|                             |
|    A interface do app       |
|    se adapta com as cores   |
|    do seu time              |
|                             |
|    [Esse e meu time! ->]    |
|    [Trocar]                 |
|                             |
+-----------------------------+
```

### Regras UX:
- Tela obrigatoria no primeiro acesso (time = identidade)
- Deteccao por geolocalizacao: mostrar times locais primeiro
- Busca com autocomplete para times de qualquer divisao
- Ao confirmar: interface se adapta com cores do time (accent color)
- Pode trocar depois nas configuracoes
- Frase iconica do time gera identificacao emocional imediata

### Conexao Wearable (mesmo fluxo anterior, adaptado):

```
+-----------------------------+
|  TELA 6: WEARABLE PRIMING   |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |  [Ilustracao:      |    |
|    |   torcedor na      |    |
|    |   arquibancada     |    |
|    |   com relogio]     |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    "Conecte seu relogio"    |
|                             |
|    "Vamos medir como seu    |
|     coracao reage a cada    |
|     lance do jogo"          |
|                             |
|    [lock] "Seus dados ficam |
|     seguros e anonimos na   |
|     torcida coletiva."      |
|                             |
|    [Conectar dispositivo]   |
|    [Fazer depois]           |
+-----------------------------+
          |
          v
+-----------------------------+
|  TELA 7: SELECAO PROVIDER   |
|                             |
|    "Qual seu dispositivo?"  |
|                             |
|    +-------------------+    |
|    | [watch] Apple Watch| >  |
|    +-------------------+    |
|    +-------------------+    |
|    | [phone] Galaxy     | >  |
|    |         Watch      |    |
|    +-------------------+    |
|    +-------------------+    |
|    | [watch] Fitbit     | >  |
|    +-------------------+    |
|    +-------------------+    |
|    | [watch] Garmin     | >  |
|    +-------------------+    |
|    +-------------------+    |
|    | [phone] Mi Band /  | >  |
|    |    Outro            |    |
|    +-------------------+    |
|                             |
+-----------------------------+
          |
          v
+-----------------------------+
|  TELA 8: SUCESSO CONEXAO    |
|                             |
|    +-------------------+    |
|    |                   |    |
|    |   [check animado] |    |
|    |                   |    |
|    +-------------------+    |
|                             |
|    "Conectado!"             |
|    "Apple Watch de Felipe"  |
|                             |
|    BPM atual: 72 [heart]    |
|                             |
|    "Pronto pra sentir o     |
|     proximo jogo do         |
|     Corinthians!"           |
|                             |
|    [Bora! ->]               |
+-----------------------------+
```

### Regras UX:
- Linguagem de futebol no priming ("cada lance do jogo")
- Mencao a privacidade: dados anonimos na torcida coletiva
- Sucesso cita o time do usuario — reforco emocional
- CTA usa giria de torcida ("Bora!")

---

## 5. Fluxo 3 — Home e Proximos Jogos

### Principio: O torcedor entra e ja ve quando e o proximo jogo do time dele

```
+-----------------------------+
|  TELA 9: HOME                |
|                             |
|  +- Header ----------------+|
|  | [escudo] Tumtum  [avtr] ||
|  +--------------------------+|
|                             |
|  -- Proximo jogo -----------|
|                             |
|  +------------------------+ |
|  | CORINTHIANS             | |
|  |        vs               | |
|  | PALMEIRAS               | |
|  |                         | |
|  | [escu]  X  [escu]       | |
|  |                         | |
|  | Neo Quimica Arena       | |
|  | Dom, 20 Abr - 16h       | |
|  |                         | |
|  | "Faltam 5 dias"         | |
|  |                         | |
|  | [timer] 247 torcedores  | |
|  |    ja confirmaram       | |
|  |                         | |
|  | [Eu vou estar la!]      | |
|  | [Vou assistir de casa]  | |
|  +------------------------+ |
|                             |
|  -- Ultimo jogo ------------|
|                             |
|  +------------------------+ |
|  | COR 2 x 1 SAO          | |
|  | +--------------------+  | |
|  | | [mini curva HR     |  | |
|  | |  com 2 picos nos   |  | |
|  | |  gols marcados]    |  | |
|  | +--------------------+  | |
|  | [heart] Pico: 142bpm    | |
|  | [ball] Gol do Yuri      | |
|  | [Ver experiencia ->]    | |
|  +------------------------+ |
|                             |
|  -- Torcidometro Geral -----|
|                             |
|  +------------------------+ |
|  | [heart] Torcida mais    | |
|  |    intensa da rodada:   | |
|  |                         | |
|  |  1. Flamengo  [bar====] | |
|  |  2. Corinthians [bar===]| |
|  |  3. Palmeiras  [bar== ] | |
|  |                         | |
|  | [Ver ranking ->]        | |
|  +------------------------+ |
|                             |
|  +- Tab Bar ---------------+|
|  | [home] [ball] [heart]   ||
|  | [trophy] [user]         ||
|  | Home Jogos Torcida      ||
|  | Ranking Perfil          ||
|  +--------------------------+|
+-----------------------------+
```

### Home — Estado vazio (sem jogos anteriores):

```
+-----------------------------+
|  HOME (primeiro acesso)      |
|                             |
|  [Proximo jogo do time]     |
|  (mesmo card acima)         |
|                             |
|  -- Sua primeira vez? ------|
|                             |
|  +------------------------+ |
|  |  [ilustracao: torcida   | |
|  |   vibrando]             | |
|  |                         | |
|  |  "Confirme presenca no  | |
|  |   proximo jogo e veja   | |
|  |   como seu coracao vive | |
|  |   cada lance"           | |
|  |                         | |
|  |  [Encontrar jogo]       | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Regras UX:
- Home = 100% focada no time do usuario
- Proximo jogo sempre no topo com countdown
- Social proof: "247 torcedores ja confirmaram"
- Duas opcoes de presenca: estadio OU em casa (ambas monitoram)
- Torcidometro resumido na Home = hook de engajamento
- Tab bar com 5 abas: Home, Jogos, Torcida, Ranking, Perfil

---

## 6. Fluxo 4 — Busca e Selecao de Jogo

### Principio: Jogos do meu time primeiro, sempre

```
+-----------------------------+
|  TELA 10: JOGOS              |
|                             |
|  -- Meu time ---------------| 
|  [Todos] [Brasileirao]      |
|  [Copa do Brasil] [Liberta] |
|                             |
|  +------------------------+ |
|  | Dom 20/04              | |
|  | COR vs PAL             | |
|  | Brasileirao - Rod. 5   | |
|  | Neo Quimica - 16h      | |
|  | [Confirmar presenca]   | |
|  +------------------------+ |
|  +------------------------+ |
|  | Qua 23/04              | |
|  | COR vs BOC             | |
|  | Libertadores - Grupo   | |
|  | Neo Quimica - 21h30    | |
|  | [Confirmar presenca]   | |
|  +------------------------+ |
|                             |
|  -- Outros jogos hoje ------|
|                             |
|  +------------------------+ |
|  | FLA vs FLU  | 18h30    | |
|  | Maracana    | [heart]423| |
|  +------------------------+ |
|  +------------------------+ |
|  | SAO vs SAN  | 21h      | |
|  | Morumbi     | [heart]189| |
|  +------------------------+ |
|                             |
+-----------------------------+
          |
          v (toque no jogo)
+-----------------------------+
|  TELA 11: DETALHE DO JOGO    |
|                             |
|  +------------------------+ |
|  |                         | |
|  |  [escu] CORINTHIANS     | |
|  |          vs              | |
|  |  [escu] PALMEIRAS       | |
|  |                         | |
|  |  Brasileirao Serie A    | |
|  |  Rodada 5               | |
|  |                         | |
|  +------------------------+ |
|                             |
|  [pin] Neo Quimica Arena    |
|  [cal] Dom, 20 Abr - 16h   |
|  [tv] Premiere              |
|                             |
|  -- Torcida confirmada -----|
|                             |
|  +------------------------+ |
|  | [heart] 247 torcedores  | |
|  |   Corinthians           | |
|  |                         | |
|  | [heart] 198 torcedores  | |
|  |   Palmeiras             | |
|  |                         | |
|  | "Fiel ja esta em        | |
|  |  vantagem!"             | |
|  +------------------------+ |
|                             |
|  -- Historico de confronto -|
|                             |
|  Ultimo Derby:              |
|  COR 1x0 PAL               |
|  Torcida Corinthians:       |
|  [heart] Pico medio: 138bpm|
|  Torcida Palmeiras:         |
|  [heart] Pico medio: 141bpm|
|                             |
|  +------------------------+ |
|  |                         | |
|  | [Eu vou estar la!]     | |
|  | (botao primario)        | |
|  |                         | |
|  | [Vou assistir de casa]  | |
|  | (botao secundario)      | |
|  |                         | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Regras UX:
- Jogos do time do usuario SEMPRE primeiro
- Filtro por competicao (Brasileirao, Copa, Libertadores)
- Social proof competitivo: "Fiel ja esta em vantagem!" (mais torcedores confirmados)
- Historico de confronto com dados Tumtum de jogos anteriores
- DUAS opcoes de presenca: estadio e de casa — ambas contam para a torcida
- Contagem de torcedores visivel = pressao social positiva

---

## 7. Fluxo 5 — Pre-Jogo

### Principio: Criar clima de jogo e garantir que tudo funciona

```
+-----------------------------+
|  TELA 12: PRE-JOGO           |
|  (aparece no dia do jogo)    |
|                             |
|  +------------------------+ |
|  | [escu] COR vs PAL      | |
|  |  HOJE - 16h             | |
|  |  Neo Quimica Arena      | |
|  +------------------------+ |
|                             |
|  -- Checklist ---------------| 
|                             |
|  [OK] Wearable conectado   |
|     "Apple Watch - 74 bpm" |
|                             |
|  [OK] Bateria OK            |
|     "82% - suficiente"     |
|                             |
|  [OK] Jogo confirmado       |
|     "Monitoramento as 16h" |
|                             |
|  -- Clima do jogo -----------|
|                             |
|  +------------------------+ |
|  |  [heart] 312 vs 267    | |
|  |  COR        PAL        | |
|  |  torcedores confirmados | |
|  |                         | |
|  |  "A Fiel esta 17%       | |
|  |   maior. Bora manter!"  | |
|  +------------------------+ |
|                             |
|  +------------------------+ |
|  |  "Monitoramento comeca  | |
|  |   automaticamente       | |
|  |   as 16:00"             | |
|  |                         | |
|  |  [Iniciar agora]        | |
|  +------------------------+ |
|                             |
|  [tip] Coloque o celular    |
|  no bolso e viva o jogo!    |
|                             |
+-----------------------------+
```

### Notificacao push (2h antes):

```
+-----------------------------+
| [bell] PUSH NOTIFICATION    |
|                             |
| [heart] Tumtum              |
| "COR x PAL em 2 horas!     |
|  312 fieis ja prontos.      |
|  Bora fazer a Fiel pulsar!" |
|                             |
| [Abrir] [Ignorar]          |
+-----------------------------+
```

### Regras UX:
- Notificacao com linguagem de torcida e contagem social
- Pre-jogo mostra "placar" de torcedores confirmados (competicao antes do jogo)
- Checklist automatico resolve problemas ANTES do apito
- "Iniciar agora" para quem quer capturar pre-jogo / aquecimento
- Monitoramento inicia automaticamente no horario do jogo

---

## 8. Fluxo 6 — Durante o Jogo (Modo Live)

### Principio: Background por padrao, mas se abrir = experiencia de estadio

```
+-----------------------------+
|  ESTADO: BACKGROUND          |
|                             |
|  [App coletando HR silencio-|
|   samente em background]    |
|                             |
|  - Coleta a cada 1-5s       |
|  - Salva localmente         |
|  - Sync quando wifi         |
|  - Zero notificacoes        |
|    durante o jogo           |
+-----------------------------+

SE o usuario abrir o app durante o jogo:

+-----------------------------+
|  TELA 13: MODO LIVE          |
|                             |
|  +------------------------+ |
|  | [escu] COR 1x0 PAL     | |
|  |       AO VIVO 67'       | |
|  +------------------------+ |
|                             |
|         [heart] 134        |
|              bpm            |
|    (numero grande,          |
|     pulsando, na cor        |
|     do time)                |
|                             |
|  +------------------------+ |
|  |                         | |
|  |  [Curva HR em tempo     | |
|  |   real, ultimos 5min]   | |
|  |                         | |
|  |  ~~~~~/\~~~~~~/\~~~~~   | |
|  |       GOL!              | |
|  |                         | |
|  +------------------------+ |
|                             |
|  -- Pulso da Torcida -------|
|                             |
|  +------------------------+ |
|  | Fiel agora:             | |
|  | [heart] 128bpm medio    | |
|  | [users] 312 torcedores  | |
|  |                         | |
|  | Alviverde agora:        | |
|  | [heart] 119bpm medio    | |
|  | [users] 267 torcedores  | |
|  |                         | |
|  | "Fiel 7% mais intensa!" | |
|  +------------------------+ |
|                             |
|  -- Timeline do jogo -------|
|  67' COR [ball] Yuri        |
|  45' Intervalo               |
|  23' PAL [card] Falta dura  |
|  12' COR [corner] Escanteio |
|                             |
|  +------------------------+ |
|  |  [tip] Guarde o celular| |
|  |  e viva o jogo!         | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Regras UX:
- Modo Live mostra PLACAR REAL atualizado via API-Football
- BPM pessoal grande + pulso coletivo da torcida (NOVO)
- Comparacao em tempo real com torcida adversaria
- Timeline do jogo com eventos reais (gols, cartoes, escanteios)
- Mensagem "guarde o celular" — experiencia real > app
- Tela com brilho reduzido automaticamente

---

## 9. Fluxo 7 — Pos-Jogo e Experiencia da Torcida

### Principio: Revelar o jogo pelos batimentos — seu e da torcida inteira

### Notificacao pos-jogo (30min depois):

```
+-----------------------------+
| [bell] PUSH NOTIFICATION    |
|                             |
| [heart] Tumtum              |
| "COR 2x1 PAL - Que jogo!   |
|  Veja como a Fiel viveu     |
|  cada gol. Seu coracao      |
|  atingiu 148bpm!"           |
|                             |
| [Ver agora ->]              |
+-----------------------------+
```

### Tela de revelacao:

```
+-----------------------------+
|  TELA 14: REVELACAO          |
|  (tela cheia, imersiva)      |
|                             |
|  [Fundo com cores do time]  |
|                             |
|  [escu] CORINTHIANS         |
|         2 x 1               |
|         PALMEIRAS [escu]    |
|                             |
|  "Neo Quimica Arena"        |
|  "20 de Abril de 2026"      |
|                             |
|  (pausa dramatica 2s)       |
|                             |
|  "Voce viveu 97 minutos     |
|   de pura emocao"           |
|                             |
|  (pausa 1.5s)               |
|                             |
|  "Seu coracao bateu mais    |
|   forte quando..."          |
|                             |
|  (animacao: numero sobe)    |
|         148                 |
|         bpm                 |
|                             |
|  "Yuri marcou o gol da      |
|   virada aos 78'"           |
|                             |
|  [Ver jogo completo ->]     |
|                             |
+-----------------------------+
```

### Tela de experiencia completa:

```
+-----------------------------+
|  TELA 15: EXPERIENCIA        |
|                             |
|  +------------------------+ |
|  | [escu] COR 2x1 PAL     | |
|  | Neo Quimica - 20 Abr   | |
|  +------------------------+ |
|                             |
|  -- Meus stats -------------|
|  +------+ +------+ +------+ |
|  | 148  | |  97  | | 97   | |
|  | pico | |media | | min  | |
|  +------+ +------+ +------+ |
|                             |
|  -- Minha curva HR ----------|
|  +------------------------+ |
|  |                         | |
|  |     /\      /\          | |
|  |    /  \    / |\         | |
|  |   /    \  /  | \        | |
|  |  /      \/   |  \       | |
|  | / GOL 1'  GOL 78'\      | |
|  |/                    \   | |
|  |                         | |
|  | [Marcadores nos gols    | |
|  |  com minuto e jogador]  | |
|  |                         | |
|  | <- arrastar timeline -> | |
|  +------------------------+ |
|                             |
|  -- Meus picos -------------|
|                             |
|  [1st] 148bpm - 78' GOL     |
|     Yuri Alberto             |
|     "Seu maior pico!"       |
|     [Criar card ->]         |
|                             |
|  [2nd] 142bpm - 12' GOL     |
|     Romero                   |
|     [Criar card ->]         |
|                             |
|  [3rd] 131bpm - 90+3'       |
|     Apito final              |
|     [Criar card ->]         |
|                             |
|  -- Curiosidades ------------|
|                             |
|  "Voce ficou acima de       |
|   120bpm por 23 minutos"    |
|                             |
|  "No intervalo seu coracao  |
|   descansou pra 72bpm"     |
|                             |
|  "Voce reagiu ao gol do     |
|   Yuri 0.3s antes da        |
|   media da torcida!"        |
|                             |
|  +------------------------+ |
|  | [Ver pulso da torcida]  | |
|  | (botao secundario)      | |
|  +------------------------+ |
|  +------------------------+ |
|  | [Gerar card do jogo]    | |
|  | (botao primario)        | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Interacao na curva HR:

```
+-------------------------------+
|  CURVA HR — INTERACAO          |
|                               |
|  Toque longo em qualquer      |
|  ponto da curva:              |
|                               |
|  +-------------------------+  |
|  |         * <- tooltip    |  |
|  |     /\ |134bpm          |  |
|  |    /  \|67'              |  |
|  |   /    |"Escanteio"     |  |
|  +-------------------------+  |
|                               |
|  Arrastar = scrub pela        |
|  timeline (como Spotify)      |
|                               |
|  Pinch = zoom in/out          |
|                               |
+-------------------------------+
```

### Regras UX:
- Revelacao cita PLACAR, JOGADOR e MINUTO do gol — contexto real
- Curiosidades de futebol: "reagiu 0.3s antes da media da torcida" = viral
- Picos sempre correlacionados com eventos do jogo (gols, cartoes, defesas)
- CTA duplo: "Gerar card" (share) + "Ver pulso da torcida" (social/coletivo)
- Curva HR com markers nos momentos do jogo (timeline de futebol)

---

## 10. Fluxo 8 — Torcidometro (Pulso Coletivo)

### Principio: Voce nao torce sozinho — ver a torcida unida e o hook emocional

```
+-----------------------------+
|  TELA 16: TORCIDOMETRO       |
|  (Pulso da Torcida)          |
|                             |
|  +------------------------+ |
|  | [escu] COR 2x1 PAL     | |
|  | Torcida Fiel - 312      | |
|  | torcedores monitorados  | |
|  +------------------------+ |
|                             |
|  -- Curva coletiva ----------|
|  +------------------------+ |
|  |                         | |
|  | LINHA VERMELHA:          | |
|  | media BPM de toda a     | |
|  | torcida Corinthians     | |
|  |                         | |
|  |     /\      /\          | |
|  |    /  \    / |\         | |
|  |---/----\--/--|-\---     | |
|  |  /      \/   |  \       | |
|  | /              \        | |
|  |                         | |
|  | LINHA FINA BRANCA:       | |
|  | seu BPM pessoal          | |
|  |                         | |
|  | [Markers nos gols]      | |
|  +------------------------+ |
|                             |
|  -- Momento mais intenso ----|
|                             |
|  +------------------------+ |
|  | 78' - GOL DO YURI       | |
|  |                         | |
|  | [heart] 143bpm medio    | |
|  |   da torcida            | |
|  |                         | |
|  | [heart] Voce: 148bpm    | |
|  |   (acima da media!)     | |
|  |                         | |
|  | 98% da torcida reagiu   | |
|  | em menos de 2 segundos  | |
|  |                         | |
|  | [chart] Distribuicao:   | |
|  | < 100bpm: 2%            | |
|  | 100-120:  15%           | |
|  | 120-140:  51%           | |
|  | 140-160:  29%           | |
|  | > 160bpm: 3%            | |
|  +------------------------+ |
|                             |
|  -- Ranking de reacao -------|
|                             |
|  "Voce reagiu mais rapido   |
|   que 73% da torcida"       |
|                             |
|  Seu ranking: #84 de 312    |
|                             |
|  +------------------------+ |
|  | [Comparar com rival ->] | |
|  +------------------------+ |
|  +------------------------+ |
|  | [Gerar card coletivo]   | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Dados coletivos exibidos:
- BPM medio da torcida ao longo do jogo (curva agregada)
- Seu BPM sobreposto na curva da torcida
- Distribuicao de intensidade por faixa de BPM
- Percentual de torcedores que reagiram a cada lance
- Seu ranking de velocidade de reacao dentro da torcida
- Tempo medio de reacao da torcida a cada gol

### Regras UX:
- Torcidometro e a tela SOCIAL do app — ver-se como parte do grupo
- Curva coletiva = media anonimizada (privacidade preservada)
- Seu BPM pessoal sobreposto na curva coletiva = "eu estava la"
- Ranking dentro da torcida = gamificacao natural
- Distribuicao de BPM humaniza os dados ("51% da torcida passou de 120bpm")
- CTA para "Comparar com rival" leva ao Duelo de Torcidas

---

## 11. Fluxo 9 — Duelo de Torcidas

### Principio: A rivalidade ja existe — Tumtum da dados pra ela

### Fase 1 (MVP): Pos-jogo, dados agregados

```
+-----------------------------+
|  TELA 17: DUELO DE TORCIDAS  |
|  (Pos-jogo)                  |
|                             |
|  +------------------------+ |
|  |  COR 2x1 PAL            | |
|  |  Neo Quimica - 20 Abr   | |
|  +------------------------+ |
|                             |
|  "QUEM VIVEU MAIS           |
|   O JOGO?"                  |
|                             |
|  +------------------------+ |
|  |                         | |
|  | CORINTHIANS  PALMEIRAS  | |
|  |                         | |
|  | [escu]       [escu]     | |
|  |                         | |
|  | 312          267        | |
|  | torcedores   torcedores | |
|  |                         | |
|  | INTENSIDADE:             | |
|  | [======>  ] [=====>   ] | |
|  |  138bpm      131bpm     | |
|  |  medio       medio      | |
|  |                         | |
|  | EXPLOSAO NO GOL:        | |
|  | [========>] [=>       ] | |
|  |  +47bpm      +12bpm     | |
|  |  no gol 1    no gol 1   | |
|  |                         | |
|  | REACAO:                  | |
|  | [=======> ] [=====>   ] | |
|  |  1.2s        2.8s       | |
|  |  tempo medio tempo medio| |
|  |                         | |
|  | SOFRIMENTO:              | |
|  | [====>    ] [========>] | |
|  |  leve        intenso    | |
|  |  (venceu)    (perdeu)   | |
|  |                         | |
|  +------------------------+ |
|                             |
|  +------------------------+ |
|  |                         | |
|  |  VEREDITO TUMTUM:       | |
|  |                         | |
|  |  "A Fiel viveu mais     | |
|  |   intensamente em 3     | |
|  |   de 4 categorias.      | |
|  |   Vitoria dentro e      | |
|  |   fora de campo!"       | |
|  |                         | |
|  +------------------------+ |
|                             |
|  [Gerar card do duelo ->]   |
|  [Compartilhar resultado]   |
|                             |
+-----------------------------+
```

### Categorias do Duelo:

| Categoria | O que mede | Calculo |
|-----------|-----------|---------|
| Intensidade | BPM medio durante o jogo | Media de todos os torcedores |
| Explosao | Reacao aos gols | Delta BPM (pico - baseline) no momento do gol |
| Reacao | Velocidade de reacao | Tempo entre o gol e o pico de BPM |
| Sofrimento | Tensao em momentos criticos | BPM medio em lances defensivos e gols sofridos |
| Fidelidade | Quem ficou ate o fim | % de torcedores monitorando ate o apito final |
| Sintonia | Sincronia da torcida | % de torcedores com pico no mesmo segundo |

### Fase 2 (Futura): Duelo em tempo real durante o jogo

```
+-----------------------------+
|  DUELO LIVE (futuro)         |
|                             |
|  +------------------------+ |
|  |  AO VIVO - 67'          | |
|  |                         | |
|  |  COR         PAL        | |
|  | [heart]128  119[heart]  | |
|  |  bpm medio   bpm medio  | |
|  |                         | |
|  |  [==========|======   ] | |
|  |  FIEL          ALVIVERDE| |
|  |                         | |
|  |  "Fiel 7% mais          | |
|  |   intensa agora!"       | |
|  |                         | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Regras UX:
- Duelo so aparece quando ha dados de AMBAS as torcidas
- Minimo de torcedores para exibir: 50 por lado (representatividade)
- Categorias sao desenhadas para gerar debate e compartilhamento
- "Sofrimento" e a metrica que transforma derrota em orgulho
- "Veredito Tumtum" e o selo oficial — conteudo viral
- Card do duelo = formato horizontal (side by side) para social

---

## 12. Fluxo 10 — Geracao e Compartilhamento do Card

### Principio: Cards de futebol sao o motor viral — devem parecer transmissao esportiva

```
+-----------------------------+
|  TELA 18: EDITOR DE CARD     |
|                             |
|  -- Tipo de card ------------|
|  [Meu jogo] [Torcida]      |
|  [Duelo] [Momento]          |
|                             |
|  +------------------------+ |
|  |                         | |
|  |  PREVIEW DO CARD        | |
|  |  (tempo real)           | |
|  |                         | |
|  |  +-----------------+    | |
|  |  |  TUMTUM          |    | |
|  |  |                  |    | |
|  |  |  COR 2x1 PAL    |    | |
|  |  |                  |    | |
|  |  |  [curva HR]      |    | |
|  |  |  [heart] 148bpm  |    | |
|  |  |  GOL 78' Yuri    |    | |
|  |  |                  |    | |
|  |  |  @felipe         |    | |
|  |  |  #FielAteFim     |    | |
|  |  +-----------------+    | |
|  |                         | |
|  +------------------------+ |
|                             |
|  -- Estilo ------------------|
|  [cores time] [preto]       |
|  [branco] [neon]            |
|                             |
|  -- Formato -----------------|
|  [Stories 9:16] [Feed 1:1]  |
|  [Post 4:5]                 |
|                             |
|  +------------------------+ |
|  | [Compartilhar ->]       | |
|  +------------------------+ |
|  [Salvar na galeria]        |
|                             |
+-----------------------------+
```

### 4 Tipos de Card:

```
CARD 1: MEU JOGO (individual)
+-----------------+
|  TUMTUM          |
|  COR 2x1 PAL    |
|  [curva HR]      |
|  [heart] 148bpm  |
|  GOL 78' Yuri    |
|  @felipe         |
+-----------------+

CARD 2: TORCIDA (coletivo)
+-----------------+
|  TUMTUM          |
|  TORCIDA FIEL    |
|  312 coracoes    |
|  [curva coletiva]|
|  143bpm no gol   |
|  #FielAteFim     |
+-----------------+

CARD 3: DUELO (versus)
+-----------------+
|  TUMTUM          |
|  COR    vs   PAL |
|  [barra COR >>>]|
|  [barra PAL >> ]|
|  Fiel viveu mais|
|  #DerbyDaFiel   |
+-----------------+

CARD 4: MOMENTO (pico unico)
+-----------------+
|  TUMTUM          |
|  78' GOOOL!      |
|  [zoom na curva  |
|   no momento     |
|   do gol]        |
|  148bpm          |
|  Yuri Alberto    |
|  @felipe         |
+-----------------+
```

### Regras UX:
- 4 tipos de card = 4x mais conteudo compartilhavel
- Card de Duelo e o mais viral (rivalidade gera debate)
- Card de Torcida gera orgulho coletivo
- Card de Momento = ideal pra Stories (vertical, dramatico)
- Hashtags automaticas por time (#FielAteFim, #AvantiPalestra)
- Estilo "cores do time" como padrao — reforco de identidade
- Pre-gerado em background — zero espera

---

## 13. Fluxo 11 — Ranking e Competicao

### Principio: Gamificacao via rivalidade real — sem pontos artificiais

```
+-----------------------------+
|  TELA 19: RANKING TORCIDAS   |
|  (Tab: Ranking)              |
|                             |
|  -- Tipo de ranking ---------|
|  [Rodada] [Campeonato]      |
|  [Historico]                |
|                             |
|  -- Ranking da Rodada 5 ----|
|                             |
|  "Torcida mais intensa      |
|   da rodada:"               |
|                             |
|  +------------------------+ |
|  |  1. [escu] Flamengo     | |
|  |     [bar=============]  | |
|  |     141bpm medio        | |
|  |     1.247 torcedores    | |
|  |                         | |
|  |  2. [escu] Corinthians  | |
|  |     [bar===========  ]  | |
|  |     138bpm medio        | |
|  |     312 torcedores      | |
|  |     "Seu time!"         | |
|  |                         | |
|  |  3. [escu] Palmeiras    | |
|  |     [bar==========   ]  | |
|  |     135bpm medio        | |
|  |                         | |
|  |  4. [escu] Sao Paulo    | |
|  |     [bar=========    ]  | |
|  |     131bpm medio        | |
|  |                         | |
|  |  ...                    | |
|  |  20. [escu] Cuiaba      | |
|  |     [bar===          ]  | |
|  |     98bpm medio         | |
|  +------------------------+ |
|                             |
|  -- Conquistas do seu time --|
|                             |
|  +------------------------+ |
|  | [trophy] "Torcida mais  | |
|  |  explosiva" na Rod. 3   | |
|  |                         | |
|  | [trophy] "Sofredor-mor" | |
|  |  no Derby da Rod. 5     | |
|  |                         | |
|  | [trophy] "100% fiel"    | |
|  |  3 jogos consecutivos   | |
|  |  com 90%+ monitorando   | |
|  |  ate o final            | |
|  +------------------------+ |
|                             |
+-----------------------------+
```

### Sistema de Conquistas por Torcida:

| Conquista | Criterio | Icone |
|-----------|----------|-------|
| Torcida Inferno | Maior BPM medio da rodada | [fire] |
| Explosao Maxima | Maior pico coletivo num gol | [explosion] |
| Sofredor-mor | Maior BPM medio em derrota | [sweat] |
| Muro de Ferro | Menor BPM medio em vitoria (sangue frio) | [shield] |
| 100% Fiel | 90%+ torcedores ate o apito final | [medal] |
| Sintonia Total | Maior % de torcedores reagindo no mesmo segundo | [wave] |
| Madrugadao | Mais torcedores em jogo depois das 22h | [moon] |
| Reacao Relampago | Tempo medio de reacao mais rapido | [lightning] |

### Ranking Individual (dentro do time):

```
+-----------------------------+
|  TELA 20: MEU RANKING        |
|  (dentro da minha torcida)   |
|                             |
|  +------------------------+ |
|  | [avatar] Felipe         | |
|  | Torcedor Fiel desde     | |
|  | Abr 2026                | |
|  |                         | |
|  | Ranking na Fiel:        | |
|  | #84 de 312 torcedores   | |
|  |                         | |
|  | -- Stats acumulados --  | |
|  | Jogos: 3                | |
|  | Pico max: 156bpm        | |
|  | Media geral: 112bpm     | |
|  | Reacao media: 1.1s      | |
|  +------------------------+ |
|                             |
|  -- Titulos pessoais --------|
|                             |
|  [trophy] "Coracao Fiel"    |
|  Presente em 100% dos jogos |
|                             |
|  [trophy] "Reacao de Goleiro"|
|  Top 10% reacao mais rapida |
|                             |
|  [trophy] "Maximo BPM"      |
|  156bpm no Derby            |
|                             |
|  -- Proxima conquista -------|
|                             |
|  [lock] "Infarto Fiel"      |
|  Atinja 160bpm em um jogo   |
|  Progresso: 156/160          |
|  [bar=================>  ]  |
|                             |
+-----------------------------+
```

### Mecanicas de Competicao (hooks de engajamento):

1. RANKING POR RODADA
   - Cada rodada do Brasileirao tem ranking de torcidas
   - Resetado a cada rodada = sempre relevante
   - "Seu time caiu para 5o lugar. Bora virar no proximo jogo!"

2. RANKING ACUMULADO
   - Soma de todas as rodadas = campeonato paralelo
   - "Corinthians e o 2o time mais intenso do Brasileirao"

3. DESAFIOS SEMANAIS
   - "Desafio da rodada: Consiga 30+ torcedores acima de 130bpm"
   - Objetivo coletivo = incentiva recrutamento
   - Desbloqueio de conquistas especiais

4. CONFRONTO DIRETO
   - Em classicos (Derby, Fla-Flu, Grenal):
   - "Duelo especial: quem vence o Derby do coracao?"
   - Card especial desbloqueado so no classico

5. META COLETIVA
   - "Se 500 fieis monitorarem o proximo jogo,
     desbloqueamos o card exclusivo da Arena"
   - Incentiva compartilhamento e convites

6. RECRUTAMENTO
   - "Convide amigos para fortalecer a Fiel"
   - "Cada torcedor conta no ranking!"
   - Link de convite com escudo do time

### Regras UX:
- Rankings usam dados REAIS de jogos — nao pontos artificiais
- Conquistas sao nomeadas com giria de futebol
- Ranking por rodada mantem relevancia semanal
- Proxima conquista com barra de progresso = motivacao
- Confronto direto em classicos = engajamento maximo
- Meta coletiva transforma torcedores em recrutadores
- Desafios semanais dao motivo para abrir o app fora de dia de jogo

---

## 14. Fluxo 12 — Perfil do Torcedor e Colecao

```
+-----------------------------+
|  TELA 21: PERFIL TORCEDOR    |
|                             |
|  +------------------------+ |
|  |  [Avatar]               | |
|  |  Felipe Zanucci         | |
|  |  @felipe                | |
|  |                         | |
|  |  [escu] Corinthians     | |
|  |  "Fiel desde Abr 2026"  | |
|  |                         | |
|  |  3 jogos | 8 cards      | |
|  |  #84 ranking na Fiel    | |
|  +------------------------+ |
|                             |
|  -- Titulos ------------------|
|  [trophy][trophy][trophy]   |
|  (badges horizontais)       |
|                             |
|  -- Meus cards --------------|
|  (grid 2 colunas)           |
|                             |
|  +------+ +------+         |
|  |card 1| |card 2|         |
|  |      | |      |         |
|  +------+ +------+         |
|  +------+ +------+         |
|  |card 3| |card 4|         |
|  |      | |      |         |
|  +------+ +------+         |
|                             |
|  -- Meus jogos --------------|
|  +------------------------+ |
|  | COR 2x1 PAL - 20 Abr   | |
|  | [heart] 148bpm - 3 picos| |
|  +------------------------+ |
|  | COR 1x1 SAN - 13 Abr   | |
|  | [heart] 134bpm - 2 picos| |
|  +------------------------+ |
|  | COR 3x0 GOI - 06 Abr   | |
|  | [heart] 128bpm - 4 picos| |
|  +------------------------+ |
|                             |
|  [gear] Configuracoes       |
|                             |
+-----------------------------+
```

### Configuracoes:

```
+-----------------------------+
|  TELA 22: CONFIGURACOES      |
|                             |
|  -- Conta --                |
|  Nome                    >  |
|  Email                   >  |
|  Senha                   >  |
|                             |
|  -- Meu time --             |
|  Corinthians         [>]    |
|  (trocar time)              |
|                             |
|  -- Dispositivo --          |
|  Apple Watch          [OK]  |
|  [Adicionar dispositivo]    |
|                             |
|  -- Privacidade --          |
|  Perfil publico        [*]  |
|  Incluir meu BPM na   [*]  |
|    torcida coletiva         |
|  Mostrar no ranking    [*]  |
|  Dados de saude        [>]  |
|                             |
|  -- Notificacoes --         |
|  Pre-jogo              [*]  |
|  Pos-jogo              [*]  |
|  Ranking da rodada     [*]  |
|  Desafios semanais     [o]  |
|                             |
|  -- Sobre --                |
|  Termos de uso           >  |
|  Politica de privacidade >  |
|  Versao 1.0.0               |
|                             |
|  [Sair da conta]            |
|  [Excluir minha conta]      |
|                             |
+-----------------------------+
```

---

## 15. Navegacao Global

### Tab Bar:

```
+------------------------------------------+
|                                          |
| [home]  [ball]  [heart]  [trophy] [user] |
|  Home   Jogos   Torcida  Ranking  Perfil |
|                                          |
+------------------------------------------+
```

### Hierarquia:

```
Tab 1: HOME
+-- Proximo jogo -> Detalhe jogo
+-- Ultimo jogo -> Experiencia -> Card
+-- Torcidometro resumido -> Ranking

Tab 2: JOGOS
+-- Lista de jogos (meu time primeiro)
+-- Filtro por competicao
+-- Detalhe jogo -> Confirmar presenca

Tab 3: TORCIDA
+-- Torcidometro (ultimo jogo)
+-- Duelo de Torcidas
+-- Historico de torcidas

Tab 4: RANKING
+-- Ranking de torcidas (rodada/campeonato)
+-- Meu ranking individual
+-- Conquistas e titulos
+-- Desafios da semana

Tab 5: PERFIL
+-- Stats pessoais
+-- Colecao de cards
+-- Lista de jogos
+-- Configuracoes
```

### Gestos globais:

| Gesto | Acao |
|-------|------|
| Swipe right (da borda) | Voltar (navegacao nativa) |
| Pull down | Refresh na Home e Jogos |
| Long press em card | Quick actions (compartilhar, deletar) |
| Pinch na curva HR | Zoom in/out |
| Drag na curva HR | Scrub pela timeline do jogo |

---

## 16. Principios de UX Aplicados

### 1. Identidade de Torcedor
- Escolha do time no primeiro acesso = identidade central
- Cores do time na interface = pertencimento
- Linguagem de arquibancada em todas as telas
- Escudo do time presente em cada momento

### 2. Friccao Zero
- Social login (1 toque para entrar)
- Monitoramento automatico no horario do jogo
- Cards pre-gerados em background
- "Vou assistir de casa" = inclusao sem estadio

### 3. Torcida como Grupo
- Torcidometro transforma dados individuais em experiencia coletiva
- Ranking de reacao dentro da torcida
- Meta coletiva incentiva recrutamento
- Dados anonimizados preservam privacidade

### 4. Rivalidade como Motor
- Duelo de Torcidas gera debate e compartilhamento
- Ranking por rodada mantem competicao semanal
- Classicos desbloqueiam cards especiais
- "Sofrimento" transforma derrota em orgulho (metrica genial)

### 5. Dados com Contexto
- "148bpm no gol do Yuri aos 78'" > "148bpm as 17:23"
- Picos correlacionados com eventos reais do jogo
- Curiosidades humanizam os numeros
- "Voce reagiu 0.3s antes da media" = micro-narrativa viral

### 6. Ciclo de Engajamento
- Pre-jogo: countdown + social proof + checklist
- Jogo: monitoramento passivo + live opcional
- Pos-jogo: revelacao + experiencia + torcida + duelo
- Entre jogos: ranking + desafios + conquistas + recrutamento
- O app tem motivo para ser aberto TODOS OS DIAS, nao so em dia de jogo

### 7. Privacidade por Design
- Dados coletivos sao SEMPRE anonimizados
- Opt-in explicito para ranking e torcida coletiva
- Perfil publico mostra apenas cards, nunca HR raw
- "Excluir minha conta" acessivel e claro

---

## 17. Inventario Completo de Telas

| # | Tela | Fluxo | Prioridade |
|---|------|-------|------------|
| 0 | Splash | Primeiro acesso | P0 |
| 1 | Onboarding 1/3 | Primeiro acesso | P0 |
| 2 | Onboarding 2/3 | Primeiro acesso | P0 |
| 3 | Onboarding 3/3 | Primeiro acesso | P0 |
| 4 | Auth (Login/Cadastro) | Auth | P0 |
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

**23 telas unicas**
**MVP (P0): 17 telas**
**Fase 2 (P1): 6 telas adicionais (Live, Duelo, Rankings)**

---

## 18. Proximos Passos

1. Wireframes de alta fidelidade — Figma com escudos reais
2. Prototipo interativo — Fluxo completo do Derby (COR x PAL)
3. Design system — Cores adaptativas por time + componentes
4. Teste de usabilidade — 5 torcedores, fluxo jogo completo
5. Integracao API-Football — Eventos em tempo real para timeline
6. Algoritmo de Torcidometro — Agregacao anonima de BPM
7. Templates de card — 4 tipos com variantes por time

