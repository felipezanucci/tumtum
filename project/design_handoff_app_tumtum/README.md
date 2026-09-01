# TumTum — Handoff do app (Android)

**Versão:** 1.0 · 1 set 2026
**Para:** Claude Code — primeira implementação do app
**Referência visual viva:** `TumTum-App.dc.html` (na raiz do projeto de design) — 15 telas em frame Android real. Abra e navegue por ids (`#3a`, `#b1`, `#a2`…).
**Manual da marca:** `TumTum-Manual-da-Marca.dc.html` (18 páginas) — manda em qualquer dúvida de cor, tipo e tom.
**Design system:** projeto `TumTum Design Systems` (tokens + 8 componentes + SKILL.md).

---

## 1. O que o app é

O TumTum lê a frequência cardíaca que o relógio do usuário **já registra** durante um show ou jogo e devolve a noite dele como história: onde o coração disparou, com o que estava acontecendo naquele segundo. Depois ele decide o que compartilha.

Três coisas que definem o produto e não são negociáveis:

1. **Nunca inventamos batida.** Buraco de dado aparece como buraco, visível. Zero interpolação cosmética.
2. **Não é saúde.** Sem diagnóstico, sem alerta, sem "sua zona cardíaca". É memória e emoção.
3. **Compartilhar é sempre ativo.** Nada sai do aparelho sem o usuário mandar.

---

## 2. Stack esperado

- **Android nativo**, Kotlin + Jetpack Compose. Sem cross-platform nesta fase.
- **Health Connect** como única fonte de FC (`HealthPermission.getReadPermission(HeartRateRecord::class)`). Sem SDK proprietário de fabricante.
- Leitura **em lote, retroativa**, na janela do evento — o app não fica lendo sensor em tempo real.
- minSdk 28 · targetSdk mais recente estável.
- Persistência local primeiro (Room). O feed social vem depois do backend; nas telas de feed, use dados fake de repositório trocável.

---

## 3. Fundação visual

### Cor
| Token | Hex | Uso |
|---|---|---|
| `ink` | `#000000` | Texto, botão primário em fundo claro, número da batida |
| `paper` | `#FFFFFF` | Fundo padrão do app |
| `rose` | `#FF6F91` | Splash, CTA de criação, acento de identidade |
| `acid` | `#EFFF00` | Destaque pontual, badge, "AO VIVO" |
| `gray-70` | `#4A4A4A` | Texto secundário |
| `gray-45` | `#8A8A8A` | Texto terciário, placeholder |
| `gray-25` | `#B4B4B4` | Borda de campo em repouso |
| `gray-10` | `#E6E6E6` | Divisor, borda suave |
| `night` | `#0A0A0A` | Telas de captura e revela |

**Regras rígidas de cor (do manual):**
- Número grande de BPM é **sempre preto**, exceto sobre fundo preto — aí é branco. **Nunca rosa.**
- Máximo 2 cores de fundo por fluxo. Rosa e ácido são acento, não tapete.
- Texto sobre vídeo mora em **plate opaco** (retângulo sólido), nunca direto sobre a imagem, nunca com sombra.

### Tipografia
- **Instrument Sans** para toda a UI. Pesos 400/500/600/700.
- **Mutante Pop** só em peça de campanha e nas 14 rotações de pôster de evento. Nunca na UI.
- Título de tela: 27–31px / 700 / tracking −0.02em.
- Corpo: 15px / 1.5.
- Meta-label: 12px / 600 / tracking 0.08em / caixa alta.
- Número herói: 64–120px / 700 / tracking −0.04em.

### Forma e espaço
- Botão: raio 12dp, altura 56dp, label 16/600.
- Card: raio 12dp. Chip/badge/meta-label: **raio 0** — retângulo seco.
- Grid de 4dp. Padding lateral padrão de tela: 28dp (24dp em telas com lista de borda a borda).
- Alvo de toque mínimo 48dp.

---

## 4. Animação de abertura — implementar exatamente assim

O splash é a assinatura do produto: o app abre batendo. Referência ao vivo em `#3a` e `#3b` do doc de telas.

