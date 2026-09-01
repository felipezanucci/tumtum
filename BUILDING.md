# TumTum — App Android (primeira implementação)

Implementação do handoff `project/design_handoff_app_tumtum/README.md` (v1.0, 1 set 2026).
Kotlin + Jetpack Compose, Health Connect como única fonte de FC, Room, sem backend
(social em repositório fake trocável).

## Compilar

Este projeto foi escrito num ambiente sem acesso ao Android SDK (rede bloqueia
`dl.google.com`), então **o primeiro build acontece na sua máquina**:

1. Android Studio (Ladybug+) ou CLI com JDK 17 e Android SDK 35 instalados.
2. `./gradlew :app:assembleDebug` — o wrapper baixa o Gradle 8.14.3 sozinho.
3. Testes de unidade: `./gradlew :app:testDebugUnitTest`
   (a lógica de análise — gaps/cobertura/momentos — já roda verificada: 6/6 testes).

Aparelho: Android 9+ (minSdk 28) com o app Health Connect instalado
(no Android 14+ já vem no sistema).

## Mapa tela → código

| id | Tela | Arquivo |
|---|---|---|
| 3a/3b | Splash batendo → corte pro FEED | `ui/splash/SplashOverlay.kt` + `MainActivity.kt` |
| b1 | Onboarding | `ui/screens/onboarding/OnboardingScreen.kt` |
| b2 | Criar conta | `ui/screens/account/CreateAccountScreen.kt` |
| b3 | Permissão — a tela quieta | `ui/screens/permission/PermissionScreen.kt` |
| b4 | Trazer do meu relógio | `ui/screens/sources/WatchSourcesScreen.kt` |
| b5 | Feed — seus amigos | `ui/screens/feed/FeedScreen.kt` |
| b6 | Feed do evento | `ui/screens/eventfeed/EventFeedScreen.kt` |
| b7 | Galeria de sentimentos | `ui/screens/gallery/GalleryScreen.kt` |
| b8 | Perfil público | `ui/screens/profile/PublicProfileScreen.kt` |
| a1 | Suas noites | `ui/screens/you/YouScreen.kt` |
| a2 | Captura ao vivo | `ui/screens/live/CaptureScreen.kt` (+ `LiveViewModel`) |
| a3 | A noite — a revela | `ui/screens/reveal/RevealScreen.kt` |
| a4 | A galera | `ui/screens/crowd/CrowdScreen.kt` |
| a5 | Vazio — primeiro uso | `ui/screens/live/LiveTabScreen.kt` |
| — | Escolha de pele + Seu card (UI kit do core loop) | `ui/screens/choose/`, `ui/screens/card/` |
| — | Configurações (§7: revogar/apagar) | `ui/screens/settings/SettingsScreen.kt` |

Design system em Compose: `ui/theme/` (tokens §3) e `ui/components/`
(botão 56dp/r12, chips r0, wordmark como asset, `BpmCurve` com regra de gap,
barra FEED · AO VIVO · VOCÊ, `ShareCardView` 9:16).

## Garantias implementadas (§7)

- Leitura de FC **somente** na janela do evento ±30min (`NightRepository`), em lote.
- Densidade por fonte medida e mostrada (b4) — a decisão nunca é escondida.
- Gap > 60s quebra a linha (`NightAnalyzer` + `BpmCurve`); zero interpolação.
- Captura e revela 100% offline; nada de rede sem ação do usuário (não há rede).
- Permissão revogada → app abre, noites ficam, captura bloqueia com explicação.
- Apagar conta → Room + DataStore zerados, avisado uma vez.

## Decisões tomadas no caminho (reversíveis — me avise para mudar)

1. **Onboarding, páginas 2 e 3**: o doc só desenha a página 1; escrevi as outras
   duas no tom da marca (curva com buracos / compartilhar é ativo). Copy em
   `res/values/strings.xml` (`ob2_*`, `ob3_*`).
2. **"Criar evento"**: a janela precisa de início/fim, e não existe tela desenhada
   para isso — fiz uma **sheet mínima** (nome + lugar + "Começa agora") em vez de
   inventar tela nova (`CreateEventSheet`).
3. **b4 no onboarding** mede as últimas 24h (não há evento ainda) e diz isso no
   rótulo; no fim da noite mede a janela real do evento.
4. **"Já tenho conta"** leva ao mesmo formulário por enquanto — login de verdade
   depende do backend.
5. **A galera (a4)** abre a partir da revela (link "SUA NOITE × A GALERA");
   a curva do agregado é fake (versão amortecida da sua), trocável com o backend.
6. **Barra de navegação** segue a rodada 2 (FEED · AO VIVO · VOCÊ) — os mockups da
   rodada 1 (NOITES/GALERA) foram substituídos. a2/a3 são tela cheia, sem barra,
   como nos mocks.
7. **Galeria/suas noites começam vazias de verdade** — dado do próprio usuário
   nunca é inventado; só o feed social usa dados fake (como manda o §2).

## Pendências conhecidas

- **Build não verificado neste ambiente** (sem SDK); erros de compilação, se
  houver, devem ser pontuais — me mande o log.
- **Wordmark autotrace** (606 retas): os VectorDrawables funcionam em tela, mas
  o handoff pede o vetor desenhado à mão antes dos densities finais.
- Ícones da status bar ficam escuros também nas telas pretas (a2/a3) — ajuste
  fino de `WindowInsetsController` por tela ficou de fora desta passada.
- Fonte Instrument Sans embarcada como variável (`res/font/`), pesos via
  `FontVariation` — requer minSdk 26+ (ok, minSdk 28).
