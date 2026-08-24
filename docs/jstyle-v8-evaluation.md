# J-Style V8 — Status da validação e próximo teste (SDK no Android)

**Última atualização:** 24/08/2026, com base na sessão de validação de 17/08/2026
(repo local `tumtum-hardware`; relatórios `relatorio_V8.md` e `relatorio_2208A.md`).

## Status: V8 e 2208A REPROVADOS na validação de hardware

A validação real dos wristbands J-Style não é feita por importação de arquivo —
ela roda no repo `tumtum-hardware` (fora deste repo), com Polar H10 como
referência ECG e o harness `tumtum_ble.py`. Resultados consolidados:

- **Fase 0 (inspeção GATT):** ambos reprovados. Nenhum expõe o Heart Rate
  Service padrão (0x180D); só serviços proprietários (`fff0`/`190e` no V8,
  mais um terceiro no 2208A). Com o V8 como DUT, `log` conecta mas recebe
  **0 pacotes** — a série do V8 veio de gravação de tela do app JCVitalPro
  (modo "GPS Caminhada").
- **Fase 1 do V8 (3 blocos: 60s repouso / 30s esforço máximo / 90s repouso):**
  reprovado. Diagnóstico central: **motion gating por acelerômetro no
  firmware** — parado, o filtro trava a rampa de subida (razão de amplitude
  0,46–0,64; atraso de pico 23–33s); em movimento, libera (razão 0,87, atraso
  2–8s) e projeta por cadência, com overshoot na descida. MAE < 2 em regime
  estável nos 3 blocos: o sensor é bom, o problema é 100% firmware.
- A condição **parada é eliminatória** para a Tumtum: pico emocional sem
  movimento corporal é o caso central do produto (show, gol assistido da
  arquibancada).

## Critérios de aprovação (protocolo permanente, 2 condições)

Todo candidato roda o protocolo **parado E em movimento**, contra Polar H10:

| Critério | Limite |
|---|---|
| Razão de amplitude do pico | ≥ 0,85 |
| Atraso do pico | ≤ 5s |
| MAE | ≤ 5 |
| Transporte | 0x180D presente, ou stream ao vivo ~1Hz via SDK documentado |

## O teste pendente — spike de PPG bruto no Samsung A17

A etapa "aguardar SDK" já passou (sessão de 20–21/08, branch
`claude/jstyle-bracelet-analysis-cm8u8z`): o SDK chegou, foi analisado por
completo (~150 MB, Android com código-fonte + iOS fechado) e a J-Style
confirmou **por escrito** (Arena Lee, 20/08) que o motion gating é firmware e
não pode ser desligado. O caminho do BPM pronto está morto; o caminho vivo é o
**PPG bruto**: o V8 Android já expõe streaming do sinal óptico cru
(`setECGRealtimeDuringHRVEnabled(true)` durante medição de HRV), antes do
algoritmo — Tumtum calcula o BPM no próprio backend.

O teste que decide a compra já está montado e publicado:

- **App instrumentado**: demo oficial da J-Style com a tela de PPG modificada
  para gravar cada amostra crua em CSV, registrar o HR do firmware em
  paralelo, re-armar a sessão automaticamente e exportar para
  `Downloads/tumtum_spike/`. Código em `hardware/jstyle-spike/` (branch acima).
- **APK pronto**: release `spike-apk`
  (github.com/felipezanucci/tumtum/releases/tag/spike-apk,
  `tumtum-spike-v8.apk`), recompilado pelo CI a cada push no spike.
- **Análise**: `analyze_ppg.py` deriva HR 1 Hz do PPG cru e compara com Polar
  H10 e com o firmware (smoke-testado com PPG sintético: MAE 0,7, atraso 2s,
  razão 0,99).
- **Host do teste**: Samsung Galaxy A17 (Android; em mãos desde 24/08).

Critérios de decisão do spike: atraso de pico parado ≤ 5s, amplitude ≥ 85%,
perda de pacotes < 5%, sessão de 4h sem intervenção. Verde nos quatro ⇒
fechar com a J-Style e negociar o pacote de integração (US$ 15k viram item de
negociação). Vermelho em qualquer um ⇒ levar os números à Arena e acelerar a
Veepoo.

## Bancada de 24/08 — Samsung A17 (spike v1→v4)

Primeira execução do spike com o A17 como host. O PPG bruto **não fluiu**, mas
a rodada produziu achados de protocolo que valem para a negociação:

**Stream de PPG bruto (AutoHRV + `setECGRealtimeDuringHRVEnabled`)**
- Rodada 1: exatamente **2 pacotes (160 amostras de PPG válidas)** 2,1s após o
  Start, depois **silêncio total por 38s** — sem nenhum callback de encerramento
  do dispositivo, o que impediu o re-arme orientado a callback de disparar.
