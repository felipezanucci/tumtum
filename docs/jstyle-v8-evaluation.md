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

## O teste pendente — seção 8: SDK da J-Style no Samsung A17

A J-Style respondeu ao e-mail técnico (positiva, sem compromisso) e o **SDK
Android/iOS está a caminho**. O Samsung Galaxy A17 é o host Android para rodar
esse SDK (o MacBook Air 2015/Monterey não compila apps iOS/watchOS modernos).

Quando o SDK chegar:

1. **Stream ao vivo existe a ~1Hz?** Se sim, substitui a gravação de tela;
   refazer a Fase 1 com captura automatizada (app de captura Android no A17,
   gravando no mesmo formato CSV do `tumtum_ble.py`:
   `timestamp_iso, elapsed_s, source, hr_bpm, rr_ms, contact, raw_hex`).
2. **Teste decisivo — onde mora o filtro:** protocolo parado capturando pelo
   SDK, comparando com o valor exibido no app JCVitalPro.
   - SDK **menos filtrado** que o app ⇒ gating na camada de aplicação ⇒
     problema contornável sem firmware novo (V8 volta ao jogo).
   - SDK **idêntico** ao app ⇒ gating no firmware ⇒ decisão fica pendurada na
     resposta da engenharia da J-Style (customização, provavelmente
     condicionada a MOQ).

## Panorama de fornecedores (17/08)

| Fornecedor | Status |
|---|---|
| J-Style (V8 + 2208A) | Reprovados como estão; aguardando SDK e resposta da engenharia |
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
