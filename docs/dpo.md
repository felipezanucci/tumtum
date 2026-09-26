# Encarregado pelo tratamento de dados pessoais (DPO)

26/09/2026. Nasceu da auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, itens
E4, E5, L3): não havia encarregado, nem canal, nem registro de pedidos.

## Nomeação

O controlador [PENDENTE — Felipe: razão social e CNPJ] nomeia como
**encarregado pelo tratamento de dados pessoais** (art. 41 da LGPD):

- **Felipe Zanucci**
- **Canal:** oi@tumtum.cc, com o assunto **"Privacidade"**
- [PENDENTE — endereço dedicado privacidade@tumtum.cc]: quando existir, passa
  a ser o canal, e oi@tumtum.cc encaminha para ele. Trocar em todos os lugares
  da lista abaixo no mesmo dia.

A TumTum é hoje um agente de pequeno porte, mas o tratamento é de alto risco
pelo teste cumulativo da Resolução CD/ANPD nº 2/2022 — critério específico,
dado sensível de saúde; critério geral, potencial de afetar significativamente
direitos fundamentais (escrito em `docs/ripd.md` §5). Isso tira a dispensa de
encarregado, e a TumTum nomeia um. **A nomeação abaixo é provisória e está em
conflito de interesse** (seção seguinte).

**Onde a identidade e o canal estão publicados** (art. 41, §1º) — tem de ser
o mesmo texto em todos:

- Política de privacidade (tumtum.cc/privacidade e /en/privacy);
- App: Configurações → Privacidade → contato do encarregado;
- Termo do piloto (`docs/pilot-consent-template.md`);
- Comunicações de incidente (`docs/incident-response.md`).

Assinatura: ______________________ Data: ____/____/________

## Conflito de interesse (Resolução CD/ANPD nº 18/2024)

Acrescentado em 26/09/2026 (noite), pelo parecer v1.1 (§6).

**O problema.** A Resolução 18/2024 permite que o encarregado acumule outras
funções, mas aponta conflito de interesse quando ele também toma as decisões
estratégicas sobre o tratamento. Felipe é quem decide, na TumTum,
**as finalidades** (o que se faz com o batimento), **as categorias** de dado
lidas, **os prazos** de retenção (o de 7 dias foi dele), **os parceiros e
operadores**, e **a arquitetura** (o que sobe, o que fica no celular). O
encarregado existe para fiscalizar exatamente essas decisões e responder por
elas às pessoas e à ANPD. Uma pessoa não fiscaliza de forma independente o que
ela mesma decidiu.

**A posição de hoje.** A nomeação de Felipe é **provisória e declaradamente
conflitada**: é melhor que nenhum encarregado (a pessoa tem a quem escrever, a
ANPD tem com quem falar), e é o que existe numa empresa de uma pessoa. Não é a
solução que o parecer recomenda. Até ela mudar: toda decisão de tratamento que
entrar no `docs/decision-log.md` diz, numa linha, que o encarregado é quem
decidiu; e o jurídico de referência revisa as que mudam finalidade, dado,
destinatário ou prazo.

**A decisão pendente.** `[PENDENTE — Felipe]` Nomear outra pessoa, sem poder
decisório incompatível, ou um **serviço externo de encarregado**. O parecer
prefere uma das duas. Item 80 do `docs/decision-log.md`.

**Critérios para a nomeação definitiva** (parecer §6) — cada um vira uma
linha preenchida antes de assinar:

| Critério | O que precisa existir | Hoje |
|---|---|---|
| **Ato formal** | Documento escrito, datado e assinado de indicação | O bloco de assinatura acima, em branco |
| **Contato público** | Canal claro no app, no site e na política | oi@tumtum.cc, assunto "Privacidade" — [PENDENTE — privacidade@tumtum.cc] |
| **Autonomia** | Acesso à liderança e liberdade para escalar risco sem retaliação | Não se aplica enquanto o encarregado é a liderança — é o conflito |
| **Conflito** | Declaração e avaliação formal **antes** da nomeação, e periodicamente | Esta seção é a declaração; a avaliação formal é do jurídico [PENDENTE] |
| **Substituição** | Pessoa ou serviço substituto nomeado | [PENDENTE — o mesmo suplente de `docs/incident-response.md`] |
| **Recursos** | Tempo, sistemas, apoio jurídico e de segurança | Admin de pedidos, `data_access_log`; sem jurídico contratado [PENDENTE] |
| **Registro** | Atas ou registros das revisões de privacidade | `docs/decision-log.md` e este diretório `docs/` |
| **Treinamento** | Das pessoas e contratados com acesso a dados | [PENDENTE] — hoje, só Felipe; as sessões do Claude Code seguem `CLAUDE.md` e nunca recebem dado real |

## Atribuições

1. Receber reclamações e pedidos dos titulares, responder e tomar as
   providências (art. 18: confirmação, acesso, correção, anonimização ou
   eliminação, portabilidade, informação sobre compartilhamento, revogação
   do consentimento).
2. Receber comunicações da ANPD e responder.
3. Orientar quem trabalha no produto — inclusive as sessões do Claude Code —
   sobre as práticas deste repositório (`CLAUDE.md`, regra de produto de
   26/09; `docs/ropa.md`).
4. Manter `docs/ropa.md`, `docs/ripd.md`, `docs/data-retention-policy.md` e
   `docs/incident-response.md` em dia, e revisar cada finalidade nova antes de
   ela ir ao ar.
5. Conduzir o incidente (`docs/incident-response.md`).

## Prazo de resposta

**15 dias** a partir do pedido. O prazo legal de 15 dias do art. 19, II é o
da declaração completa de confirmação e acesso; para os outros direitos a
TumTum **adota** os mesmos 15 dias como SLA interno, conservador — não é um
prazo legal uniforme, e não deve ser descrito assim (parecer §7.1). O servidor
grava `due_at = opened_at + 15 dias` em cada pedido. Confirmação simples de que
tratamos dados da pessoa: na hora, pelo próprio app (Perfil → "Baixar meus
dados").

## Onde os pedidos ficam registrados

Na tabela **`data_subject_requests`** do servidor: tipo (`access`,
`portability`, `correction`, `deletion`, `revocation`, `other`), mensagem,
`status` (`open` → `answered` → `closed`), `opened_at`, `due_at`,
`answered_at`, resposta.

- **A pessoa abre** pelo site (tumtum.cc/perfil → Pedidos,
  `POST /api/users/me/requests`) e acompanha na mesma tela.
- **O encarregado lê e responde** pelo admin (`GET /api/admin/requests`,
  `PATCH /api/admin/requests/{id}` com `status` e `answer`). Cada leitura
  fica no `data_access_log`.
- **Pedido que chega por e-mail:** responder no mesmo dia que foi recebido;
  anotar na planilha de pedidos (fora do repositório) com a data de chegada,
  porque o prazo conta dela; e, se a pessoa tem conta, pedir que registre
  também em Perfil → Pedidos. [PENDENTE — um endpoint de admin para
  registrar em nome da pessoa um pedido que chegou por e-mail.]
- **Antes de responder**, confirmar que quem pede é o titular: pedido feito
  logado na conta, ou resposta vinda do e-mail da conta. Nunca mandar dado de
  saúde para um endereço diferente do da conta.

Os pedidos saem com a conta (a exclusão apaga as linhas em que a pessoa é o
titular). O `deletion_log` guarda só que uma exclusão aconteceu.

## Rotina

- Toda segunda-feira: abrir `GET /api/admin/requests`, ver os `open` e o
  `due_at` de cada um.
- A cada seis meses: revisar o `docs/ripd.md` (próxima: 26/03/2027) e fazer o
  ensaio de mesa de `docs/incident-response.md` §9.
- A cada incidente, suspeita ou não: uma linha em `docs/incident-register.md`.
