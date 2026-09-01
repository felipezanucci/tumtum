# TumTum Design System

Sistema de design da **TumTum** (tumtum.cc) — o app que transforma o que você sentiu num show ou jogo em uma história com prova: batimento cardíaco + o exato momento em que aconteceu.

**Categoria:** memória emocional + cultura + dado. NUNCA healthtech, fitness, wellness, sono, recovery ou software médico — esse é o único modo de falha que destrói a marca.

**Fontes deste sistema:** `uploads/design-brief.md` (Manual da Marca v0.4 + contexto de produto), a rodada de design de ago/2026 nesta workspace (cards, app Android, site, manual v0.5 em `TumTum-Manual-da-Marca.dc.html`), e os assets oficiais do wordmark enviados pelo fundador. Não há Figma nem codebase conectados; o site atual é tumtum.cc.

**Status das decisões:** a paleta, o logo, a voz e as regras de contraste são LEI (manual v0.4). Neutros, spacing, radius, superfícies, nav e componentes são PROPOSTA da rodada v0.5 — consistentes entre si, mas aguardando martelo do fundador.

---

## CONTENT FUNDAMENTALS

**Voz:** se a frase cabe num grupo de WhatsApp às 2h da manhã, ela cabe na TumTum.

- **Português do Brasil informal.** "A gente", "tá", "tava", "tb" onde for natural. Sem caricatura de gíria.
- **A pessoa é o sujeito.** "Seu coração foi a 187." Nunca "Detectamos um pico de 187."
- **Sentimento, não saúde.** batida · momento · noite · galera · arrepio · vibe. Proibido: zona, recuperação, performance, frequência cardíaca (como marketing), diagnóstico, normal/anormal.
- **Curta como um grito.** Headlines em CAIXA ALTA de 2 linhas: "EU TAVA TRANQUILO. / AÍ VEIO ISSO." Ninguém lê parágrafo no meio da multidão.
- **Humor cúmplice, autoirônico** — nunca às custas do usuário: "Aqui acabou meu psicológico." "Não respondo pelo que aconteceu depois."
- **Vazios e erros humanos:** "Nenhum show ainda. Seu coração tá de folga." · "Não achamos batida nesse horário. O relógio tava no pulso?" Nunca culpar o usuário.
- **A tela quieta:** em permissão/privacidade/consentimento a personalidade DESLIGA. Clara, calma, zero piada, botão preto (não rosa), o que a TumTum NÃO faz dito na frente.
- **Estatística honesta:** toda métrica coletiva diz exatamente o que mede ("71% bateram o próprio pico entre 23h45 e 23h49"). Nunca ranking de BPM entre corpos.
- **Sem emoji.** A marca não usa. Números com algarismos tabulares, sempre.
- **Copy aprovada verbatim** (banco completo na pág. 11 do manual v0.5): a linha-herói do MVP é "EU TAVA TRANQUILO. / AÍ VEIO ISSO."; comunidade é "NINGUÉM TAVA TRANQUILO. / APARENTEMENTE FOI COLETIVO."; artista é "NA MESMA VIBE. / LITERALMENTE."

## VISUAL FOUNDATIONS

**Território: Mutante Pop.** A forma fica, a pele muda — estrutura reconhecível que sobrevive a milhares de aparências (referência MTV conceitual, nunca nostálgica). Mutação é tratamento de superfície, nunca redesign.