**Composição:** fundo `#FF6F91` ocupando tudo, wordmark preto centrado no centro óptico, largura = **46% da largura da tela**. Mais nada. Sem tagline, sem spinner, sem barra de progresso.

**Timeline (total ~2000ms):**

| Tempo | O que acontece |
|---|---|
| 0 – 120ms | `#FF6F91` já pintado como `windowBackground` do theme de splash — **zero flash branco** no cold start. |
| 120 – 460ms | Wordmark entra: opacidade 0 → 1, escala 0,82 → 1,045 → 1,00. Easing `cubic-bezier(.2,.9,.25,1)`. |
| 460 – 1410ms | **Frase 1 — tum-tum.** Ciclo de 950ms: escala 1,00 → **1,06** (66ms) → 1,00 (170ms) → **1,035** (247ms) → 1,00 (360ms) → parado até 950ms. |
| 1410 – 2360ms | **Frase 2** — a mesma frase, uma vez só. |
| ~2000–2360ms | Corte seco para o FEED. Sem fade, sem escala, sem transição compartilhada. |

**Regras:**
- São **duas frases e acabou**. Não é loop infinito.
- A saída acontece **no fim de uma frase**, nunca no meio de um toque.
- Se a sessão ainda não resolveu quando a segunda frase termina, o rosa fica **parado** (wordmark visível, sem batida) até resolver. Nunca reinicia a batida.
- A batida só existe em **escala**. O wordmark não troca de cor, não gira, não desmonta em letras, não ganha glow.
- **Acessibilidade:** com `Settings.Global.ANIMATOR_DURATION_SCALE == 0` ou "reduzir animação" ativo → wordmark estático em escala 1, sem batida, saída em 800ms.
- Só no **cold start**. Voltar de background não mostra splash.

Compose, esqueleto:

```kotlin
// escala do "tum-tum": keyframes de 950ms, 2 iterações
val beat = remember { Animatable(1f) }
LaunchedEffect(Unit) {
    repeat(2) {
        beat.animateTo(1f, keyframes {
            durationMillis = 950
            1f    at 0
            1.06f at 66  with FastOutSlowInEasing
            1f    at 170
            1.035f at 247
            1f    at 360
            1f    at 950
        })
    }
    onSplashFinished()
}
```

---

## 5. Navegação

Três zonas, barra inferior fixa, label sempre visível (sem ícone-só):

```
FEED          AO VIVO           VOCÊ
```

- **FEED** — momentos de quem você segue + feed do evento que você está.
- **AO VIVO** — captura da noite corrente. Quando não há evento ativo, é o estado de espera/vazio.
- **VOCÊ** — suas noites, a galeria de sentimentos, o perfil público, configurações.

Nada de gaveta lateral. Nada de FAB. O botão de criar mora dentro do contexto (na noite, no card).

---

## 6. Mapa de telas

Referência: cada linha aponta para o id navegável no doc de telas.

### Abertura
| id | Tela | Notas de implementação |
|---|---|---|
| `3a` | Splash pulsando | Seção 4 acima. Theme de splash + Compose. |
| `3b` | Corte para o FEED | Camada rosa sai de uma vez; o FEED já está montado atrás. |

### Entrada
| id | Tela | Notas |
|---|---|---|
| `b1` | Onboarding | Fundo rosa, 3 páginas (dots), "O que você sentiu vira história." + plate preto "Com prova." |
| `b2` | Criar conta | Nome, @username (valida disponibilidade ao digitar), email, senha. Tribos = chips opcionais, ácido quando selecionado. CTA rosa. |
| `b3` | Permissão — **a tela quieta** | 4 linhas: 2 ✓ (o que fazemos) e 2 ✕ (o que não fazemos). CTA preto "Permitir leitura" + "Agora não" com borda. **Não pedir permissão antes desta tela.** Revogável em Configurações; apagar conta apaga dados. |
| `b4` | Trazer do meu relógio | Lista as fontes do Health Connect com **densidade real** medida na janela do evento (% de cobertura + intervalo de amostra). Melhor fonte ganha badge ácido e borda 2dp preta. Fonte sem dado fica cinza, não clicável. |

