# Tumtum App — UX Structure & User Flows

> Documento de arquitetura UX — contornos e fluxos de todas as telas
> Versao: 1.0 | Data: 2026-04-06

---

## Indice

1. [Mapa Geral de Telas](#1-mapa-geral-de-telas)
2. [Fluxo 1 — Primeiro Acesso e Onboarding](#2-fluxo-1--primeiro-acesso-e-onboarding)
3. [Fluxo 2 — Conexao do Wearable](#3-fluxo-2--conexao-do-wearable)
4. [Fluxo 3 — Home e Descoberta de Eventos](#4-fluxo-3--home-e-descoberta-de-eventos)
5. [Fluxo 4 — Pre-Evento](#5-fluxo-4--pre-evento)
6. [Fluxo 5 — Durante o Evento (Modo Live)](#6-fluxo-5--durante-o-evento-modo-live)
7. [Fluxo 6 — Pos-Evento e Experiencia](#7-fluxo-6--pos-evento-e-experiencia)
8. [Fluxo 7 — Geracao e Compartilhamento do Card](#8-fluxo-7--geracao-e-compartilhamento-do-card)
9. [Fluxo 8 — Perfil e Colecao](#9-fluxo-8--perfil-e-colecao)
10. [Navegacao Global](#10-navegacao-global)
11. [Principios de UX Aplicados](#11-principios-de-ux-aplicados)

---

## 1. Mapa Geral de Telas

```
SPLASH → ONBOARDING (3 telas) → AUTH → WEARABLE SETUP → HOME
                                                           |
                                    ┌──────────────────────┼──────────────────────┐
                                    v                      v                      v
                               EVENTOS              MEU PERFIL              COLECAO
                                    |                      |                      |
                                    v                      v                      v
                            DETALHE EVENTO          CONFIGURACOES          DETALHE CARD
                                    |
                                    v
                            MODO LIVE (durante)
                                    |
                                    v
                            EXPERIENCIA (pos)
                                    |
                                    v
                            GERAR CARD
                                    |
                                    v
                            PREVIEW CARD
                                    |
                                    v
                            SHARE SHEET
```

---

## 2. Fluxo 1 — Primeiro Acesso e Onboarding

### Principio: Mostrar o valor antes de pedir qualquer coisa

```
┌─────────────────────────────┐
│        TELA 0: SPLASH       │
│                             │
│    ┌───────────────────┐    │
│    │                   │    │
│    │   Logo Tumtum     │    │
│    │   (animacao do    │    │
│    │    coracao pulse)  │    │
│    │                   │    │
│    └───────────────────┘    │
│                             │
│    Duracao: 1.5s            │
│    Transicao: fade out      │
└─────────────────────────────┘
          │
          v
┌─────────────────────────────┐
│    TELA 1: ONBOARDING 1/3   │
│    "Sinta cada momento"     │
│                             │
│    ┌───────────────────┐    │
│    │                   │    │
│    │  [Animacao: curva  │    │
│    │   HR subindo com   │    │
│    │   pico no drop     │    │
│    │   de uma musica]   │    │
│    │                   │    │
│    └───────────────────┘    │
│                             │
│    "Seu coracao conta a     │
│     historia do show"       │
│                             │
│    ● ○ ○                    │
│                             │
│    [Proximo →]              │
│    [Pular]                  │
└─────────────────────────────┘
          │
          v
┌─────────────────────────────┐
│    TELA 2: ONBOARDING 2/3   │
│    "Descubra seus picos"    │
│                             │
│    ┌───────────────────┐    │
│    │                   │    │
│    │  [Animacao: curva  │    │
│    │   com marcadores   │    │
│    │   nos picos +      │    │
│    │   nome da musica   │    │
│    │   aparecendo]      │    │
│    │                   │    │
│    └───────────────────┘    │
│                             │
│    "Veja qual musica fez    │
│     seu coracao acelerar"   │
│                             │
│    ○ ● ○                    │
│                             │
│    [Proximo →]              │
│    [Pular]                  │
└─────────────────────────────┘
          │
          v
┌─────────────────────────────┐
│    TELA 3: ONBOARDING 3/3   │
│    "Compartilhe a emocao"   │
│                             │
│    ┌───────────────────┐    │
│    │                   │    │
│    │  [Mockup: card de  │    │
│    │   compartilhamento │    │
│    │   com HR curve +   │    │
│    │   foto do show]    │    │
│    │                   │    │
│    └───────────────────┘    │
│                             │
│    "Crie cards e mostre     │
│     como voce viveu"        │
│                             │
│    ○ ○ ●                    │
│                             │
│    [Comecar →]              │
└─────────────────────────────┘
```

### Regras UX:
- Swipe horizontal entre as 3 telas (gesto natural)
- "Pular" sempre visivel (nao forcar onboarding)
- Animacoes leves, 60fps, nao bloquear interacao
- Onboarding so aparece no PRIMEIRO acesso (flag local)

---

## 3. Fluxo Auth — Cadastro / Login

### Principio: Maximo 2 toques para entrar

```
┌─────────────────────────────┐
│       TELA 4: AUTH           │
│                             │
│    Logo Tumtum              │
│                             │
│    ┌───────────────────┐    │
│    │ [G] Entrar com     │    │
│    │     Google          │    │
│    └───────────────────┘    │
│                             │
│    ┌───────────────────┐    │
│    │ [] Entrar com     │    │
│    │      Apple          │    │
│    └───────────────────┘    │
│                             │
│    ─── ou ───               │
│                             │
│    ┌───────────────────┐    │
│    │ Email              │    │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ Senha              │    │
│    └───────────────────┘    │
│                             │
│    [Entrar]                 │
│                             │
│    Nao tem conta? Cadastrar │
│                             │
│    ───────────────────      │
│    Ao continuar, voce       │
│    aceita os Termos e       │
│    Politica de Privacidade  │
└─────────────────────────────┘
```

### Fluxo de Cadastro por Email (se nao usar social):

```
┌─────────────────────────────┐
│    TELA 4b: CADASTRO         │
│                             │
│    "Crie sua conta"         │
│                             │
│    ┌───────────────────┐    │
│    │ Nome               │    │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ Email              │    │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ Senha              │    │
│    └───────────────────┘    │
│                             │
│    [Criar conta]            │
│                             │
│    Ja tem conta? Entrar     │
└─────────────────────────────┘
```

### Regras UX:
- Social login (Google/Apple) como CTAs primarios — maiores e no topo
- Email/senha como fallback — visualmente secundario
- Zero campos desnecessarios no cadastro (sem telefone, sem username obrigatorio)
- Validacao inline em tempo real (email valido, senha 8+ chars)
- Botao desabilitado ate todos os campos validos
- Apos auth: transicao direta para setup do wearable

---

## 4. Fluxo 2 — Conexao do Wearable

### Principio: Pre-permission priming — explicar ANTES de pedir

```
┌─────────────────────────────┐
│  TELA 5: WEARABLE PRIMING   │
│                             │
│    ┌───────────────────┐    │
│    │                   │    │
│    │  [Ilustracao:      │    │
│    │   relogio no       │    │
│    │   pulso com ondas  │    │
│    │   de coracao]      │    │
│    │                   │    │
│    └───────────────────┘    │
│                             │
│    "Conecte seu relogio"    │
│                             │
│    "Vamos ler sua           │
│     frequencia cardiaca     │
│     durante os shows para   │
│     mostrar seus momentos   │
│     mais emocionantes"      │
│                             │
│    🔒 "Seus dados ficam     │
│     seguros. Voce decide    │
│     o que compartilhar."    │
│                             │
│    [Conectar dispositivo]   │
│    [Fazer depois]           │
└─────────────────────────────┘
          │
          v (se tocou "Conectar")
┌─────────────────────────────┐
│  TELA 6: SELECAO PROVIDER   │
│                             │
│    "Qual seu dispositivo?"  │
│                             │
│    ┌───────────────────┐    │
│    │ ⌚ Apple Watch     │ >  │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ 📱 Galaxy Watch   │ >  │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ ⌚ Fitbit          │ >  │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ ⌚ Garmin          │ >  │
│    └───────────────────┘    │
│    ┌───────────────────┐    │
│    │ 📱 Outro (Health  │ >  │
│    │    Connect)        │    │
│    └───────────────────┘    │
│                             │
│    "Nao tenho relogio"      │
│    (link → explica que      │
│     pode usar com celular   │
│     no bolso em alguns      │
│     dispositivos)           │
└─────────────────────────────┘
          │
          v
┌─────────────────────────────┐
│  TELA 7: PERMISSAO OS       │
│                             │
│  [Dialog nativo do OS:      │
│   "Tumtum quer acessar      │
│    seus dados de saude"     │
│                             │
│   Frequencia cardiaca ✓     │
│                             │
│   [Nao permitir] [OK] ]    │
│                             │
└─────────────────────────────┘
          │
          v
┌─────────────────────────────┐
│  TELA 8: SUCESSO CONEXAO    │
│                             │
│    ┌───────────────────┐    │
│    │                   │    │
│    │   ✓ (check        │    │
│    │    animado verde)  │    │
│    │                   │    │
│    └───────────────────┘    │
│                             │
│    "Conectado!"             │
│    "Apple Watch de Felipe"  │
│                             │
│    BPM atual: 72 ♥         │
│    (mostra BPM real como    │
│     prova de que funciona)  │
│                             │
│    [Ir para o app →]        │
└─────────────────────────────┘
```

### Regras UX:
- NUNCA pedir permissao do OS sem a tela de priming antes
- Mostrar BPM real apos conexao (feedback imediato = confianca)
- "Fazer depois" sempre disponivel — nao bloquear acesso ao app
- Se usuario pular: mostrar banner persistente na Home ate conectar
- Detectar automaticamente o OS (iOS → Apple Health, Android → Health Connect)

---

## 5. Fluxo 3 — Home e Descoberta de Eventos

### Principio: O app deve parecer vivo mesmo sem dados

```
┌─────────────────────────────┐
│  TELA 9: HOME                │
│                             │
│  ┌─ Header ──────────────┐  │
│  │ ♥ Tumtum    [avatar]  │  │
│  └───────────────────────┘  │
│                             │
│  ── Se tem evento proximo ──│
│                             │
│  ┌───────────────────────┐  │
│  │ PROXIMO EVENTO         │  │
│  │ ┌───────────────────┐ │  │
│  │ │ [Capa do evento]   │ │  │
│  │ │                   │ │  │
│  │ │ Coldplay           │ │  │
│  │ │ Allianz Parque     │ │  │
│  │ │ 12 Abr • 21h       │ │  │
│  │ └───────────────────┘ │  │
│  │                       │  │
│  │ "Faltam 6 dias" ⏱     │  │
│  │ [Ver detalhes]        │  │
│  └───────────────────────┘  │
│                             │
│  ── Eventos populares ──    │
│                             │
│  ┌─────┐ ┌─────┐ ┌─────┐  │
│  │Show1│ │Show2│ │Show3│  │
│  │     │ │     │ │     │  │
│  └─────┘ └─────┘ └─────┘  │
│  (scroll horizontal)       │
│                             │
│  ── Suas experiencias ──    │
│                             │
│  ┌───────────────────────┐  │
│  │  [Empty state:         │  │
│  │   ilustracao leve]     │  │
│  │                        │  │
│  │  "Sua primeira         │  │
│  │   experiencia comeca   │  │
│  │   no proximo show"     │  │
│  │                        │  │
│  │  [Encontrar evento]    │  │
│  └───────────────────────┘  │
│                             │
│  ┌─ Tab Bar ─────────────┐  │
│  │ 🏠    🔍    ♥    👤   │  │
│  │ Home  Busca Card Perfil│  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

### Home — Estado com experiencias anteriores:

```
┌─────────────────────────────┐
│  HOME (com historico)        │
│                             │
│  [Proximo evento — mesmo]   │
│                             │
│  ── Ultima experiencia ──   │
│                             │
│  ┌───────────────────────┐  │
│  │ Coldplay • 12 Abr      │  │
│  │ ┌───────────────────┐ │  │
│  │ │ [Mini curva HR     │ │  │
│  │ │  com pico marcado] │ │  │
│  │ └───────────────────┘ │  │
│  │ ♥ Pico: 142bpm        │  │
│  │ 🎵 "Fix You"          │  │
│  │ [Ver experiencia →]   │  │
│  └───────────────────────┘  │
│                             │
│  ── Eventos populares ──    │
│  [cards horizontais]        │
│                             │
└─────────────────────────────┘
```

---

## 6. Fluxo 4 — Busca e Selecao de Evento

### Principio: Achar o evento em menos de 10 segundos

```
┌─────────────────────────────┐
│  TELA 10: BUSCA EVENTOS      │
│                             │
│  ┌───────────────────────┐  │
│  │ 🔍 Buscar evento...   │  │
│  └───────────────────────┘  │
│                             │
│  ── Filtros rapidos ──      │
│  [Shows] [Futebol] [Festival]│
│                             │
│  ── Proximos na sua cidade ─│
│                             │
│  ┌───────────────────────┐  │
│  │ [img] Coldplay         │  │
│  │       Allianz • 12 Abr │  │
│  ├───────────────────────┤  │
│  │ [img] Corinthians x    │  │
│  │       Palmeiras        │  │
│  │       Neo Quimica • 15 │  │
│  ├───────────────────────┤  │
│  │ [img] Lollapalooza     │  │
│  │       Interlagos • 22  │  │
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
          │
          v (toque no evento)
┌─────────────────────────────┐
│  TELA 11: DETALHE EVENTO     │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │   [Imagem capa grande │  │
│  │    com gradiente      │  │
│  │    escuro embaixo]    │  │
│  │                       │  │
│  │   Coldplay            │  │
│  │   Music of the        │  │
│  │   Spheres Tour        │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  📍 Allianz Parque, SP      │
│  📅 12 Abr 2026 • 21:00     │
│  🎵 Concert                 │
│                             │
│  ── Setlist (se disponivel)─│
│  1. Higher Power             │
│  2. Adventure of a Lifetime  │
│  3. The Scientist            │
│  ... ver mais               │
│                             │
│  ── X pessoas monitorando ──│
│  👤👤👤 + 47 pessoas         │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │   [Eu vou estar la!]  │  │
│  │   (botao primario)    │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
```

### Regras UX:
- Busca com autocomplete (debounce 300ms)
- Geolocalizacao para sugerir eventos proximos (pedir permissao com priming)
- Mostrar social proof ("47 pessoas monitorando")
- Setlist pre-carregado via Setlist.fm quando disponivel
- "Eu vou estar la" = confirma presenca + ativa monitoramento automatico

---

## 7. Fluxo 5 — Pre-Evento

### Principio: Criar antecipacao e garantir que tudo funciona

```
┌─────────────────────────────┐
│  TELA 12: PRE-EVENTO         │
│  (aparece no dia do evento)  │
│                             │
│  ┌───────────────────────┐  │
│  │ Coldplay • HOJE       │  │
│  │ Allianz Parque • 21h  │  │
│  └───────────────────────┘  │
│                             │
│  ── Checklist automatico ── │
│                             │
│  ✅ Wearable conectado      │
│     "Apple Watch • 74 bpm"  │
│                             │
│  ✅ Bateria do relogio OK   │
│     "82% — suficiente"      │
│                             │
│  ✅ Evento confirmado       │
│     "Setlist carregado"     │
│                             │
│  ── ou, se problema ──      │
│  ⚠️ Wearable desconectado  │
│     [Reconectar agora]      │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │  "Monitoramento       │  │
│  │   comeca              │  │
│  │   automaticamente     │  │
│  │   as 21:00"           │  │
│  │                       │  │
│  │  [Iniciar agora]      │  │
│  │  (para quem quer      │  │
│  │   comecar antes)      │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  💡 Dica: Coloque o app    │
│     em segundo plano.       │
│     Nos cuidamos do resto.  │
│                             │
└─────────────────────────────┘
```

### Notificacao push (3h antes):

```
┌─────────────────────────────┐
│ 🔔 PUSH NOTIFICATION        │
│                             │
│ ♥ Tumtum                    │
│ "Coldplay em 3 horas!       │
│  Tudo pronto para capturar  │
│  seus batimentos. ♥"        │
│                             │
│ [Abrir] [Ignorar]          │
└─────────────────────────────┘
```

### Regras UX:
- Notificacao push NAO intrusiva — apenas 1x, 3h antes
- Checklist automatico: detecta problemas ANTES do show
- Monitoramento inicia automaticamente baseado no horario do evento
- Nao exigir que o usuario abra o app durante o show
- "Iniciar agora" para quem quer incluir a fila/pre-show

---

## 8. Fluxo 6 — Durante o Evento (Modo Live)

### Principio: O app trabalha em background — zero interacao necessaria

```
┌─────────────────────────────┐
│  ESTADO: BACKGROUND          │
│                             │
│  [App coletando HR silencio-│
│   samente em background]    │
│                             │
│  - Coleta a cada 1-5s       │
│  - Salva localmente         │
│  - Sync quando wifi         │
│  - Zero notificacoes        │
│    durante o evento         │
│                             │
└─────────────────────────────┘

SE o usuario abrir o app durante o evento:

┌─────────────────────────────┐
│  TELA 13: MODO LIVE          │
│                             │
│  ┌───────────────────────┐  │
│  │    Coldplay • AO VIVO │  │
│  │    Allianz Parque      │  │
│  └───────────────────────┘  │
│                             │
│         ♥ 127              │
│        bpm                  │
│    (numero grande,          │
│     pulsando no ritmo)      │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │  [Curva HR em tempo   │  │
│  │   real, ultimos 5min, │  │
│  │   scrollando para     │  │
│  │   a esquerda]         │  │
│  │                       │  │
│  │   ~~~~/\~~~~~/\~~~~   │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  ⏱ 01:23:45 de evento      │
│                             │
│  🎵 Tocando agora:          │
│     "The Scientist"         │
│     (se setlist disponivel) │
│                             │
│  ── Stats rapidos ──        │
│  Pico: 142bpm               │
│  Media: 98bpm                │
│  Tempo monitorando: 1h23    │
│                             │
│  ┌───────────────────────┐  │
│  │  💡 Guarde o celular  │  │
│  │  e aproveite o show!  │  │
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
```

### Regras UX:
- Modo Live e OPCIONAL — usuario NAO precisa abrir o app
- Se abrir, mostrar BPM grande e pulsante (feedback visceral)
- Mensagem "guarde o celular" — incentivar experiencia real
- Tela com brilho reduzido automaticamente (modo cinema)
- Nao mostrar notificacoes de pico DURANTE o evento (nao interromper)

---

## 9. Fluxo 7 — Pos-Evento e Experiencia

### Principio: O "momento magico" — revelar a experiencia como presente

### Notificacao pos-evento (30min depois do fim):

```
┌─────────────────────────────┐
│ 🔔 PUSH NOTIFICATION        │
│                             │
│ ♥ Tumtum                    │
│ "Sua experiencia no         │
│  Coldplay esta pronta!      │
│  Veja como seu coracao      │
│  viveu o show. ♥"           │
│                             │
│ [Ver agora →]               │
└─────────────────────────────┘
```

### Tela de revelacao (transicao cinematica):

```
┌─────────────────────────────┐
│  TELA 14: REVELACAO          │
│  (tela cheia, imersiva)      │
│                             │
│  [Fundo escuro]             │
│                             │
│  "Coldplay"                 │
│  "Allianz Parque"           │
│  "12 de Abril de 2026"      │
│                             │
│  (pausa dramatica 2s)       │
│                             │
│  "Voce viveu 2h14 de pura  │
│   emocao"                   │
│                             │
│  (pausa 1.5s)               │
│                             │
│  "Seu coracao bateu         │
│   mais forte em..."         │
│                             │
│  (animacao: numero sobe)    │
│         142                 │
│         bpm                 │
│                             │
│  "durante Fix You"          │
│                             │
│  [Ver experiencia completa →]│
│                             │
└─────────────────────────────┘
```

### Tela de experiencia completa:

```
┌─────────────────────────────┐
│  TELA 15: EXPERIENCIA        │
│                             │
│  ┌───────────────────────┐  │
│  │ Coldplay              │  │
│  │ Allianz Parque • 12Abr│  │
│  └───────────────────────┘  │
│                             │
│  ── Stats resumo ──         │
│  ┌──────┐ ┌──────┐ ┌──────┐│
│  │ 142  │ │  98  │ │ 2h14 ││
│  │ pico │ │media │ │ tempo││
│  └──────┘ └──────┘ └──────┘│
│                             │
│  ── Curva HR completa ──    │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │     /\      /\        │  │
│  │    /  \    / |\       │  │
│  │   /    \  /  | \      │  │
│  │  /      \/   |  \     │  │
│  │ /        Fix You \    │  │
│  │/                  \   │  │
│  │                       │  │
│  │ [Markers nos picos    │  │
│  │  com nome da musica]  │  │
│  │                       │  │
│  │ ← arrastar para       │  │
│  │   explorar timeline → │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  ── Seus momentos ──        │
│  (top 5 picos, ordenados)   │
│                             │
│  🥇 142bpm • Fix You        │
│     "Seu maior pico!"       │
│     [Criar card →]          │
│                             │
│  🥈 138bpm • Viva la Vida   │
│     [Criar card →]          │
│                             │
│  🥉 135bpm • Yellow          │
│     [Criar card →]          │
│                             │
│  4. 131bpm • Clocks          │
│     [Criar card →]          │
│                             │
│  5. 128bpm • Paradise        │
│     [Criar card →]          │
│                             │
│  ── Curiosidades ──         │
│  "Voce ficou acima de       │
│   120bpm por 34 minutos"    │
│                             │
│  "Seu coracao descansou     │
│   no intervalo: 72bpm"     │
│                             │
│  ┌───────────────────────┐  │
│  │ [Gerar card do show]  │  │
│  │ (botao primario)      │  │
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
```

### Interacao na curva HR:
```
┌───────────────────────────────┐
│  CURVA HR — INTERACAO          │
│                               │
│  Toque longo em qualquer      │
│  ponto da curva:              │
│                               │
│  ┌─────────────────────────┐  │
│  │         ● ← tooltip     │  │
│  │     /\ │127bpm          │  │
│  │    /  \│21:47            │  │
│  │   /    │"The Scientist"  │  │
│  └─────────────────────────┘  │
│                               │
│  Arrastar = scrub pela        │
│  timeline (como Spotify)      │
│                               │
│  Pinch = zoom in/out          │
│  na curva                     │
│                               │
└───────────────────────────────┘
```

### Regras UX:
- Revelacao e CINEMATICA — tratar como abertura de presente
- Animacoes sequenciais com timing (nao tudo de uma vez)
- Curva HR desenhada com animacao (como escrita a mao)
- Interacao de scrub na curva = CORE UX do produto
- Cada pico e acionavel → leva direto para gerar card
- "Curiosidades" humanizam os dados (nao so numeros)

---

## 10. Fluxo 8 — Geracao e Compartilhamento do Card

### Principio: 2 toques do trigger ate o share sheet

```
┌─────────────────────────────┐
│  TELA 16: EDITOR DE CARD     │
│                             │
│  ── Template ──             │
│  [Solo ●] [Comparacao ○]    │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │  PREVIEW DO CARD      │  │
│  │  (tempo real)         │  │
│  │                       │  │
│  │  ┌─────────────────┐  │  │
│  │  │   TUMTUM         │  │  │
│  │  │                  │  │  │
│  │  │  Coldplay        │  │  │
│  │  │  Fix You         │  │  │
│  │  │                  │  │  │
│  │  │  [curva HR mini] │  │  │
│  │  │  ♥ 142bpm        │  │  │
│  │  │                  │  │  │
│  │  │  @felipe         │  │  │
│  │  │  12.04.2026      │  │  │
│  │  └─────────────────┘  │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  ── Estilo ──               │
│  [🔴] [🔵] [⚫] [🟣]       │
│  (paleta de cores)          │
│                             │
│  ── Formato ──              │
│  [Stories 9:16] [Feed 1:1]  │
│  [Post 4:5]                 │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │  [Compartilhar →]     │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  [Salvar na galeria]        │
│                             │
└─────────────────────────────┘
```

### Card de Comparacao (feature futura - artista):

```
┌─────────────────────────────┐
│  CARD COMPARACAO             │
│                             │
│  ┌─────────────────────┐    │
│  │   TUMTUM             │    │
│  │                      │    │
│  │  Coldplay • Fix You  │    │
│  │                      │    │
│  │  [duas curvas HR:    │    │
│  │   vermelha = voce    │    │
│  │   cyan = artista]    │    │
│  │                      │    │
│  │  ♥ Voce: 142bpm      │    │
│  │  ♥ Chris: 156bpm     │    │
│  │                      │    │
│  │  78% em sincronia    │    │
│  │                      │    │
│  │  @felipe             │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

### Share Sheet:

```
┌─────────────────────────────┐
│  TELA 17: SHARE SHEET        │
│  (bottom sheet nativo)       │
│                             │
│  ┌───────────────────────┐  │
│  │ [mini preview card]   │  │
│  └───────────────────────┘  │
│                             │
│  ── Compartilhar em ──      │
│                             │
│  ┌──────┐ ┌──────┐ ┌──────┐│
│  │ Insta│ │TikTok│ │  X   ││
│  │Stories│ │     │ │      ││
│  └──────┘ └──────┘ └──────┘│
│  ┌──────┐ ┌──────┐ ┌──────┐│
│  │ Whats│ │Copiar│ │ Mais ││
│  │ App  │ │ Link │ │      ││
│  └──────┘ └──────┘ └──────┘│
│                             │
│  ── ou ──                   │
│  [Salvar imagem]            │
│  [Salvar video]             │
│                             │
└─────────────────────────────┘
```

### Pos-compartilhamento:

```
┌─────────────────────────────┐
│  TELA 18: POS-SHARE          │
│  (celebracao)                │
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │   🎉 (confetti        │  │
│  │    animacao sutil)    │  │
│  │                       │  │
│  └───────────────────────┘  │
│                             │
│  "Compartilhado!"           │
│                             │
│  "Seu card foi salvo        │
│   na sua colecao"           │
│                             │
│  [Ver minha colecao]        │
│  [Voltar para experiencia]  │
│                             │
└─────────────────────────────┘
```

### Regras UX:
- Card pre-gerado em background (Celery) — zero espera ao abrir editor
- Preview atualiza em TEMPO REAL ao trocar estilo/formato
- Instagram Stories = formato padrao (maior viralidade)
- Deep link para Instagram Stories API (card ja pre-carregado como sticker)
- Salvar na galeria sempre disponivel (fallback se share falhar)
- Pos-share: celebracao BREVE (1.5s) e redireciona — nao prender o usuario
- Card salvo automaticamente na colecao do perfil

---

## 11. Fluxo 9 — Perfil e Colecao

```
┌─────────────────────────────┐
│  TELA 19: MEU PERFIL         │
│                             │
│  ┌───────────────────────┐  │
│  │  [Avatar]              │  │
│  │  Felipe Zanucci        │  │
│  │  @felipe               │  │
│  │                        │  │
│  │  3 shows • 12 cards    │  │
│  └───────────────────────┘  │
│                             │
│  ── Stats gerais ──         │
│  ┌──────┐ ┌──────┐ ┌──────┐│
│  │ 156  │ │ 3    │ │ 12   ││
│  │ pico │ │shows │ │cards ││
│  │ max  │ │      │ │      ││
│  └──────┘ └──────┘ └──────┘│
│                             │
│  ── Minha colecao ──        │
│  (grid de cards, 2 colunas) │
│                             │
│  ┌──────┐ ┌──────┐         │
│  │card 1│ │card 2│         │
│  │      │ │      │         │
│  └──────┘ └──────┘         │
│  ┌──────┐ ┌──────┐         │
│  │card 3│ │card 4│         │
│  │      │ │      │         │
│  └──────┘ └──────┘         │
│                             │
│  ── Minhas experiencias ──  │
│  ┌───────────────────────┐  │
│  │ Coldplay • 12 Abr      │  │
│  │ ♥ 142bpm • 5 picos     │  │
│  ├───────────────────────┤  │
│  │ Corinthians • 08 Abr   │  │
│  │ ♥ 138bpm • 3 picos     │  │
│  └───────────────────────┘  │
│                             │
│  [⚙️ Configuracoes]         │
│                             │
└─────────────────────────────┘
```

### Perfil publico (compartilhavel):

```
┌─────────────────────────────┐
│  TELA 20: PERFIL PUBLICO     │
│  (tumtum.app/@felipe)        │
│                             │
│  [Mesmo layout do perfil,   │
│   mas apenas cards publicos │
│   e stats agregados]        │
│                             │
│  Sem dados sensiveis de HR  │
│  Apenas cards compartilhados│
│                             │
└─────────────────────────────┘
```

### Configuracoes:

```
┌─────────────────────────────┐
│  TELA 21: CONFIGURACOES      │
│                             │
│  ── Conta ──                │
│  Nome                    >  │
│  Email                   >  │
│  Senha                   >  │
│                             │
│  ── Dispositivo ──          │
│  Apple Watch          ✅ >  │
│  [Adicionar dispositivo]    │
│                             │
│  ── Privacidade ──          │
│  Perfil publico        [◉]  │
│  Mostrar BPM real      [◉]  │
│  Dados de saude        [>]  │
│                             │
│  ── Notificacoes ──         │
│  Pre-evento            [◉]  │
│  Pos-evento            [◉]  │
│  Novos eventos         [○]  │
│                             │
│  ── Sobre ──                │
│  Termos de uso           >  │
│  Politica de privacidade >  │
│  Versao 1.0.0               │
│                             │
│  [Sair da conta]            │
│  [Excluir minha conta]      │
│                             │
└─────────────────────────────┘
```

---

## 12. Navegacao Global

### Tab Bar (sempre visivel nas telas principais):

```
┌─────────────────────────────────────┐
│                                     │
│   🏠        🔍        ♥        👤   │
│   Home     Busca    Cards    Perfil │
│                                     │
└─────────────────────────────────────┘
```

### Hierarquia de navegacao:

```
Tab 1: HOME
├── Proximo evento → Detalhe evento
├── Ultima experiencia → Experiencia completa → Editor card
└── Eventos populares → Detalhe evento

Tab 2: BUSCA
├── Lista de eventos
├── Filtros (tipo, cidade, data)
└── Detalhe evento → Confirmar presenca

Tab 3: CARDS (Colecao)
├── Grid de todos os cards gerados
├── Detalhe card → Re-compartilhar
└── Filtro por evento

Tab 4: PERFIL
├── Stats pessoais
├── Lista de experiencias
├── Configuracoes
└── Perfil publico (preview)
```

### Gestos globais:

| Gesto | Acao |
|-------|------|
| Swipe right (da borda) | Voltar (navegacao nativa) |
| Pull down | Refresh na Home e Busca |
| Long press em card | Quick actions (compartilhar, deletar) |
| Pinch na curva HR | Zoom in/out |
| Drag na curva HR | Scrub pela timeline |

---

## 13. Principios de UX Aplicados

### 1. Friccao Zero
- Social login como padrao (1 toque para entrar)
- Monitoramento automatico (zero interacao durante o evento)
- Card pre-gerado (zero espera ao compartilhar)
- Deteccao automatica de OS para wearable correto

### 2. Value First, Ask Later
- Onboarding mostra o produto antes de pedir cadastro
- Pre-permission priming antes de cada permissao do OS
- "Fazer depois" sempre disponivel — nunca bloquear

### 3. Progressive Disclosure
- Home simples → detalhes sob demanda
- Stats resumidos → curva completa ao tocar
- Picos ranqueados → card detalhado ao selecionar

### 4. Feedback Visceral
- BPM pulsante no modo live
- Curva HR animada na revelacao
- Confetti no pos-compartilhamento
- Haptic feedback nos picos (vibra suave no celular)

### 5. Empty States Uteis
- Toda tela vazia tem: ilustracao + texto + CTA
- Empty state da Home = demo do produto
- Empty state dos Cards = link para experiencias

### 6. Privacidade por Design
- Tela de priming antes de TODA permissao
- Dados de saude = opt-in explicito
- Perfil publico mostra apenas cards, nunca HR raw
- "Excluir minha conta" acessivel e claro

### 7. Performance Percebida
- Skeleton screens em todas as listas
- Cards pre-gerados em background
- Curva HR renderizada com WebGL/Canvas (60fps)
- Imagens com blur-up progressive loading

---

## 14. Mapa de Estados e Transicoes

```
PRIMEIRO ACESSO:
Splash → Onboarding 1 → 2 → 3 → Auth → Wearable Setup → Home (empty)

FLUXO RECORRENTE:
Home → Busca → Detalhe Evento → Confirmar Presenca
→ [dia do evento] Pre-Evento → [durante] Background/Live
→ [depois] Notificacao → Revelacao → Experiencia → Card → Share → Colecao

FLUXO RAPIDO (usuario recorrente):
Home → Experiencia (da notificacao) → Card → Share
(3 telas do trigger ao share)

FLUXO DE REVISITA:
Home → Colecao → Card → Re-share
Perfil → Experiencia antiga → Novo card
```

---

## 15. Inventario Completo de Telas

| # | Tela | Tipo | Prioridade MVP |
|---|------|------|----------------|
| 0 | Splash | Transitoria | P0 |
| 1 | Onboarding 1/3 | Onboarding | P0 |
| 2 | Onboarding 2/3 | Onboarding | P0 |
| 3 | Onboarding 3/3 | Onboarding | P0 |
| 4 | Auth (Login/Cadastro) | Auth | P0 |
| 4b | Cadastro email | Auth | P0 |
| 5 | Wearable Priming | Setup | P0 |
| 6 | Selecao Provider | Setup | P0 |
| 7 | Permissao OS | Setup (nativo) | P0 |
| 8 | Sucesso Conexao | Setup | P0 |
| 9 | Home | Core | P0 |
| 10 | Busca Eventos | Core | P0 |
| 11 | Detalhe Evento | Core | P0 |
| 12 | Pre-Evento | Core | P1 |
| 13 | Modo Live | Core | P1 |
| 14 | Revelacao | Core | P0 |
| 15 | Experiencia | Core | P0 |
| 16 | Editor de Card | Core | P0 |
| 17 | Share Sheet | Core | P0 |
| 18 | Pos-Share | Core | P0 |
| 19 | Meu Perfil | Core | P0 |
| 20 | Perfil Publico | Social | P2 |
| 21 | Configuracoes | Settings | P1 |

**Total: 22 telas unicas**
**MVP (P0): 17 telas**

---

## 16. Proximos Passos

1. **Wireframes de alta fidelidade** — Figma com componentes reutilizaveis
2. **Prototipo interativo** — Fluxo completo clicavel para validacao
3. **Design system** — Tokens de cor, tipografia, espacamento, componentes
4. **Teste de usabilidade** — 5 usuarios, fluxo primeiro acesso ate share
5. **Especificacao de animacoes** — Timing, easing, triggers para cada transicao

