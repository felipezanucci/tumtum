# 🎛️ Painel de Controle

A única página que você precisa abrir no dia a dia. Ela não guarda nada: são todas visões
filtradas das databases. Você atualiza a tarefa, o painel se atualiza sozinho.

Monte nesta ordem. Para cada bloco: digite `/linked` → **Visualização vinculada de banco de
dados** → escolha a database → configure filtro e layout como descrito.

---

## Bloco 1 — Onde estamos

**Fonte:** 🎯 Etapas · **Layout:** Galeria (cartão grande) · **Filtro:** `Ativa` está marcada
**Mostrar:** Objetivo, Progresso (barra), Pendências, Período, Risco

Um único cartão grande no topo, com a barra de progresso da etapa atual. É a primeira coisa que
seus olhos batem ao abrir o Notion.

> Regra: só uma etapa com `Ativa` marcada por vez. Ao fechar uma etapa, desmarque e marque a
> próxima — o painel inteiro se realinha nesse único clique.

---

## Bloco 2 — ⏳ Falta isso para fechar a etapa

**Fonte:** ✅ Tarefas · **Layout:** Tabela
**Filtro:** `Etapa ativa` está marcada **E** `Feita` não está marcada
**Ordenar:** Prioridade ↑, depois Prazo ↑
**Mostrar:** Tarefa, Status, Área, Responsável, Prazo, Atrasada

Esta é a resposta literal para "o que falta para terminar essa etapa". Se este bloco está vazio,
a etapa acabou — vá para o retrô e ative a próxima.

*Se o rollup `Etapa ativa` der trabalho, tem um plano B: filtre por `Etapa` é "Sprint X" e
troque o nome à mão quando virar a etapa. Funciona igual, dá 10 segundos de trabalho por mês.*

---

## Bloco 3 — 🚧 Travado

**Fonte:** ✅ Tarefas · **Layout:** Tabela · **Filtro:** `Status` é `Bloqueada`
**Mostrar:** Tarefa, Bloqueada por, Responsável, Área

Bloco curto e feio de propósito. Toda linha aqui é dinheiro parado. Se ficar com mais de três
linhas por mais de uma semana, o problema não é execução, é decisão pendente.

---

## Bloco 4 — 🔥 Foco da semana

**Fonte:** ✅ Tarefas · **Layout:** Quadro agrupado por Status
**Filtro:** `Status` é `Fazendo` **ou** `Status` é `Em revisão` **ou** (`Prazo` é esta semana)
**Mostrar:** Responsável, Área, Prazo, Atrasada

O kanban do que está em movimento agora. Máximo saudável: 3 cards em `Fazendo` por pessoa.

---

## Bloco 5 — ➡️ Próximos passos

**Fonte:** ✅ Tarefas · **Layout:** Tabela · **Filtro:** `Status` é `Próxima`
**Ordenar:** Prioridade ↑ · **Limite:** mostre umas 8 linhas
**Mostrar:** Tarefa, Área, Esforço, Prioridade

A fila. Quando alguém terminar algo e perguntar "e agora?", a resposta é a primeira linha daqui.
Manter esta lista curta e ordenada é 80% do trabalho de tocar o projeto.

---

## Bloco 6 — 🧪 O que estamos provando

**Fonte:** 🧪 Hipóteses · **Layout:** Galeria · **Filtro:** `Status` é `Testando` ou `Não testada`
**Mostrar:** Como medir, Meta, Criticidade

A Fase 0 não é uma fase de construir, é uma fase de descobrir. Este bloco existe para você não
esquecer disso no meio de um sprint de código.

---

## Bloco 7 — 📈 Números

**Fonte:** 📈 Métricas · **Layout:** Tabela · **Ordenar:** Data ↓ · **Limite:** 6 linhas

As últimas seis semanas. Lado a lado com o bloco 6: a métrica confirma ou derruba a hipótese.

---

## Bloco 8 — Atalhos

Uma linha de links no rodapé, em colunas:

| | |
|---|---|
| 🧠 Decisões recentes | 📚 Wiki |
| 👥 Rede | 📖 Manual |
| GitHub `felipezanucci/tumtum` | Vercel · Railway · Sentry · PostHog |

---

## Como o painel deve ficar

```
┌──────────────────────────────────────────────────────────┐
│  🎯 SPRINT 6 — LANÇAMENTO BETA                            │
│  Objetivo: 50 pessoas usando em eventos reais em SP       │
│  ███████████░░░░░░░░░  55%      Pendências: 9             │
└──────────────────────────────────────────────────────────┘

⏳ FALTA ISSO PARA FECHAR          🚧 TRAVADO
• Publicar backend no Railway      • Chave API-Football (aguardando cartão)
• Política de privacidade          
• Recrutar 50 beta testers         

🔥 FOCO DA SEMANA                  ➡️ PRÓXIMOS PASSOS
[Fazendo]  [Em revisão]            1. Testes do peak_detection
                                   2. Escolher 3 eventos-piloto

🧪 PROVANDO                        📈 NÚMEROS
H1 · H2 · H4                       6 semanas
```

Se você abrir essa página e em 20 segundos não souber o que fazer hoje, algum filtro está
errado — conserte o filtro, não a sua memória.
