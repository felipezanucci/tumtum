# Handoff Tumtum app para Claude

_Started 2026-09-01 13:25 UTC_

---

## User

Continuing from "Projeto TumTum começa".

## Assistant

We've been working on **Projeto TumTum**, a full-stack design system for a music festival haptic-capture app that reveals moment-by-moment emotion during live shows as visual patterns.

**What we built:**

- **Card 01** (9:16 hero) in four surface treatments (black, white, yellow, rose) with embedded video reenframed to center the band; wordmark applied across all variations.
- **Full TumTum app** (Android): eight-screen user journey from onboarding through Health Connect permission → live capture → emotion reveal → moment card + social feed (friends/event feeds, shared moments with "SENTI TB" reactions, feelings gallery with 14 event posters, public profile).
- **Brand Manual v0.5** — 18 pages consolidating the original brief (black/white/#FF6F91/#EFFF00, Instrument Sans + Mutante Pop) plus all new rules: no pink numbers except on black; number always black elsewhere; text on video lives in opaque plates; three-zone nav (FEED·AO VIVO·VOCÊ); single reaction (SENTI TB).
- **Design System** (TumTum Design Systems project): tokens, 8 components (Button/Chip/MetaLabel/WordmarkPlate + HeroNumber/BpmCurve/StatPair + ShareCard/MomentRow), 15 specimen cards, UI kit with the four-screen flow, SKILL.md. Wordmark still has the autotrace problem (606 rects instead of curves) — awaiting hand-drawn SVG export from designer.
- **Bilingual website** (PT/EN toggle in nav, saved per session): hero + four cards (Só o Momento/A Galera/Minha Noite/Escolha) with embedded GIFs (Coldplay clip + crowd footage), event feed mockup, feelings gallery as four rotated posters, promo stats, waitlist, footer. Desktop version locked. **Mobile version complete** (390px): compact sticky nav with MENU drawer, stacked hero, horizontal ticker, swipeable card carousel, full-width forms.

**Files involved:**
- `TumTum-Card01.dc.html` (four surfaces + video reenframing)
- `TumTum-App.dc.html` (eight screens + wireframes)
- `TumTum-Manual-da-Marca.dc.html` (18-page brand bible)
- `TumTum-Site.dc.html` (desktop, PT/EN)
- `TumTum-Site-Mobile.dc.html` (mobile 390px, PT/EN)
- `design_handoff_site_tumtum/README.md` (v0.5, ready for Claude Code)
- **TumTum Design Systems** project at https://claude.ai/design/p/c75ae53d-6c08-4957-90ba-31851f171ef1 (tokens + components compiled)

**Next:** Site goes live via Claude Code (same handoff flow as before). After launch, we iterate on mobile QA and decide whether to skin the app screens in the Mutante Pop rotations (14 event templates) or keep core UI stable. Wordmark PDF ready whenever the designer has the real vector.

## User

prepare um documento de hand off pro claude code sobre o app tumtum. ele irá começar a fazer o app com base no que construimos

## Assistant

_[tool: list_files]_

_[tool: read_file]_

## Assistant

_[tool: update_todos]_

_[tool: write_file]_

## User

antes de fazer o handoff eu quero que vc considere uma animação quando o usuário abre o app tumtum. considere a animação no link abaixo https://drive.google.com/file/d/1zfATngSvFFAF0pQ9bCiCdZWX0PH8XAfX/view?usp=drive_link

## Assistant



## Assistant

_[tool: googledrive__read_file_content]_

_[tool: web_fetch]_

_[tool: tool_search_tool_bm25]_

Não consigo abrir esse link — o arquivo do Drive está privado para mim (e vídeo não é legível por lá). Preciso que você anexe o arquivo aqui.

_[tool: ask_user]_

Aguardando o arquivo da animação — sem ver o movimento não faço a especificação dela no handoff.

Aviso: o limite de uso desta conversa chegou perto do teto. O handoff do app (README + assets) fica como próximo passo assim que você subir o vídeo.

## User

Answered — ref_file: 961b3ea8-3ca3-5934-8cb6-c819a6524ad5.mp4; role: Splash de abertura (todo cold start)

## Assistant

_[tool: run_script]_

_[tool: view_image]_

_[tool: view_image]_

_[tool: view_image]_

_[tool: view_image]_

_[tool: view_image]_

_[tool: run_script]_

_[tool: run_script]_

_[tool: run_script]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: ready_for_verification]_

A referência é: fundo rosa cheio, wordmark preto centrado, entrada com overshoot e depois pulsação de coração (dois toques + descanso, ~125 BPM) — nada mais. Especifiquei isso como splash de cold start na **Rodada 3**, no topo do doc do app: 3a a batida em loop, 3b o corte seco para o FEED, mais a tabela de tempos (incluindo o fundo pintado no window background pra não haver flash branco, e o comportamento com "reduzir animação").

Falta decidir só uma coisa antes do handoff: se a saída espera sempre 2 ciclos (≈1,4s) mesmo quando a sessão já resolveu. Diga "ok" e eu escrevo o handoff do app com essa animação incluída.

## User

Found issues — fixing…

## Assistant

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: ready_for_verification]_

Corrigido: o wordmark agora repousa visível em escala 1 (só a batida é animada). A entrada com overshoot ficou apenas na especificação de tempo — no preview estático ela apareceria como frame 0, invisível.

## User

a animação de abertura deve ser um pouco mais lenta... deve ser tum tum, uma breve pausa e tum tum de novo (cerca de dois segundos a animação toda) e já abre o app normal

## Assistant

_[tool: str_replace_edit]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: ready_for_verification]_

Agora é tum-tum → pausa → tum-tum: frase de 950ms, duas frases, ~1,9s no total (2,0s com a entrada) e corte seco pro FEED. No preview ela repete em loop para você conseguir ver; na spec está explícito que são só duas frases.

## User

certo! pode fazer o handoff

## Assistant

_[tool: grep]_

_[tool: run_script]_

_[tool: copy_files]_

_[tool: update_todos]_

_[tool: show_to_user]_

Handoff em `design_handoff_app_tumtum/README.md` — 10 seções: produto e as 3 regras não negociáveis, stack (Kotlin/Compose + Health Connect), tokens de cor/tipo/forma, **a animação de abertura com timeline em ms e esqueleto Compose**, navegação de 3 zonas, mapa das 15 telas com id navegável, garantias de dados/privacidade, tom de voz e ordem de construção sugerida.

Assets copiados junto: wordmarks, 4 frames da referência do splash e uma cópia do doc de telas.

Duas coisas pra você decidir: a pendência do wordmark autotrace (Claude Code vai precisar do vetor bom pros densities) e se o backend do social entra já ou fica com repositório fake, como escrevi.