- Rodadas seguintes: **zero pacotes**, mesmo com watchdog re-armando a cada 5s
  (~50 re-armes numa rodada de 4 min). A pulseira **ignora o comando em
  silêncio** — não recusa, não responde.
- Pulseira no pulso em todas as rodadas: a detecção de uso não explica o
  comportamento.

**Stream de 1 Hz (`RealTimeStep`) — o achado positivo**
- **241 callbacks em 4 minutos, mediana de 1,004s entre pacotes, zero
  interrupções.** O transporte a ~1 Hz prometido pela J-Style existe e é
  estável — atende ao critério de transporte do protocolo de validação.
- Porém `heartRate=0` em todos os 241 pacotes: o stream só carrega batimento
  enquanto uma **sessão de medição está ativa**, e o comando de HRV do spike
  derrubava a medição de HR.
- Os passos ficaram **congelados em 143** durante toda a rodada, inclusive
  durante esforço físico real — o stream parece repetir um snapshot em vez de
  atualizar ao vivo sem sessão ativa. Confirmar com a J-Style.

**Canal de comandos**: íntegro. `Get Battery Level` respondeu
(`batteryLevel=88, VoltageValue=578`) e `Real Time Measurement` mediu batimento
(77–78 bpm) — logo, a conexão BLE e o SDK funcionam; o problema é específico da
sessão de medição do PPG bruto.

**Causa provável identificada no código do demo**: a tela que funciona passa a
duração da sessão em **segundos** (campo com aviso de mínimo 30s), enquanto o
`PPGActivity` passava `50 * 1000` — plausivelmente lido como pedido de ~14h e
descartado. Corrigido na v4 (300s), junto de um modo de fallback que grava a
série de HR de 1 Hz do SDK quando o PPG bruto não flui.

**Rodada 4 (16:32, spike v4) — o PPG bruto FLUIU**
- **10 pacotes, 800 amostras, ~394 amostras/s**, forma de onda óptica real e
  contínua em 8 dos 10 pacotes (o pacote 1 traz ~33 valores de cabeçalho
  misturados às amostras — o parser do SDK não separa payload de metadados).
- Durou **exatamente 2,03s**, começando **59ms após um re-arme do watchdog** —
  e os **47 re-armes seguintes não produziram nada**.
- Conclusão de protocolo: **o firmware trata "start" como no-op enquanto acredita
  que há sessão ativa**. Repetir start nunca recupera o stream; é preciso
  `stop` → pausa → `start` (implementado na v5).
- 2s é curto demais para extrair BPM: o trecho ainda está no transiente de
  estabilização do sensor (coerente com o warm-up de ~40s medido em julho).
- A série de HR não foi capturada: o modo AutoHRV derruba a medição de HR, e o
  fallback do app exigia "zero pacotes de PPG" — a rajada de 2s bloqueou a troca.
  Corrigido na v5 (o fallback passa a olhar o total de PPG efetivamente
  transmitido).

**Rodada 5 (17:00, spike v5) — a série de HR do SDK e a assinatura do filtro**
- **Zero pacotes de PPG.** Causa identificada e nossa: o watchdog fazia toggle a
  cada 6s, e o **warm-up do sensor é de 8–9s** — toda sessão morria antes de o
  sensor ficar pronto. Explica também por que a rodada 4 capturou a rajada
  exatamente aos 10s (re-arme mais lento). Corrigido na v6 (watchdog de 30s).
- **Duração da sessão confirmada em SEGUNDOS**: pedimos 60s e recebemos 9s de
  warm-up + exatamente 51s de HR a 1 Hz.
- **A série de HR de 1 Hz do SDK funciona**: bloco contínuo de 51s, sem falhas.
- **Assinatura do filtro visível no dado do SDK** (achado principal): duas
  reaquisições de sessão, com o sujeito **em repouso** e batimento real ~77 bpm,
  começaram em 73 e 67 bpm e **ramparam a 0,50 e 0,59 BPM/s** até o valor
  verdadeiro. Compare com o relatório de julho: rampa de **0,51 BPM/s** parado,
  contra subida real de 1,7–2,5 BPM/s medida no Polar H10.
- **Leitura preliminar do teste decisivo (seção 8, item 2)**: o SDK entrega o
  **mesmo valor filtrado** que o app — indício forte de que o gating está no
  firmware, não na camada de aplicação. Ressalva: trata-se de rampa de
  reaquisição, não de resposta a pico de esforço; o veredito formal exige o
  protocolo de 2 condições contra o Polar H10.

**Rodada 6 (17:18, spike v6) — protocolo de esforço: a V8 não vê o pico**

Protocolo: repouso → 30s de esforço máximo → sentar imediatamente → repouso.
Fim do esforço informado pelo operador: **17:22:00**. Sem Polar H10 nesta rodada
(o evento de esforço serve como referência temporal), 292 leituras de HR
contínuas a 1 Hz.

