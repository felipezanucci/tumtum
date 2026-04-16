# Briefing Criativo — Marca Tumtum

**Preparado por:** Felipe Zanucci
**Data:** Abril 2026
**Para:** Estúdio de Branding (criação de marca do zero)

---

## 1. Sobre o Projeto

### O que é o Tumtum?

Tumtum é uma plataforma de tecnologia para entretenimento ao vivo que captura **como as pessoas se sentem** nos seus momentos mais emocionantes — shows, jogos de futebol, festivais — monitorando a frequência cardíaca e correlacionando com a linha do tempo do evento.

O nome "Tumtum" vem da onomatopeia do batimento cardíaco. É visceral, é universal, é emocional.

### Como funciona?

1. **Conecte** — O usuário conecta seu smartwatch (Apple Watch, Galaxy Watch, Fitbit, Garmin)
2. **Viva** — Vai ao show ou jogo. O wearable captura cada batida do coração
3. **Veja** — Depois do evento, vê sua curva de emoção sincronizada com o setlist do show ou os gols da partida
4. **Compartilhe** — Gera um card visual com seu pico de emoção e compartilha nas redes sociais

### O produto viral: Share Cards

O coração do modelo de crescimento são os **cards compartilháveis**. Existem dois tipos:

- **Solo Card** — Mostra o pico de BPM do usuário vinculado a um momento do evento. Ex: "Meu coração foi a 142 BPM durante Evidências no show do Chitãozinho & Xororó"
- **Comparison Card** — Compara o batimento do fã com o do artista/atleta. Ex: "Eu estava 78% em sincronia com o Chris Martin durante A Sky Full of Stars"

Esses cards precisam ser **visualmente impressionantes** e feitos para serem compartilhados nativamente no Instagram Stories, TikTok, X (Twitter) e WhatsApp.

### Visão de futuro

Artistas e atletas também poderão compartilhar seus batimentos cardíacos, criando uma nova forma de conexão emocional entre ídolo e fã. Imagine ver que seu coração bateu no mesmo ritmo que o do seu artista favorito durante aquele refrão.

---

## 2. Fase Atual

Estamos na **Fase 0 (MVP)** — validando a hipótese de que as pessoas querem ver e compartilhar como seu coração reagiu durante eventos. Usamos wearables existentes no mercado, sem hardware próprio.

O app é uma **Progressive Web App (PWA)**, mobile-first, dark mode only.

Já temos o produto funcional com:
- Landing page
- Sistema de autenticação
- Integração com wearables (HealthKit + Google Health Connect)
- Visualização da curva cardíaca com animação
- Detecção de picos emocionais
- Geração de share cards (solo e comparação)
- Perfil público e galeria de cards

**O que falta:** Uma identidade de marca profissional. Tudo o que existe hoje é provisório.

---

## 3. Público-Alvo

### Demografia primária
- **Idade:** 18-35 anos
- **Localização:** Brasil (foco inicial: São Paulo)
- **Perfil:** Frequentadores assíduos de shows e jogos de futebol
- **Comportamento digital:** Nativos de redes sociais (Instagram, TikTok, WhatsApp, X)
- **Renda:** Classes B e C — gastam com experiências e entretenimento

### Perfis de usuário

**"A Superfã"** — Ana, 24 anos
Vai a 8+ shows por ano. Filma tudo, posta stories durante o show, cria threads sobre setlists. Para ela, o Tumtum é mais uma camada para documentar e mostrar o quanto aquele momento significou.

**"O Torcedor Apaixonado"** — Lucas, 28 anos
Sócio-torcedor, vai ao estádio toda semana. Sente cada gol no peito. Quer provar pros amigos que quase infartou no gol nos acréscimos. O card do Tumtum é essa prova.

**"A Influenciadora de Lifestyle"** — Marina, 22 anos
Usa wearable no dia a dia, posta sobre saúde e wellness. O Tumtum é conteúdo novo e diferente — dados emocionais autênticos em vez de métricas genéricas de fitness.

### O que os une
- Valorizam **experiências ao vivo**
- São **socialmente ativos** e querem compartilhar momentos
- Têm uma relação emocional forte com música e/ou esportes
- Usam dados de saúde/fitness mas querem algo **mais emocional e menos clínico**

---

## 4. Personalidade da Marca

### Tom de voz

| É | Não é |
|---|-------|
| Emocional, visceral | Clínico, médico |
| Premium, sofisticado | Elitista, inacessível |
| Nocturno, envolvente | Escuro, pesado |
| Jovem, energético | Infantil, forçado |
| Autêntico, real | Exagerado, falso |
| Confiante, direto | Arrogante, frio |

