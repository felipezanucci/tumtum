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
