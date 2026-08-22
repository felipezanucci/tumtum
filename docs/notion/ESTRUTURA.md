# Estrutura das databases

Especificação de cada banco de dados: propriedades, tipos, opções e visões.
Use como referência ao converter as colunas depois do import dos CSVs.

---

## 🎯 Etapas

As fases do projeto. Uma linha por sprint ou marco. É a espinha dorsal — tudo pendura aqui.

| Propriedade | Tipo | Opções / observação |
|---|---|---|
| Nome | Título | ex.: "Sprint 4 — Motor de conteúdo" |
| Status | Seleção | `Planejada` · `Em andamento` · `Em validação` · `Concluída` · `Pausada` |
| Ativa | Caixa de seleção | **marque em uma única etapa por vez** — é o que o painel filtra |
| Objetivo | Texto | uma frase: o que muda no produto quando essa etapa fecha |
| Período | Data (intervalo) | semanas previstas |
| Tarefas | Relação → ✅ Tarefas | criada automaticamente ao relacionar do outro lado |
| Progresso | Rollup (Tarefas → Feita, % marcado) | mostrar como **Barra** |
| Pendências | Rollup (Tarefas → Feita, contar não marcados) | |
| Total | Rollup (Tarefas → Feita, contar tudo) | |
| Critério de pronto | Texto | a régua objetiva: "quando X acontecer, a etapa fechou" |
| Risco | Seleção | `Baixo` · `Médio` · `Alto` |

**Corpo de cada página de etapa** (use o template `templates/etapa.md`): checklist do critério de
pronto, links das decisões tomadas na etapa e a retrospectiva no fim.

**Views:**
- `Linha do tempo` (Timeline, por Período) — visão padrão
- `Quadro` (Board por Status)
- `Só a atual` (filtro: Ativa = marcada)

---

## ✅ Tarefas

O motor. Se alguém precisa fazer alguma coisa, é uma linha aqui — não importa se é código,
contrato, ligação ou post no Instagram.

| Propriedade | Tipo | Opções / observação |
|---|---|---|
| Tarefa | Título | comece com verbo: "Publicar backend no Railway" |
| Status | Seleção | `Backlog` · `Próxima` · `Fazendo` · `Em revisão` · `Bloqueada` · `Concluída` |
| Feita | Fórmula | `prop("Status") == "Concluída"` |
| Etapa | Relação → 🎯 Etapas | **obrigatório**; sem isso a tarefa some do painel |
| Área | Seleção | `Produto` · `Frontend` · `Backend` · `Infra` · `Design` · `Growth` · `Legal` · `Parcerias` · `Operação` |
| Tipo | Seleção | `Feature` · `Bug` · `Infra` · `Pesquisa` · `Conteúdo` · `Legal` · `Parceria` · `Design` |
| Prioridade | Seleção | `P0 — trava tudo` · `P1 — desta semana` · `P2 — deste mês` · `P3 — algum dia` |
| Responsável | Pessoa | |
| Prazo | Data | só preencha quando existir data real; prazo falso vira ruído |
| Esforço | Seleção | `P` (até 2h) · `M` (até 1 dia) · `G` (vários dias) |
| Bloqueada por | Relação → ✅ Tarefas (ela mesma) | o que precisa acontecer antes |
| Link | URL | PR do GitHub, Figma, contrato, planilha |
| Decisão | Relação → 🧠 Decisões | quando a tarefa nasceu de uma decisão |
| Doc | Relação → 📚 Wiki | a especificação ou pesquisa por trás |
| Etapa ativa | Rollup (Etapa → Ativa, mostrar original) | truque para o painel filtrar "pendências da etapa atual" |
| Atrasada | Fórmula | ver abaixo |

Fórmula de `Atrasada`:

```
if(empty(prop("Prazo")), "", if(prop("Feita"), "", if(prop("Prazo") < now(), "🔴 atrasada", if(dateBetween(prop("Prazo"), now(), "days") <= 2, "🟡 vence já", ""))))
```

**Views:**
- `Quadro` (Board por Status, agrupando `Concluída` no fim) — visão padrão
- `Esta semana` (filtro: Prazo é esta semana **ou** Status = Fazendo)
- `Bloqueadas` (filtro: Status = Bloqueada)
- `Por área` (Board por Área)
- `Calendário` (por Prazo)

---

## 🧠 Decisões

O registro de *por que* as coisas são do jeito que são. Evita re-discutir o mesmo assunto e
serve de onboarding para quem entrar depois. Uma linha por decisão que custou mais de 10 minutos
de conversa.

