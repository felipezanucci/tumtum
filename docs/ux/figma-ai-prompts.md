# Tumtum — Prompts para Figma AI

> Copie e cole cada prompt no Figma AI para gerar as telas.
> O padrao visual ja esta no Figma. Estes prompts focam em estrutura e UX.
> Frame: iPhone 15 Pro (393x852)

---

## BLOCO 1: PRIMEIRO ACESSO

### Tela 0 — Splash

```
Mobile app splash screen. Center the Tumtum logo vertically and horizontally. Below the logo, a subtle heart pulse animation line. No other elements. Clean, minimal, full bleed dark background.
```

### Tela 1 — Onboarding 1/3

```
Mobile onboarding screen 1 of 3. Top 60% of the screen: illustration area showing an animated heart rate curve spiking during a football goal moment, with a stadium silhouette in the background. Below: headline "Sinta o jogo" in large bold text. Subtext: "Seu coração conta a história do jogo". Three dot indicators at bottom (first active). Two buttons: "Próximo" primary and "Pular" text link below.
```

### Tela 2 — Onboarding 2/3

```
Mobile onboarding screen 2 of 3. Top 60%: illustration showing multiple heart rate curves from different fans converging and peaking at the same moment, representing collective fan experience. Below: headline "Sua torcida unida". Subtext: "Veja como a torcida inteira sentiu cada momento do jogo". Three dot indicators (second active). "Próximo" button and "Pular" link.
```

### Tela 3 — Onboarding 3/3

```
Mobile onboarding screen 3 of 3. Top 60%: illustration showing two progress bars side by side competing — one red, one green — representing rival fan bases measuring who felt the game more intensely. Below: headline "Quem vibra mais?". Subtext: "Compare sua torcida com a rival e prove quem vive mais o jogo". Three dot indicators (third active). Single CTA button: "Começar".
```

### Tela 4 — Login / Cadastro

```
Mobile login screen. Tumtum logo at top. Two large social login buttons stacked: "Entrar com Google" with Google icon, "Entrar com Apple" with Apple icon. Divider line with "ou" text. Below: email input field, password input field, "Entrar" button. At bottom: "Não tem conta? Cadastrar" text link. Footer: small legal text about terms and privacy policy.
```

---

## BLOCO 2: SETUP DO TORCEDOR

### Tela 5 — Escolha do Time

```
Mobile screen for choosing a football team. Top: headline "Qual é o seu time?" with a search input field below. Section "Populares em SP" showing a 2x2 grid of team cards — each card has the team badge/crest centered with team name below (Corinthians, Palmeiras, São Paulo, Santos). Below: section "Outros times" as a scrollable list with smaller team entries (Flamengo, Vasco, Cruzeiro, Atlético). Full-width tappable cards.
```

### Tela 5b — Confirma Time

```
Mobile confirmation screen after selecting a team. Center: large team badge/crest with a celebratory entrance animation feel. Below the badge: team name "Corinthians" in large bold text. Iconic team phrase "Fiel até o fim!" in italic below. Descriptive text: "A interface do app se adapta com as cores do seu time". Two buttons: primary "Esse é meu time!" and secondary text link "Trocar".
```

### Tela 6 — Wearable Priming

```
Mobile pre-permission screen. Top: illustration of a fan in a stadium stands wearing a smartwatch, with heart pulse waves radiating from the watch. Headline: "Conecte seu relógio". Body text: "Vamos medir como seu coração reage a cada lance do jogo". Privacy badge with lock icon: "Seus dados ficam seguros e anônimos na torcida coletiva." Two buttons: primary "Conectar dispositivo", secondary "Fazer depois".
```

### Tela 7 — Seleção de Provider

```
Mobile device selection list. Top: "Qual seu dispositivo?". Stacked list of tappable rows, each with device icon, device name, and chevron arrow: Apple Watch, Galaxy Watch, Fitbit, Garmin, Mi Band / Outro. Each row is a full-width card with left icon and right chevron.
```

### Tela 8 — Sucesso Conexão

```
Mobile success confirmation screen. Center: animated green checkmark in a circle. Below: "Conectado!" headline. Device name: "Apple Watch de Felipe". Real-time BPM display: "72" with a small heart icon pulsing. Personalized message: "Pronto pra sentir o próximo jogo do Corinthians!". CTA button: "Bora!".
```