### Se o Tumtum fosse...

- **Uma pessoa:** O amigo que lembra exatamente em que música você chorou no show e te manda o vídeo no dia seguinte
- **Um ambiente:** O momento entre as luzes apagarem e o artista entrar no palco — aquela expectativa elétrica
- **Uma hora do dia:** 23h — o pico da noite
- **Um sentido:** O tato — sentir o coração acelerando
- **Uma rede social:** Um misto de Strava (dados pessoais com orgulho) com BeReal (autenticidade do momento)

### Pilares da marca

1. **Emoção Autêntica** — Dados reais do seu corpo, não opiniões ou filtros. O coração não mente.
2. **Experiência Compartilhada** — Momentos que são ainda melhores quando divididos.
3. **Privacidade como Respeito** — Dados de saúde são íntimos. Transparência e controle total.
4. **Noite como Território** — Shows, jogos noturnos, a energia do coletivo ao vivo.

---

## 5. Direção Visual (Referências Atuais)

O que temos hoje como ponto de partida (pode mudar completamente):

### Paleta atual (provisória)

| Uso | Cor | Hex |
|-----|-----|-----|
| Vermelho principal ("Tumtum Red") | Vermelho intenso | `#C0392B` |
| Vermelho secundário | Vermelho mais claro | `#E74C3C` |
| Acento (artista/comparação) | Ciano elétrico | `#00D2FF` |
| Background escuro | Quase preto | `#08080C` |
| Superfície (cards) | Cinza muito escuro | `#111118` |
| Bordas | Cinza escuro sutil | `#1A1A24` |
| Texto secundário | Cinza médio | `#6B6B80` |
| Texto principal | Branco suave | `#F0F0F5` |

### Tipografia atual (provisória)
- **Logo:** Georgia, serif — bold, uppercase, espaçamento 2-3px
- **Corpo:** System fonts (San Francisco, Segoe UI)

### Atmosfera desejada
- **Dark mode only** — o app deve parecer que você está dentro de um show à noite
- **Premium mas acessível** — pense no app do Spotify, não no app de um banco
- **Dados com emoção** — gráficos que parecem batimentos, não planilhas
- **Glow e luz** — elementos luminosos sobre fundo escuro (como luzes de palco)

---

## 6. Universo Visual de Referência

### Marcas que admiramos (não copiar, apenas referência de posicionamento)

| Marca | O que pegar de referência |
|-------|---------------------------|
| **Spotify Wrapped** | A mecânica de "seus dados como storytelling compartilhável" |
| **Strava** | Orgulho dos seus dados pessoais, comunidade, share cards de atividade |
| **Apple Watch (Fitness)** | A visualização elegante de dados de saúde |
| **BeReal** | Autenticidade do momento real, anti-filtro |
| **Coachella / Lollapalooza** | A estética de festival, tipografia ousada, cores vibrantes no escuro |
| **Nike Run Club** | Celebração da conquista pessoal, dados como troféu |

### Referências visuais mais amplas
- Luzes de palco cortando a escuridão
- A tela de um celular gravando um show (aquele brilho no meio da multidão)
- Visualizadores de áudio (ondas sonoras, frequências)
- Monitores cardíacos (a linha contínua do ECG)
- Neon signs em fundo escuro
- A energia visual de capas de álbuns ao vivo

---

## 7. Tagline Atual

**"Sinta o evento. Compartilhe a emoção."**

Essa tagline é provisória e pode ser reformulada pelo estúdio. O conceito que ela encapsula:

- O Tumtum transforma uma **sensação física** (batimento cardíaco) em **conteúdo emocional** compartilhável
- É sobre **sentir** (input do corpo) e **compartilhar** (output social)
- O evento é o catalisador, a emoção é o produto

Alternativas que já exploramos (nenhuma é definitiva):
- "Seu coração conta a história"
- "Cada batida é uma memória"
- "O ritmo da sua emoção"

---

## 8. Entregáveis Esperados

### 8.1 Identidade da Marca

- [ ] **Logo principal** — versões horizontal, vertical, ícone/símbolo
- [ ] **Variações do logo** — positivo, negativo, monocromático, para fundos claros e escuros
- [ ] **Símbolo/ícone** — que funcione como ícone de app (512x512, 192x192, favicon)
- [ ] **Paleta de cores definitiva** — primárias, secundárias, complementares, com códigos HEX/RGB/HSL
- [ ] **Tipografia** — fontes para logo, títulos, corpo, e numerais (BPM é destaque frequente)
- [ ] **Tom de voz** — guia de comunicação com exemplos práticos