### Social
| id | Tela | Notas |
|---|---|---|
| `b5` | Feed — seus amigos | Linhas de momento: avatar, @, evento, número de BPM, faixa/minuto, ação. |
| `b6` | Feed do evento | Todo mundo que estava no mesmo evento. Ordenação por pico. |
| `b7` | Galeria de sentimentos | Grade dos pôsteres de evento (14 rotações Mutante Pop) que o usuário coleciona. |
| `b8` | Perfil público | O que outra pessoa vê: momentos publicados, eventos, contagem de SENTI TB. |

**Reação: existe uma só — `SENTI TB`.** Sem like, sem coração, sem emoji, sem contador de reações variadas. Toggle, uma vez por momento por usuário.

### Núcleo da noite
| id | Tela | Notas |
|---|---|---|
| `a1` | Suas noites | Lista cronológica das noites capturadas. |
| `a2` | Captura ao vivo | Fundo `#0A0A0A`. Estado calmo, quase sem UI — o app está só marcando a janela. |
| `a3` | A noite — a revela | Fundo preto. Curva de BPM da noite + picos ancorados no que tocava. **O momento de maior impacto do produto** — animar a curva desenhando da esquerda para a direita, 1,2s, e só então revelar os picos. |
| `a4` | A galera | Comparação da sua noite com a do público do evento. |
| `a5` | Vazio — primeiro uso | Estado sem noite nenhuma. Convida a marcar o próximo evento, não a comprar nada. |

---

## 7. Dados e privacidade — o que o código precisa garantir

- **Janela de evento**: leitura só entre início e fim do evento marcado, com margem de 30min de cada lado. Fora disso, nenhuma query de FC.
- **Escolha de fonte**: para cada fonte disponível, calcular cobertura (amostras / segundos da janela) e intervalo mediano. Usar a de maior cobertura. Mostrar a decisão para o usuário (`b4`), nunca decidir escondido.
- **Buracos**: gap > 60s aparece na curva como interrupção da linha. Não interpolar.
- **Nada de rede sem ação do usuário.** A captura e a revela funcionam 100% offline.
- **Revogar permissão** → app continua abrindo, noites já capturadas continuam lá, nova captura fica bloqueada com explicação.
- **Apagar conta** → apaga noites, momentos publicados e reações. Irreversível, avisado uma vez.

---

## 8. Tom de voz

Português brasileiro, curto, falado, primeira e segunda pessoa. "Sua noite", "a galera", "senti tb". Sem gerúndio corporativo, sem "experiência única", sem exclamação, sem emoji. Números por extenso nunca — número é número, grande e preto.

Erro é honesto: "Não achamos batida nessa janela." Não "Ops! Algo deu errado."

---

## 9. Ordem de construção sugerida

1. Splash (seção 4) + theme + navegação de 3 zonas com telas stub.
2. `b1` → `b2` → `b3` → `b4`: onboarding e permissão de verdade, com Health Connect ligado.
3. `a5` → `a2` → `a3`: marcar evento, capturar janela, revelar a noite. **Aqui está o produto.**
4. `a1`, `a4`: histórico e comparação.
5. `b5` – `b8`: social, com repositório fake até o backend existir.

Pare e pergunte antes de: inventar tela nova, mudar a barra de 3 zonas, adicionar uma segunda reação, ou pintar número de BPM de rosa.

---

## 10. Assets nesta pasta

- `assets/tumtum-wordmark-black.svg` — wordmark para fundo claro e para o splash rosa.
- `assets/tumtum-wordmark-white.svg` — wordmark para fundo preto.
- `assets/splash-ref-*.png` — frames do vídeo de referência da abertura, na ordem.

**Pendência conhecida:** os SVGs de wordmark são autotrace (606 rects em vez de curvas). Funcionam em tela, mas peça o vetor desenhado à mão antes de gerar os densities finais do Android.
