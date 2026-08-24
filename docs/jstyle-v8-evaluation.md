# JStyle V8 — Protocolo de Avaliação (Fase 0)

**Objetivo:** decidir se a JStyle (Joint Corp) serve como fornecedora de hardware
para o Tumtum, usando a pulseira **JCVital V8** como unidade de teste.

**Setup do teste:** Samsung Galaxy A17 (Android) + pulseira JStyle V8 + app do
fornecedor (JCVital / app indicado pela JStyle) + Tumtum PWA.

> O Galaxy A17 não tem sensor de frequência cardíaca próprio — toda a medição
> vem da pulseira. O celular é só o coletor e o ponto de acesso ao Tumtum.

---

## O que estamos medindo

A detecção de picos do Tumtum (ver CLAUDE.md) impõe requisitos duros ao sensor:

| Requisito | Alvo | Mínimo utilizável | Por quê |
|---|---|---|---|
| Cadência de amostragem | 1 leitura / 5s | 1 leitura / 30s | Média móvel de 5s + descarte de picos < 5s |
| Cobertura da sessão | ≥ 80% | ≥ 50% | Baseline rolante de 60s precisa de janela contínua |
| Duração contínua | ≥ 3h (show inteiro) | ≥ 2h | Evento típico: show ou jogo com prorrogação |
| Exportação dos dados | CSV/JSON com timestamp + BPM | qualquer arquivo legível | Fase 0 importa por arquivo; Fase 1 usa SDK BLE |

A tela **Importar** do Tumtum (`/import`) calcula essas métricas automaticamente
e dá o veredito: **Aprovado / Limítrofe / Reprovado**.

## Roteiro do teste

### Preparação (uma vez)
1. Carregue a V8 por completo e anote o horário de início da carga.
2. Instale o app da JStyle no A17 e pareie a pulseira.
3. No app da JStyle, configure a medição contínua de FC no **menor intervalo
   disponível** (ideal: contínuo ou 5s; anote qual é o mínimo que o app permite).
4. Confira se o relógio do A17 está em horário automático (o timestamp das
   leituras depende disso).
5. Instale o Tumtum PWA no A17 (abra o app no Chrome → menu → "Adicionar à tela
   inicial") e crie sua conta.

### Sessão de teste A — bancada (30–60 min)
Valida o pipeline antes de gastar um evento real.
1. Use a pulseira por 30–60 min alternando repouso e esforço (subir escada,
   polichinelos) para forçar picos reais.
2. Sincronize a pulseira com o app da JStyle.
3. Exporte os dados de FC do app (CSV ou JSON; se o app só compartilhar via
   e-mail/planilha, serve também).
4. No Tumtum: **Importar** → selecione o arquivo → confira o veredito de
   qualidade → "Ver minha experiência".
5. **Critério de sucesso:** veredito "Aprovado" e os picos detectados batem com
   os momentos de esforço.

### Sessão de teste B — evento real (jogo ou show)
1. Repita o fluxo em um evento de verdade, com o evento cadastrado no Tumtum
   (com linha do tempo: setlist ou gols).
2. Na importação, vincule ao evento — os picos serão casados com os momentos.
3. **Critério de sucesso:** curva completa do evento, veredito ≥ "Limítrofe",
   picos casando com momentos reais, e bateria da V8 sobrevivendo ao evento.

## Scorecard do fornecedor

Preencha depois das duas sessões:

| # | Critério | Peso | Como medir | Nota (0–5) |
|---|---|---|---|---|
| 1 | Cadência de FC | Alto | Métrica "Cadência" da tela Importar | |
| 2 | Cobertura / interrupções | Alto | Métricas "Cobertura" e "Interrupções" | |
| 3 | Exportação de dados | Alto | Formato, atrito (nº de toques), completude | |
| 4 | Bateria em sessão longa | Médio | % de bateria antes/depois do evento | |
| 5 | Conforto e uso em show | Médio | Incomoda? Solta? Chama atenção? | |
| 6 | Qualidade do app do fornecedor | Baixo | Estabilidade do pareamento e sync | |
| 7 | SDK/BLE para a Fase 1 | Alto | Docs do SDK, protocolo aberto, custo | (avaliação de mesa) |

**Regra de decisão sugerida:** qualquer critério de peso Alto com nota ≤ 2
desqualifica a V8 para a Fase 1 sem renegociação com o fornecedor.

## Limitações conhecidas da Fase 0
- A importação é por arquivo — não há sync automático com a V8 (BLE/streaming é
  Fase 1, fora do escopo do MVP por decisão de projeto).
- Se o app da JStyle não exportar arquivo nenhum, o teste já responde o critério
  3 com nota 0 — e vira pergunta comercial para a JStyle: "o SDK dá acesso ao
  dado bruto?".
- O veredito de qualidade avalia o **dado exportado**, que pode ser reamostrado
  pelo app do fornecedor. Se a cadência vier ruim, confirmar com a JStyle qual é
  a cadência real do sensor via SDK antes de reprovar o hardware.
