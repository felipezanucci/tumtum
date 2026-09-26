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

A TumTum é hoje um agente de pequeno porte, mas trata dado sensível (saúde)
com tecnologia nova, o que pode caracterizar tratamento de alto risco pela
Resolução CD/ANPD nº 2/2022 e tirar a dispensa de encarregado. Em vez de
apostar na dispensa, nomeia um.

**Onde a identidade e o canal estão publicados** (art. 41, §1º) — tem de ser
o mesmo texto em todos:

- Política de privacidade (tumtum.cc/privacidade e /en/privacy);
- App: Configurações → Privacidade → contato do encarregado;
- Termo do piloto (`docs/pilot-consent-template.md`);
- Comunicações de incidente (`docs/incident-response.md`).

Assinatura: ______________________ Data: ____/____/________

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

**15 dias** a partir do pedido (art. 19, II). O servidor grava
`due_at = opened_at + 15 dias` em cada pedido. Confirmação simples de que
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
- A cada seis meses: revisar o `docs/ripd.md` (próxima: 26/03/2027).