---

## BLOCO 3: APP PRINCIPAL

### Tela 9 — Home (com próximo jogo)

```
Mobile home screen with 5-tab bottom navigation (Home, Jogos, Torcida, Ranking, Perfil — Home active). 

Content from top to bottom:

1. Header bar: small Tumtum logo left, user avatar right.

2. "Próximo jogo" card: large prominent card showing "CORINTHIANS vs PALMEIRAS" with both team badges facing each other, venue "Neo Química Arena", date "Dom, 20 Abr · 16h", countdown "Faltam 5 dias", social proof "247 torcedores já confirmaram" with heart icon. Two stacked buttons: primary "Eu vou estar lá!" and secondary "Vou assistir de casa".

3. "Último jogo" card: "COR 2 x 1 SÃO" with a mini heart rate curve preview showing 2 peaks at the goals, "Pico: 142bpm" and "Gol do Yuri", link "Ver experiência".

4. "Torcidômetro geral" card: "Torcida mais intensa da rodada" showing top 3 teams in a mini ranking with horizontal bars (1. Flamengo, 2. Corinthians, 3. Palmeiras). Link "Ver ranking".
```

### Tela 9b — Home (empty state, primeiro acesso)

```
Mobile home screen first-time state. Same header and tab bar. Top: "Próximo jogo" card of user's team (same as above with CTA). Below: empty state section with a light illustration of fans cheering, headline "Sua primeira vez?", body text "Confirme presença no próximo jogo e veja como seu coração vive cada lance", CTA button "Encontrar jogo".
```

### Tela 10 — Jogos (lista)

```
Mobile screen listing football matches. Tab bar with Jogos active. Top section: horizontal filter chips for competitions: "Todos", "Brasileirão", "Copa do Brasil", "Libertadores".

Section "Meu time" with match cards stacked:
- Card 1: "Dom 20/04 · COR vs PAL · Brasileirão Rod.5 · Neo Química 16h" with "Confirmar presença" button.
- Card 2: "Qua 23/04 · COR vs BOC · Libertadores Grupo · Neo Química 21h30" with "Confirmar presença" button.

Section "Outros jogos hoje" with compact match rows:
- "FLA vs FLU · 18h30 · Maracanã · 423 torcedores"
- "SÃO vs SAN · 21h · Morumbi · 189 torcedores"
```

### Tela 11 — Detalhe do Jogo

```
Mobile match detail screen. Top hero section: both team badges large with "CORINTHIANS vs PALMEIRAS" between them, competition "Brasileirão Série A · Rodada 5" below.

Info section: venue with pin icon "Neo Química Arena", date with calendar icon "Dom, 20 Abr · 16h", TV with "Premiere".

"Torcida confirmada" card: side by side comparison — "247 torcedores Corinthians" vs "198 torcedores Palmeiras" with heart icons. Motivational text: "Fiel já está em vantagem!".

"Histórico de confronto" section: last derby result "COR 1x0 PAL", fan peak averages for each side.

Bottom: sticky footer with two buttons — primary "Eu vou estar lá!" and secondary "Vou assistir de casa".
```

---

## BLOCO 4: DIA DO JOGO

### Tela 12 — Pré-Jogo

```
Mobile pre-match checklist screen. Top card: team badges, "COR vs PAL · HOJE · 16h · Neo Química Arena".

Checklist section with three items, each with status icon:
- Green check: "Wearable conectado · Apple Watch · 74 bpm"
- Green check: "Bateria OK · 82% — suficiente"
- Green check: "Jogo confirmado · Monitoramento às 16h"

"Clima do jogo" card: heart icons showing "312 vs 267" fan counts for each team. Text: "A Fiel está 17% maior. Bora manter!"

Bottom section: "Monitoramento começa automaticamente às 16:00" with secondary button "Iniciar agora". Tip text: "Coloque o celular no bolso e viva o jogo!"
```

### Tela 13 — Modo Live