| Propriedade | Tipo | Opções |
|---|---|---|
| Decisão | Título | escreva como afirmação: "PWA primeiro, app nativo só se necessário" |
| Status | Seleção | `Proposta` · `Decidida` · `Revisada` · `Revertida` |
| Data | Data | |
| Área | Seleção | mesma lista de Tarefas |
| Impacto | Seleção | `Alto` · `Médio` · `Baixo` |
| Etapa | Relação → 🎯 Etapas | |

**Corpo da página** (template `templates/decisao.md`): contexto, opções consideradas, decisão,
consequências, quando revisitar.

**Views:** `Cronológica` (por Data, mais recente primeiro) · `Por área` · `A revisitar`.

---

## 📚 Wiki

O conhecimento que não é tarefa nem decisão: pesquisa de mercado, especificação de feature,
manual de operação de evento, análise de API, roteiro de card, guia de marca.

| Propriedade | Tipo | Opções |
|---|---|---|
| Documento | Título | |
| Área | Seleção | mesma lista de Tarefas |
| Tipo | Seleção | `Especificação` · `Pesquisa` · `Processo` · `Referência` · `Contrato` · `Marca` |
| Estado | Seleção | `Rascunho` · `Em revisão` · `Vigente` · `Obsoleto` |
| Dono | Pessoa | |
| Atualizado em | Última edição | automático |

**Views:** `Por área` (Board) · `Vigentes` · `Precisa revisar` (filtro: Atualizado em > 90 dias).

---

## 🧪 Hipóteses

A Fase 0 existe para responder perguntas, não para entregar código. Este banco guarda as
perguntas e o veredicto de cada uma. É o que diz se a TumTum continua ou muda de rumo.

| Propriedade | Tipo | Opções |
|---|---|---|
| Hipótese | Título | "As pessoas querem compartilhar a curva de batimentos" |
| Status | Seleção | `Não testada` · `Testando` · `Confirmada` · `Refutada` · `Inconclusiva` |
| Criticidade | Seleção | `Mata o negócio` · `Muda o produto` · `Ajusta detalhe` |
| Como medir | Texto | o sinal concreto: "≥30% dos beta testers geram um card" |
| Meta | Texto | o número que separa sim de não |
| Resultado | Texto | preencher ao fechar |
| Etapa | Relação → 🎯 Etapas | |

**Views:** `Painel` (Board por Status) · `Críticas primeiro` (por Criticidade).

---

## 📈 Métricas

Uma linha por semana. Preenchida na sexta, em 10 minutos. Poucas colunas de propósito: métrica
que ninguém olha é trabalho jogado fora.

| Propriedade | Tipo |
|---|---|
| Semana | Título (ex.: "2026-S34") |
| Data | Data |
| Usuários cadastrados | Número |
| Wearables conectados | Número |
| Sessões de evento | Número |
| Cards gerados | Número |
| Cards compartilhados | Número |
| Taxa de compartilhamento | Fórmula: `prop("Cards compartilhados") / prop("Cards gerados")` (mostrar como %) |
| Nota | Texto (o que explica o número desta semana) |

**Views:** `Tabela` (mais recente no topo) · `Gráfico` — a TumTum é um produto de curva; ver a
própria curva de crescimento em linha ajuda mais do que parece.

---

## 👥 Rede

Pessoas e organizações. Beta testers, venues, produtoras, artistas, advogado, contador,
investidores.

| Propriedade | Tipo | Opções |
|---|---|---|
| Nome | Título | |
| Categoria | Seleção | `Beta tester` · `Venue` · `Produtora` · `Artista` · `Fornecedor` · `Investidor` · `Mentor` |
| Contato | Texto | |
| Wearable | Seleção | `Apple Watch` · `Galaxy Watch` · `Garmin` · `Fitbit` · `Nenhum` — só para beta testers |
| Cidade | Texto | |
| Situação | Seleção | `A abordar` · `Conversando` · `Ativo` · `Frio` |
| Próximo passo | Texto | |
| Notas | Texto | |

**Views:** `Beta testers` · `Parcerias` (Board por Situação) · `A abordar`.

---

## Por que essas sete e não mais

A tentação em Notion é criar uma database por assunto. Isso mata o sistema: em duas semanas
ninguém lembra onde as coisas ficam. Sete cobre tudo que a TumTum faz na Fase 0, e cada uma
responde uma pergunta diferente:

| Pergunta | Database |
|---|---|
| Onde estamos? | 🎯 Etapas |
| O que fazer agora? | ✅ Tarefas |
| Por que é assim? | 🧠 Decisões |
| Como funciona? | 📚 Wiki |
| Está dando certo? | 🧪 Hipóteses + 📈 Métricas |
| Com quem falamos? | 👥 Rede |

Antes de criar uma oitava, pergunte qual dessas perguntas ela responde melhor. Quase sempre a
resposta é "nenhuma — isso é uma view".