| Momento | HR reportado pela V8 |
|---|---|
| Durante os 30s de esforço máximo (17:21:20–17:22:00) | **74–79 bpm, plano** — chega a *cair* para 74 |
| Início da subida | ~17:22:05, já sentado |
| Primeiro sub-pico | 98 bpm às 17:22:28 (**+28s** após o fim do esforço) |
| Queda espúria | 95 → 60 bpm entre 17:22:40 e 17:23:00 (**−35 bpm em 20s**, fisiologicamente impossível na recuperação) |
| Pico máximo | **100 bpm às 17:23:34 — +94s após o fim do esforço** |
| Taxa máxima de subida | **1,33 BPM/s** (real, medida no Polar em julho: 1,7–2,5 BPM/s) |

Leitura:
- **Durante o esforço a V8 não registrou absolutamente nada.** Trinta segundos de
  esforço máximo produziram variação de −4 bpm no valor reportado. É o motion
  gating operando exatamente como a J-Style descreveu por escrito.
- **Atraso do pico: +94s** (ou +28s no sub-pico mais caridoso) contra o critério
  de **≤5s**. Reprovado por uma ordem de grandeza.
- A **queda espúria de 35 bpm** é nova: o algoritmo perde o sinal e reinicia a
  reaquisição no meio da recuperação. Isso não aparecia nos testes de julho e
  agrava o quadro — não é só lentidão, é instabilidade.
- **Amplitude**: 100 bpm de máximo após 30s de esforço máximo é baixo demais para
  ser verdade; sem o Polar nesta rodada não dá para cravar a razão de amplitude,
  mas a evidência aponta para bem abaixo dos 0,85 exigidos.

**Conclusão para a decisão**: o caminho do BPM do SDK está **reprovado com dado
próprio**. O SDK entrega o mesmo valor filtrado do app — confirma a resposta da
Arena de 20/08 e fecha o item 2 da seção 8: **o gating é firmware**.

**PPG bruto**: continua bloqueado. Nas rodadas 5 e 6 (v5 e v6), **zero pacotes**
em 5 tentativas de sessão AutoHRV; a única captura segue sendo a rajada de 2s da
rodada 4. Hipótese a testar: o `stop` do toggle desabilita o flag de PPG bruto de
forma que o `true` seguinte não restaura — a rodada 4 nunca enviou `false`.

**Rodada 7 (17:57, spike v7) — sonda A/B/C: o PPG bruto não é reproduzível**

Experimento controlado, três configurações numa gravação de 246s, com reset
físico da pulseira antes. Sujeito em pé, parado, sem esforço (irrelevante para
um resultado de zero pacotes: ausência total de dados é falha de protocolo, não
de qualidade de sinal).

| Fase | Configuração | Comandos | Pacotes PPG |
|---|---|---|---|
| A (75s) | receita exata da v4: sessão 300s, re-arme só com *start* a cada 5s, flag **nunca** posto em `false` | 15 | **0** |
| B (75s) | duração literal do demo do fornecedor (`50*1000`), mesmo re-arme | 15 | **0** |
| C (75s) | parada limpa + **um único start**, 75s sem qualquer interferência | 1 | **0** |
| HR (21s) | fallback `AutoHeartRate` 300s | 1 | — (HR também ficou em 0) |

**Conclusão: zero pacotes de PPG bruto em 32 comandos de medição e três
configurações independentes.** A hipótese do flag `false` está descartada: a
fase A replicou a v4 byte a byte, em estado virgem, e não produziu nada. A
rajada de 2s da rodada 4 **não é reproduzível** com nenhuma sequência de
comandos que se consiga montar a partir do SDK entregue.

Achado secundário (sugestivo, janela curta): após os 32 comandos, o dispositivo
parou de responder até ao `AutoHeartRate` — 19s sem nenhum HR, contra warm-up de
8–9s medido nas rodadas anteriores. Indica degradação do firmware sob comandos
repetidos, exigindo reset físico.

## Rodada 8 (18:33, spike v8) — protocolo de 2 condições com Polar H10

Protocolo de julho reproduzido: 3 blocos de 60s repouso / 30s esforço máximo /
90s recuperação **sentado**, com Polar H10 (ECG) como referência gravado pelo
`tumtum_ble.py` no Mac, e a série de 1 Hz do SDK do V8 gravada em paralelo no
A17 (um único Start cobrindo os três blocos). Alinhamento pelos **relógios
absolutos** dos dois aparelhos — deliberadamente não se usou minimização de MAE,
que absorveria no "offset" justamente o atraso a ser medido.