```
Mobile live match screen with dark/dimmed feel. Top bar: team badges with live score "COR 1x0 PAL · AO VIVO 67'" with pulsing red dot.

Center: very large BPM number "134" with heart icon, pulsing animation, in team accent color.

Heart rate curve section: real-time HR line chart showing last 5 minutes, scrolling left, with a marker at a spike labeled "GOL!".

"Pulso da Torcida" card: side-by-side stats — "Fiel agora: 128bpm médio · 312 torcedores" vs "Alviverde agora: 119bpm médio · 267 torcedores". Text: "Fiel 7% mais intensa!"

Match timeline section: scrollable list of events — "67' GOL Yuri", "45' Intervalo", "23' Falta dura", "12' Escanteio" with appropriate icons.

Subtle banner at bottom: "Guarde o celular e viva o jogo!"
```

---

## BLOCO 5: PÓS-JOGO

### Tela 14 — Revelação

```
Mobile full-screen cinematic reveal screen. Dark immersive background with team color gradient.

Centered content with dramatic vertical spacing:
- Both team badges small with score: "CORINTHIANS 2 x 1 PALMEIRAS"
- Venue and date: "Neo Química Arena · 20 de Abril de 2026"
- Dramatic stat: "Você viveu 97 minutos de pura emoção"
- Large highlighted number: "148" with "bpm" below in accent color
- Context line: "Yuri marcou o gol da virada aos 78'"

Single CTA at bottom: "Ver jogo completo"
```

### Tela 15 — Experiência

```
Mobile post-match experience screen, scrollable. Top: match header "COR 2x1 PAL · Neo Química · 20 Abr".

Stats row: three metric cards side by side — "148 pico", "97 média", "97 min".

Heart rate curve section: full-width interactive line chart with the complete HR curve over the match duration. Markers at each goal moment showing minute and player name ("GOL 12' Romero", "GOL 78' Yuri"). X-axis shows match minutes (0-90+). Hint text: "Arraste para explorar".

"Meus picos" section: ranked list of peak moments:
1. Gold medal icon: "148bpm · 78' GOL Yuri Alberto · Seu maior pico!" with "Criar card" button
2. Silver medal icon: "142bpm · 12' GOL Romero" with "Criar card" button
3. Bronze medal icon: "131bpm · 90+3' Apito final" with "Criar card" button

"Curiosidades" section: card with fun facts like "Você ficou acima de 120bpm por 23 minutos", "No intervalo seu coração descansou pra 72bpm", "Você reagiu ao gol do Yuri 0.3s antes da média da torcida!"

Bottom: two buttons — secondary "Ver pulso da torcida", primary "Gerar card do jogo".
```

### Tela 16 — Torcidômetro

```
Mobile collective fan pulse screen. Top header: "COR 2x1 PAL · Torcida Fiel · 312 torcedores monitorados".

Main chart: full-width line chart with two overlaid lines — thick team-colored line showing average BPM of the entire fan base over match time, and a thinner white/gray line showing the user's personal BPM. Markers at goals. Legend: "Torcida Fiel" and "Você".

"Momento mais intenso" card:
- "78' GOL DO YURI"
- "143bpm médio da torcida"
- "Você: 148bpm (acima da média!)"
- "98% da torcida reagiu em menos de 2 segundos"
- Distribution bar chart: "<100bpm: 2%", "100-120: 15%", "120-140: 51%", "140-160: 29%", ">160bpm: 3%"

Ranking section: "Você reagiu mais rápido que 73% da torcida · Seu ranking: #84 de 312"

Bottom: two buttons — "Comparar com rival" and "Gerar card coletivo".
```

### Tela 17 — Duelo de Torcidas

```
Mobile rival fan comparison screen. Top: "COR 2x1 PAL · Neo Química · 20 Abr". Headline: "QUEM VIVEU MAIS O JOGO?"

Side-by-side comparison with 4 metric rows, each showing team badge, horizontal bar, and value for both sides:

1. "INTENSIDADE" — bar chart COR 138bpm vs PAL 131bpm (COR longer bar)
2. "EXPLOSÃO NO GOL" — bar chart COR +47bpm vs PAL +12bpm (COR much longer)
3. "REAÇÃO" — bar chart COR 1.2s vs PAL 2.8s (COR longer = faster)
4. "SOFRIMENTO" — bar chart PAL longer than COR (loser suffered more)

"Veredito Tumtum" card with seal/badge styling: "A Fiel viveu mais intensamente em 3 de 4 categorias. Vitória dentro e fora de campo!"

Bottom: two buttons — primary "Gerar card do duelo" and secondary "Compartilhar resultado".
```

