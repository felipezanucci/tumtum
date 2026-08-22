# TumTum HQ — Second Brain no Notion

Guia de montagem do workspace da TumTum no Notion. Tempo estimado: **40 a 60 minutos**.

> Conteúdo em português porque é material de trabalho do time, não documentação de código.

## A ideia em uma frase

Uma página raiz (**TumTum HQ**) com um **Painel de Controle** na frente e **7 bancos de dados**
atrás. Você nunca navega por pastas: você abre o painel e ele te responde três perguntas —
*como está a etapa atual*, *o que está travado* e *qual é o próximo passo*.

## As 3 regras que fazem isso funcionar

1. **Uma informação, um lugar.** Cada coisa mora em um único banco de dados. Todo o resto são
   *views* (visões filtradas) do mesmo dado. Nunca copie e cole — relacione.
2. **Toda tarefa pertence a uma Etapa.** É isso que faz o "% concluído" e a lista de pendências
   aparecerem sozinhos, sem ninguém atualizar planilha.
3. **Se não está no Notion, não existe.** Decisão tomada no WhatsApp vira card em Decisões no
   mesmo dia, ou ela será re-discutida em três semanas.

## Passo a passo da montagem

### 1. Crie a página raiz

No Notion: `+ Nova página` → título **🫀 TumTum HQ** → ícone 🫀 → capa escura.
Essa página fica no topo da barra lateral e é a única que você precisa favoritar.

### 2. Importe os bancos de dados prontos

Os arquivos em `seed/` já vêm com o conteúdo real do projeto (etapas, tarefas pendentes,
decisões já tomadas, hipóteses a validar). Para cada arquivo:

1. Dentro de **TumTum HQ**, digite `/importar` → **Importar** → **CSV**.
2. Selecione o arquivo (`seed/01-etapas.csv`, depois `02-tarefas.csv`, e assim por diante).
3. O Notion cria a database com todas as colunas como texto — isso é normal.

Importe **nesta ordem**, porque as relações dependem disso:

| Ordem | Arquivo | Vira a database |
|---|---|---|
| 1 | `seed/01-etapas.csv` | 🎯 Etapas |
| 2 | `seed/02-tarefas.csv` | ✅ Tarefas |
| 3 | `seed/03-decisoes.csv` | 🧠 Decisões |
| 4 | `seed/04-wiki.csv` | 📚 Wiki |
| 5 | `seed/05-hipoteses.csv` | 🧪 Hipóteses |
| 6 | `seed/06-metricas.csv` | 📈 Métricas |
| 7 | `seed/07-rede.csv` | 👥 Rede |

### 3. Converta os tipos de coluna

Depois de importar, ajuste o tipo de cada propriedade conforme a especificação em
[`ESTRUTURA.md`](./ESTRUTURA.md). Clique no cabeçalho da coluna → `Editar propriedade` → `Tipo`.

Prioridade (faça nesta ordem, o resto pode esperar):

1. `Status`, `Área`, `Tipo`, `Prioridade` → **Seleção** (Select).
2. `Etapa` em Tarefas → **Relação** apontando para 🎯 Etapas. Depois de criar a relação, o texto
   antigo não vira link sozinho: use a coluna de texto importada como referência e arraste cada
   tarefa para a etapa certa (são poucos minutos, e você aproveita para revisar o backlog).
   Quando terminar, apague a coluna de texto.
3. `Feita` em Tarefas → **Fórmula** (ver abaixo). É ela que alimenta a barra de progresso.
4. `Prazo`, `Data` → **Data**.

### 4. Cole as duas fórmulas que fazem o painel funcionar

**Em ✅ Tarefas**, crie a propriedade `Feita` (tipo Fórmula):

```
prop("Status") == "Concluída"
```

**Em 🎯 Etapas**, crie três propriedades do tipo **Rollup**, todas relacionadas a Tarefas
pela propriedade `Tarefas`:

| Nome | Propriedade | Cálculo | O que mostra |
|---|---|---|---|
| `Progresso` | Feita | Percentual marcado | barra de % da etapa |
| `Pendências` | Feita | Contar não marcados | quantas tarefas faltam |
| `Total` | Feita | Contar tudo | tamanho da etapa |

Em `Progresso`, clique no nome → `Mostrar como` → **Barra**. É o número que você vai olhar todo dia.

### 5. Monte o Painel de Controle

Crie uma subpágina de TumTum HQ chamada **🎛️ Painel de Controle** e siga
[`PAINEL.md`](./PAINEL.md) — é o layout bloco a bloco, com os filtros de cada visão.

### 6. Importe as páginas de apoio

Arraste `ESTRUTURA.md`, `PAINEL.md`, `RITUAIS.md` e a pasta `templates/` para dentro do Notion
(`/importar` → **Markdown**). Elas viram páginas de manual dentro do HQ.

### 7. Ligue o GitHub

Configure `Configurações → Conexões → GitHub` no Notion. Depois, ao colar um link de PR na
propriedade `Link` de uma tarefa, o Notion mostra título e status do PR direto no card — é assim
que a parte técnica aparece no painel sem ninguém precisar traduzir nada.

## O que fica pronto no final

```
🫀 TumTum HQ
├── 🎛️ Painel de Controle        ← a única página que você abre no dia a dia
├── 🎯 Etapas                     ← as fases do projeto, com % e pendências automáticas
├── ✅ Tarefas                    ← o motor: tudo que alguém precisa fazer
├── 🧠 Decisões                   ← por que fizemos as coisas do jeito que fizemos
├── 📚 Wiki                       ← o conhecimento que não é tarefa (pesquisa, specs, contratos)
├── 🧪 Hipóteses                  ← o que a Fase 0 precisa provar
├── 📈 Métricas                   ← os números da semana
├── 👥 Rede                       ← pessoas: beta testers, venues, artistas, fornecedores
└── 📖 Manual                     ← ESTRUTURA, PAINEL, RITUAIS, templates
```

## Se você só tiver 15 minutos hoje

Importe `01-etapas.csv` e `02-tarefas.csv`, crie a fórmula `Feita`, crie o rollup `Progresso`
e monte só o primeiro bloco do painel. O resto pode entrar na semana seguinte — a estrutura
foi desenhada para crescer sem precisar refazer nada.
