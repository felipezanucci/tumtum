# Rituais

Um second brain morre por falta de manutenção, não por falta de estrutura. São três hábitos —
40 minutos por semana no total. Se você só puder manter um, mantenha o de segunda.

---

## Segunda, 15 min — Planejar a semana

1. Abra o **Painel de Controle**.
2. Bloco `🚧 Travado`: para cada linha, decida quem destrava e quando. Bloqueio sem dono não
   sai do lugar.
3. Bloco `➡️ Próximos passos`: escolha de 3 a 5 tarefas e mova para `Fazendo`, com prazo.
4. Bloco `⏳ Falta isso`: a etapa fecha nesta semana? Se não, o que atrapalha?
5. Crie a página da semana com `templates/semana.md` dentro da Wiki.

**A pergunta da segunda:** *se eu só conseguir fazer uma coisa nesta semana, qual delas mais
aproxima a TumTum de provar a hipótese da vez?* Essa tarefa entra em `Fazendo` primeiro.

---

## Sexta, 10 min — Fechar os números

1. Nova linha em **📈 Métricas** (template `templates/semana.md` já traz os campos).
2. Preencha os números do PostHog. Se ainda não tem PostHog, conte na mão — número contado
   na mão é melhor que número não contado.
3. Uma frase no campo `Nota` explicando o que aconteceu. Daqui a três meses essa frase vale
   mais que o número.
4. Olhe 🧪 **Hipóteses**: alguma passou de `Testando` para `Confirmada` ou `Refutada`?

---

## Fim de cada etapa, 30 min — Retrospectiva

1. Confira o `Critério de pronto` da etapa. Cumprido de verdade? Se não, a etapa não acabou.
2. Preencha a retrospectiva no corpo da página da etapa (`templates/etapa.md`): o que funcionou,
   o que travou, o que faríamos diferente.
3. Registre em **🧠 Decisões** tudo que foi decidido no caminho e ainda não está lá.
4. Desmarque `Ativa` na etapa que fechou, marque na próxima. O painel inteiro vira de página.

---

## Regras de higiene

**Captura em 30 segundos.** Ideia, pedido ou problema vira tarefa em `Backlog` na hora, com
título e área só. Refinar depois é fácil; lembrar depois é impossível.

**Toda tarefa tem etapa.** Se não couber em nenhuma etapa atual, ela é `Backlog` de uma etapa
futura — ou não deveria existir agora.

**Decisão relevante vira card no mesmo dia.** O teste: "isso vai ser perguntado de novo em dois
meses?" Se sim, é uma Decisão.

**Nada de tarefa vaga.** "Melhorar o card" não é tarefa. "Aumentar contraste do BPM no card solo
para passar em AA" é.

**Arquive, não delete.** Tarefa cancelada vira `Concluída` com uma nota, ou vai para uma view
`Arquivo`. O histórico do que você decidiu não fazer é metade do aprendizado.

---

## Cadência sugerida da Fase 0

| Quando | O quê |
|---|---|
| Toda segunda | Planejamento (15 min) |
| Toda sexta | Métricas (10 min) |
| A cada 2 semanas | Fim de sprint + retrô (30 min) |
| A cada evento-piloto | Página de evento na Wiki, no dia seguinte, enquanto está fresco |
| A cada 6 semanas | Revisar 🧪 Hipóteses: a Fase 0 já respondeu? Segue, pivota ou para? |