---

## BLOCO 6: CARDS

### Tela 18 — Editor de Card

```
Mobile card editor screen. Top: horizontal tabs for card type — "Meu Jogo" (active), "Torcida", "Duelo", "Momento".

Center: large card preview taking up ~50% of screen, showing a share card template with match result, HR curve, peak BPM, goal info, and username. The preview updates live.

Below preview: "Estilo" section with 4 color circle swatches (team colors, black, white, neon). "Formato" section with 3 chips: "Stories 9:16", "Feed 1:1", "Post 4:5".

Bottom sticky: primary button "Compartilhar" and text link "Salvar na galeria".
```

---

## BLOCO 7: RANKING E PERFIL

### Tela 19 — Ranking de Torcidas

```
Mobile ranking screen. Tab bar with Ranking active. Top: horizontal filter chips "Rodada", "Campeonato", "Histórico".

Heading: "Torcida mais intensa da rodada 5".

Ranked list of teams, each row with: position number, team badge, team name, horizontal intensity bar, average BPM, and fan count. Row 2 (user's team) highlighted with "Seu time!" badge.

1. Flamengo — 141bpm — 1.247 torcedores
2. Corinthians — 138bpm — 312 torcedores (highlighted)
3. Palmeiras — 135bpm
4. São Paulo — 131bpm
...down to 20.

"Conquistas do seu time" section: horizontal scroll of trophy/badge cards — "Torcida mais explosiva Rod.3", "Sofredor-mor no Derby Rod.5", "100% fiel — 3 jogos consecutivos".
```

### Tela 20 — Meu Ranking

```
Mobile personal ranking screen within the fan base. Top profile card: user avatar, name, team badge, "Fiel desde Abr 2026". Ranking: "#84 de 312 torcedores".

Stats grid: "Jogos: 3", "Pico máx: 156bpm", "Média geral: 112bpm", "Reação média: 1.1s".

"Títulos pessoais" section: list of earned badges with icons and descriptions — "Coração Fiel" (100% jogos), "Reação de Goleiro" (top 10% reação), "Máximo BPM" (156bpm no Derby).

"Próxima conquista" card: locked badge "Infarto Fiel — Atinja 160bpm em um jogo". Progress bar showing 156/160.
```

### Tela 21 — Perfil do Torcedor

```
Mobile profile screen. Tab bar with Perfil active. Top: user avatar, name "@felipe", team badge with "Corinthians", "Fiel desde Abr 2026", stats "3 jogos · 8 cards · #84 ranking".

Horizontal scroll of trophy badges.

"Meus cards" section: 2-column grid of card thumbnails.

"Meus jogos" section: stacked list of past matches with result, peak BPM, and peak count for each. Gear icon link to Configurações at bottom.
```

### Tela 22 — Configurações

```
Mobile settings screen. Grouped list sections:

"Conta": Name, Email, Senha — each with chevron.
"Meu time": Corinthians with team badge and chevron to change.
"Dispositivo": Apple Watch with green check, "Adicionar dispositivo" link.
"Privacidade": toggles for "Perfil público", "Incluir meu BPM na torcida coletiva", "Mostrar no ranking", link to "Dados de saúde".
"Notificações": toggles for "Pré-jogo", "Pós-jogo", "Ranking da rodada", "Desafios semanais".
"Sobre": Termos de uso, Política de privacidade, Versão 1.0.0.

Bottom: "Sair da conta" red text button. "Excluir minha conta" destructive text link.
```

---

## DICAS DE USO NO FIGMA AI

1. **Uma tela por vez** — Cole um prompt por vez para manter qualidade
2. **Ajuste depois** — O Figma AI gera a estrutura, você refina com seu design system
3. **Ordem sugerida**: Comece pelo Bloco 5 (pós-jogo) que é o core da experiência, depois Bloco 3 (home), depois os outros
4. **Componentes reutilizáveis**: Após gerar, extraia como componentes: match card, team badge pair, HR curve chart, stat card, tab bar
5. **Protótipo**: Conecte as telas na ordem: Splash → Onboarding → Auth → Time → Wearable → Home → Jogo → Live → Revelação → Experiência → Torcidômetro → Card → Share