- **Cores:** 4 institucionais travadas — Preto #000, Branco #FFF, TumTum Pink #FF6F91 (acento líder, pode ocupar superfícies grandes), Toxic Yellow #EFFF00 (rajada: chip, CTA, marcador). Equilíbrio de acento ~70% pink / 30% yellow. Neutros de apoio: escala de cinzas puros #0F0F0F→#E6E6E6 (proposta), sem matiz.
- **Regras duras de contraste (medidas):** nunca branco sobre rosa (2,65:1) ou amarelo (1,11:1); sobre rosa/amarelo tudo é preto; rosa nunca é par texto/fundo com amarelo (2,39:1); **o número nunca é rosa fora do preto**. No preto: linha de dado rosa + marcador amarelo. No branco: linha preta + marcador rosa (amarelo some, 1,11:1).
- **Sistema de superfícies (proposta):** cada noite/tela escolhe UMA superfície inteira — preto (a noite: captura, revela), branco (o dia: listas, feed), rosa (a assinatura), amarelo (o grito, raro). Composição idêntica, pele diferente.
- **Tipo:** 2 famílias. Chosmos = SÓ o wordmark (asset travado, nunca live text — EULA Typozon v3.4 proíbe imitar). Instrument Sans 400/500/600/700 = todo o resto. Sem monoespaçada (seria uma terceira família); metadado = caixa alta + tracking +0.16em. Impacto = escala e peso, nunca fonte nova. Números-herói em Bold com tracking negativo forte (-0.04 a -0.055em) e line-height 0.78.
- **Fundos:** cor chapada sempre. Sem gradientes decorativos (único gradiente permitido: scrim preto sobre vídeo/foto em pôsteres da galeria). Vídeo/foto do evento entra como matéria-prima nos vãos — tipo NUNCA solto sobre vídeo, sempre em placa opaca (preta/branca/rosa/amarela).
- **Bordas e sombras:** separação por hairline (#1E1E1E no preto, #E6E6E6 no branco), não por sombra. Sombra só em contexto físico (pôster da galeria, card do hero do site). Sem blur, sem transparência estrutural (só rgba(0,0,0,.6) em chips sobre imagem).
- **Radius:** 0 em superfícies e campos de cor (pôster/sticker/flyer); 12px no que se toca (botões, cards tocáveis); pílula 999px em chips de nav — nunca em containers de texto longos.
- **Espaçamento:** base 4px; gutter mobile 24px; seções de site 100–120px verticais; gutter lateral desktop 64px.
- **Motion:** quase nenhum. PROIBIDO qualquer motion de pulso/batimento/ECG. Hover = troca de cor sólida (120–200ms ease-out), sem transform. Motion pode premiar a atenção depois, nunca atrasar a compreensão (teste dos 2 segundos).
- **Dataviz:** o gráfico responde "quando aconteceu?", nunca leitura clínica. Buraco de captura = linha QUEBRA (nunca interpolar minuto não medido) com traço pontilhado #4A4A4A na base. Sem zonas, cores de risco, faixas, scores.
- **Cards de compartilhamento:** 9:16 primeiro (1080×1920, safe areas ~250px topo / ~340px base); feed 1080×1080 e preview 1200×630 são layouts próprios. O número é o herói; a TumTum assina, nunca domina.
- **Hover/press (proposta):** hover troca cor de fundo/texto por par aprovado (ex.: CTA rosa → amarelo); press sem shrink; foco = borda amarela.

## ICONOGRAFIA

**Não existe sistema de ícones.** Decisão da rodada v0.5: **rótulos de texto em caixa alta** fazem o papel de ícones (nav: FEED · AO VIVO · VOCÊ) até existir um sistema desenhado. Não inventar ícones, não usar emoji, não puxar biblioteca de terceiros sem decisão do fundador. Elementos gráficos permitidos: ponto/círculo de status (9px), traço de 20×3px como indicador de aba ativa, setas de texto (←), tocos de linha como legenda de série. O wordmark existe em 2 assets: `assets/tumtum-wordmark-black.svg` e `assets/tumtum-wordmark-white.svg` (únicos aprovados; o vetor atual é trace poligonal — pedir export em curvas antes de OOH).

---

## ÍNDICE

- `styles.css` — entry point; importa `tokens/` (colors, typography, spacing, radius, motion, elevation, fonts)
- `readme.md` — este guia
- `SKILL.md` — skill para agentes
- `assets/` — wordmarks oficiais (preto/branco), clipes de referência (event-clip, torcida, cold-play)
- `components/core/` — Button, Chip, MetaLabel, WordmarkPlate
- `components/data/` — HeroNumber, BpmCurve, StatPair
- `components/share/` — ShareCard (as 4 peles), MomentRow
- `guidelines/` — specimen cards de Colors, Type, Spacing, Brand (tab Design System)
- `ui_kits/app/` — fluxo Android: Ao vivo → A revela → Escolha → Card (index.html interativo)
- Referências de design (fora do sistema, mesma workspace): `TumTum-Manual-da-Marca.dc.html` (manual v0.5, 18 pág.), `TumTum-App.dc.html` (13 telas), `TumTum-Site.dc.html` (site PT/EN), `TumTum-Card01.dc.html` (explorações do card 01), `uploads/design-brief.md` (brief original v0.4)

## Intentional additions

- **WordmarkPlate** — wrapper que aplica a regra "logo só preto/branco sobre superfície aprovada" sem recriar o logo (o SVG é o asset).
- **BpmCurve** — o único gráfico da marca; embute a regra do buraco visível e o mapa de cores por superfície.
- Nenhum outro primitive foi adicionado além do que as telas de referência usam.