| Bloco | Baseline | Pico Polar | Pico V8 | Atraso | Razão de amplitude | MAE |
|---|---|---|---|---|---|---|
| 1 | 80,9 | **129** | 108 | **+42s** | **0,56** | **19,1** |
| 2 | 78,1 | **136** | 92 | n/d* | **0,24** | **17,3** |
| 3 | 87,7 | **138** | 97 | n/d* | **0,21** | **16,1** |
| **Média** | | | | | **0,34** | **17,5** |

\* Nos blocos 2 e 3 a métrica de atraso pico-a-pico é inválida: o V8 nunca
rastreou a subida, então o horário do seu máximo é ruído, não resposta. Métrica
robusta substituta — **tempo para atingir metade da excursão real**:

| Bloco | Polar cruza | V8 cruza | Atraso | Erro máximo |
|---|---|---|---|---|
| 1 | 18:34:50 | 18:35:44 | **+54s** | −47 bpm |
| 2 | 18:40:07 | **nunca** | — | −45 bpm |
| 3 | 18:45:21 | **nunca** | — | −47 bpm |

**Comportamento observado (bloco 1, ilustrativo):** durante os 30s de esforço o
Polar sobe de 81 → 128 bpm; o V8 vai de 78 → 82. Quando o Polar já está em plena
recuperação (89 bpm), o V8 finalmente sobe até 107 — e passa a **superestimar em
+18 a +27 bpm**. Ou seja: cego durante o pico, e errado na direção oposta depois.

**Resultado contra os três critérios do protocolo permanente:**

| Critério | Limite | Medido | |
|---|---|---|---|
| Razão de amplitude | ≥ 0,85 | **0,34** (0,21–0,56) | ❌ |
| Atraso do pico | ≤ 5s | **+42s a +54s**, ou nunca | ❌ |
| MAE | ≤ 5 | **17,5** (16,1–19,1) | ❌ |

**Os três critérios reprovados, nos três blocos, contra referência ECG.**

## Veredito da avaliação (24/08)

| Caminho | Status |
|---|---|
| **A — BPM processado (firmware/SDK)** | **Reprovado com dado próprio.** Atraso de pico +94s (critério ≤5s), nenhuma resposta durante 30s de esforço máximo, queda espúria de 35 bpm na recuperação. O SDK entrega o mesmo valor filtrado do app: **o gating é firmware**, fechando o item 2 da seção 8. |
| **B — PPG bruto** | **Não demonstrável com o SDK entregue.** Três configurações, 32 comandos, zero pacotes. |

**A avaliação está completa.** O protocolo com Polar H10 (rodada 8) fechou os
três critérios: razão de amplitude 0,34, atraso de +42s a +54s (ou pico nunca
alcançado), MAE 17,5. Nada pendente do lado técnico.

**Perguntas cirúrgicas para a Arena (com log em mãos)**
1. Qual a sequência de comandos correta para manter o stream de PPG bruto
   **contínuo**? O nosso entrega ~2s (≈394 amostras/s) por sessão e depois só
   volta com um `stop` explícito antes do próximo `start`. Há um modo de stream
   sustentado, ou a duração da rajada é fixa em firmware?
2. Qual a unidade e o limite do parâmetro de duração em
   `SetDeviceMeasurementWithType`? Ele governa a duração da rajada de PPG?
3. O pacote de PPG mistura bytes de cabeçalho às amostras (visto no pacote 1):
   qual o layout exato do frame e a taxa de amostragem nominal do sensor?
4. O `RealTimeStep` só entrega `heartRate` durante sessão de medição ativa e
   repete o snapshot de passos — é o comportamento esperado?

## Panorama de fornecedores (17/08)

| Fornecedor | Status |
|---|---|
| J-Style (V8 + 2208A) | BPM de firmware morto (confirmado por escrito 20/08); avaliação segue pelo spike de PPG bruto |
| Veepoo (H Band SDK) | Contato inicial redigido; se confirmar raw PPG/RR, vira candidato principal |
| Amazfit Bip 6 (Zepp OS 3.0+, broadcast BLE 0x180D) | Candidato nº 1 de prateleira; comprar 1 unidade e rodar o protocolo de 2 modos |
| Braçadeiras (Chileaf, Verity Sense) | Descartadas por decisão de produto (usabilidade) |

## Relação com este repo (Phase 0 / MVP)

A crise de fornecedor **não bloqueia o piloto** (Caminho 2): o primeiro evento
real pode rodar com relógios dos próprios fãs (Apple Watch / Wear OS) +
Amazfit emprestados. Neste repo, isso aparece como:

- A tela **`/import`** (importação de arquivo com análise de qualidade) serve
  para dados vindos de wearables dos usuários — Samsung Health, Health
  Connect, Apple Saúde — não para a validação de hardware da J-Style.
- O contrato único de BPM do Caminho 2 —
  `{source, bpm, timestamp, quality}` — é a direção para a ingestão quando
  houver captura ao vivo; o backend atual (`POST /api/health/sessions`) já
  recebe séries com essa informação por sessão.
