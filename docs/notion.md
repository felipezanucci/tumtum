# TumTum HQ — o second brain no Notion

O workspace está montado e é a fonte de verdade do dia a dia. Este arquivo é só a
referência versionada: o que existe lá, como está configurado e o que fazer se
precisar reconstruir.

| | |
|---|---|
| **Página raiz** | [🫀 TumTum HQ](https://app.notion.com/p/3c4cb8f196238184a9c5eec93ffa8dbd) |
| **Uso diário** | [🎛️ Painel de Controle](https://app.notion.com/p/3c4cb8f19623819c88fce906ba282226) |
| **Estratégia de mercado** | [🚀 GTM — Go to Market](https://app.notion.com/p/3c4cb8f1962381e0a4fdda20ae0ba086) |
| **Rituais e templates** | [📖 Manual](https://app.notion.com/p/3c4cb8f1962381b28a13c92b95d0364c) |

## As três regras

1. **Uma informação, um lugar.** Cada coisa mora em um único banco. Todo o resto são
   visões filtradas do mesmo dado — nunca copie e cole, relacione.
2. **Toda tarefa pertence a uma etapa.** É isso que faz o progresso e as pendências
   aparecerem sozinhos.
3. **Se não está lá, não existe.** Decisão de WhatsApp vira card em Decisões no mesmo dia.

---

## Os sete bancos

Cada um responde uma pergunta diferente. Antes de criar um oitavo, pergunte qual
dessas ele responde melhor — quase sempre a resposta é "nenhuma, isso é uma view".

| Pergunta | Banco | ID |
|---|---|---|
| Onde estamos? | 🎯 Etapas | `43eae1ed1ca54e7399167eb192db752a` |
| O que fazer agora? | ✅ Tarefas | `686f81f9a5f54ccdbf72b7d24f523d3f` |
| Por que é assim? | 🧠 Decisões | `5085f206575c47e5be7ffc8814c96ed9` |
| Como funciona? | 📚 Wiki | `5f020ae01e9a49e19929b965c0405c62` |
| Está dando certo? | 🧪 Hipóteses | `be3cc085de9c4f2ba474af67dad2d5f9` |
| Está dando certo? | 📈 Métricas | `94c031ef5c8b491baed0e96c9ef7c66a` |
| Com quem falamos? | 👥 Rede | `7284279a3b0f4e50874a30d86db3faa6` |

### 🎯 Etapas

`Nome` (título) · `Status` (Planejada / Em andamento / Em validação / Concluída / Pausada) ·
`Ativa` (caixa — **uma etapa por vez**) · `Objetivo` · `Período` (data) ·
`Critério de pronto` · `Risco` (Baixo / Médio / Alto) ·
`Tarefas` `Decisões` `Hipóteses` (relações) ·
`Progresso` `Pendências` `Total` (rollups).

### ✅ Tarefas

`Tarefa` (título) · `Status` (Backlog / Próxima / Fazendo / Em revisão / Bloqueada / Concluída) ·
`Etapa` (relação, obrigatória) · `Área` (Produto, Frontend, Backend, Infra, Design, Growth,
Legal, Parcerias, Operação) · `Tipo` (Feature, Bug, Infra, Pesquisa, Conteúdo, Legal,
Parceria, Design) · `Prioridade` (P0–P3) · `Responsável` · `Prazo` · `Esforço` (P/M/G) ·
`Link` · `Notas` · `Bloqueada por` ⇄ `Bloqueia` (auto-relação) · `Decisão` e `Doc` (relações).

### 🧠 Decisões
`Decisão` · `Status` (Proposta / Decidida / Revisada / Revertida) · `Data` · `Área` ·
`Impacto` · `Etapa` · `Resumo`.

### 📚 Wiki
`Documento` · `Área` · `Tipo` (Especificação, Pesquisa, Processo, Referência, Contrato, Marca) ·
`Estado` (Rascunho / Em revisão / Vigente / Obsoleto) · `Dono` · `Atualizado em` · `Resumo`.

### 🧪 Hipóteses
`Hipótese` · `Status` (Não testada / Testando / Confirmada / Refutada / Inconclusiva) ·
`Criticidade` (Mata o negócio / Muda o produto / Ajusta detalhe) · `Como medir` · `Meta` ·
`Resultado` · `Etapa`.

### 📈 Métricas
`Semana` · `Data` · `Usuários cadastrados` · `Wearables conectados` · `Sessões de evento` ·
`Cards gerados` · `Cards compartilhados` · `Taxa de compartilhamento` (fórmula) · `Nota`.

### 👥 Rede
`Nome` · `Categoria` (Beta tester, Venue, Produtora, Artista, Fornecedor, Investidor, Mentor) ·
`Contato` · `Wearable` · `Cidade` · `Situação` · `Próximo passo` · `Notas`.

---

## O que torna o status automático

Duas peças, e nenhuma planilha:

**Em Tarefas**, a fórmula `Feita`:

```
prop("Status") == "Concluída"
```

**Em Etapas**, três rollups sobre a relação `Tarefas`, todos mirando `Feita`:

| Propriedade | Cálculo |
|---|---|
| `Progresso` | percentual marcado (exibir como **Barra**) |
| `Pendências` | contar não marcados |
| `Total` | contar tudo |

Mover um card para `Concluída` move a barra da etapa e tira a linha das pendências.

Tarefas também tem `Atrasada`:

```
if(empty(prop("Prazo")), "", if(prop("Feita"), "", if(prop("Prazo") < now(), "🔴 atrasada", if(dateBetween(prop("Prazo"), now(), "days") <= 2, "🟡 vence já", ""))))
```

---

## Os blocos do painel

Todos são visões vinculadas — o painel não guarda nada.

| Bloco | Fonte | Layout | Filtro |
|---|---|---|---|
| 🎯 Onde estamos | Etapas | galeria | `Ativa` marcada |
| ⏳ Falta isso para fechar a etapa | Tarefas | tabela | `Etapa` = sprint atual **e** `Status` ≠ Concluída |
| 🚧 Travado | Tarefas | tabela | `Status` = Bloqueada |
| 🔥 Foco da semana | Tarefas | quadro por Status | `Status` em (Fazendo, Em revisão) |
| ➡️ Próximos passos | Tarefas | tabela | `Status` = Próxima, ordenado por Prioridade |
| 🧪 O que estamos provando | Hipóteses | tabela | `Status` em (Não testada, Testando) |
| 📈 Números | Métricas | tabela | ordenado por Data ↓ |

## Duas peculiaridades conhecidas

**O filtro da etapa atual é manual.** O Notion não aceita filtrar por um rollup de caixa
de seleção, então o bloco *"Falta isso para fechar a etapa"* aponta direto para a etapa
em curso em vez de seguir o `Ativa` sozinho. Ao virar de sprint: desmarque `Ativa` na
etapa que fechou, marque na próxima, e troque a etapa naquele filtro.

**A barra de progresso é uma opção de exibição.** `Progresso` → *Mostrar como* → **Barra**.
Só dá para configurar pela interface.

---

## 🚀 GTM

Página de trabalho da estratégia de entrada no mercado, com as sete perguntas que a
estratégia precisa responder e duas visões vinculadas: tarefas de Growth e Parcerias em
aberto, e as hipóteses por criticidade. O que sai de lá se espalha pelos bancos — decisão
vira card em Decisões, ação vira tarefa, aposta vira hipótese. Na página fica só o
raciocínio.

---

## Rituais

Quarenta minutos por semana. O detalhe está no [Manual](https://app.notion.com/p/3c4cb8f1962381b28a13c92b95d0364c),
dentro do Notion.

- **Segunda, 15 min** — destravar o que está travado, escolher de 3 a 5 tarefas da semana.
- **Sexta, 10 min** — fechar os números e escrever a frase que os explica.
- **Fim de etapa, 30 min** — conferir o critério de pronto, retrospectiva, virar a etapa ativa.

---

## Manutenção deste arquivo

Este documento e o workspace precisam contar a mesma história. Mudou o esquema no Notion,
atualize aqui — é o que permite auditar ou reconstruir o second brain sem depender da
memória de ninguém.