### 8.2 Sistema Visual

- [ ] **Padrões e texturas** — elementos gráficos recorrentes derivados do conceito (batimento, onda, pulso)
- [ ] **Iconografia** — estilo para ícones de UI e ícones de funcionalidade
- [ ] **Tratamento fotográfico/ilustrativo** — como tratar imagens dentro da marca
- [ ] **Gradientes e efeitos** — linguagem visual para glows, overlays, transições

### 8.3 Aplicações

- [ ] **Share Cards** — Templates visuais para os dois tipos de card (solo e comparação), formato 9:16 (1080x1920px)
- [ ] **App UI Kit** — Direção visual para os principais componentes (botões, cards, inputs, navegação)
- [ ] **Social media templates** — Posts e stories para Instagram, TikTok, X
- [ ] **App icon** — Para iOS, Android e PWA
- [ ] **OG Image** — Preview quando o link é compartilhado no WhatsApp/redes

### 8.4 Brand Book

- [ ] **Documento compilado** — PDF com todas as definições, regras de uso, espaçamentos, o que fazer e não fazer
- [ ] **Arquivos fonte** — Em formatos editáveis (AI, Figma, SVG)

---

## 9. Contexto Técnico Relevante

O estúdio não precisa se preocupar com código, mas é útil saber:

- **O app é dark mode only** — toda a identidade precisa funcionar primariamente em fundos escuros
- **Mobile-first** — 90%+ dos acessos serão mobile. Tudo precisa funcionar em tela pequena
- **Share cards são 9:16** — proporção de Instagram Stories / TikTok
- **BPM é um número protagonista** — a tipografia precisa fazer números grandes ficarem bonitos
- **A curva cardíaca é o asset visual central** — a linha do batimento cardíaco sincronizada com o tempo é a visualização principal do app
- **As cores precisam ter bom contraste em OLED** — maioria dos usuários tem tela AMOLED
- **Vamos precisar de uma cor "artista/cyan"** — para diferenciar os dados do fã vs. dados do artista nos cards de comparação

---

## 10. O Que NÃO Queremos

- Estética **médica/hospitalar** — não é um app de saúde, é um app de entretenimento
- Visual **genérico de startup tech** — sem gradientes azul-roxo genéricos
- **Excesso de elementos** — a marca precisa respirar, ser limpa
- **Infantilidade** — usamos coração como conceito, mas não é Valentine's Day
- **Logo literal de coração** — queremos algo mais abstrato e menos óbvio
- **Branco/claro como base** — dark mode é inegociável para o MVP
- **Tipografia fina/light** — precisamos de presença e impacto

---

## 11. Referências Culturais (Mercado Brasil)

O público inicial é brasileiro, especificamente paulistano. Contextos culturais relevantes:

- **Shows:** Lollapalooza BR, The Town, Rock in Rio, shows sertanejo no Allianz Parque, pagode no bar
- **Futebol:** Paulistão, Brasileirão, Libertadores — torcidas organizadas, rivalidade Corinthians x Palmeiras x São Paulo x Santos
- **Redes:** Instagram Stories e Reels, TikTok, WhatsApp (status + grupos), X para eventos ao vivo
- **Cultura de compartilhamento:** Brasileiro adora mostrar onde foi, o que sentiu, fazer stories. O Tumtum canaliza isso com dados reais
- **Diversidade musical:** Do sertanejo ao trap, do pagode ao rock. A marca não pode ser nichada em um gênero só

---

## 12. Cronograma Ideal

| Fase | Duração | Entregável |
|------|---------|------------|
| Imersão e pesquisa | 1 semana | Moodboard, referências visuais, alinhamento |
| Conceitos | 2 semanas | 2-3 direções criativas com logo, paleta, tipografia |
| Refinamento | 1 semana | Direção escolhida refinada com aplicações iniciais |
| Sistema visual | 2 semanas | Share cards, UI direction, templates sociais |
| Brand book | 1 semana | Documento final + arquivos fonte |
| **Total** | **~7 semanas** | |

---

## 13. Contato e Alinhamento

**Felipe Zanucci** — Fundador, Tumtum
- Disponível para calls semanais de alinhamento
- Feedback rápido via WhatsApp/Slack
- Acesso ao app funcional para referência visual do produto atual

---

## Resumo em Uma Frase

> Tumtum precisa de uma marca que traduza a emoção de estar ao vivo em um evento — aquele frio na barriga, o coração acelerado, a energia coletiva — em uma identidade visual premium, noturna e irresistivelmente compartilhável.

---

*Este documento é confidencial e destinado exclusivamente ao estúdio contratado para o projeto de branding.*
