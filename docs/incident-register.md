# Registro de incidentes de segurança

Criado em 26/09/2026 (noite), pelo parecer v1.1 (§12) e pela Resolução
CD/ANPD nº 15/2024: **todo incidente de segurança com dado pessoal é
registrado, comunicado ou não, e o registro é guardado por no mínimo 5 anos**,
com a justificativa da avaliação de risco. O como está em
`docs/incident-response.md`; aqui fica só o registro.

**Regras deste arquivo:**

- **Uma linha por incidente**, a partir da ciência. Suspeita descartada
  (nível 0) também entra — é a linha que prova que se olhou.
- **O repositório é público.** Nada que identifique pessoa: categorias e
  quantidades, nunca nome, e-mail, id de conta ou IP. Lista de afetados,
  protocolos, e-mails enviados e evidências ficam na pasta fora do repositório
  [PENDENTE — Felipe: onde], pelo número INC-AAAA-NN.
- **Nenhum detalhe explorável antes da correção estar no ar.** Enquanto
  estiver em contenção, a descrição diz só a classe ("acesso indevido a
  noites de outras contas") e o status; completa-se depois.
- **Nada se apaga.** Uma linha errada é corrigida com uma nota datada na
  própria linha. Guardar por 5 anos a partir do encerramento.
- A coluna **avaliação de risco** responde aos critérios da Resolução 15/2024:
  o incidente pode afetar significativamente interesses e direitos
  fundamentais? Envolve dado sensível, de criança/adolescente/idoso,
  financeiro, de autenticação, sob sigilo, ou larga escala? Daí o nível
  (0, 1, 2 — `docs/incident-response.md` §3) e **por quê**.
- Cada incidente também ganha uma entrada no `docs/decision-log.md`, sem
  dado pessoal: o que falhou, o que custou, o que mudou.

## Registro

| Nº | Data de detecção (ciência) | Descrição | Dados e titulares afetados | Avaliação de risco (critérios da Res. 15/2024) | Comunicado à ANPD (sim/não, data, justificativa) | Comunicado aos titulares (sim/não, data) | Contenção | Causa raiz | Correção | Responsável | Encerrado em | Revisão do RIPD |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| INC-2026-00 — **exemplo — apagar** | 01/10/2026 14h10, avisado pelo operador do piloto | Celular emprestado do piloto esquecido num bar por cerca de 2 h, depois devolvido; tela bloqueada | Noites de 2 participantes no aparelho (batimento, momentos); nenhuma conta aberta no servidor por ele | Dado sensível: sim. Afeta significativamente direitos? **Não**: aparelho bloqueado, banco em SQLCipher, sem sinal de acesso, devolvido. Nível 1 | **Não.** Justificativa: dado criptografado e ininteligível sem a chave do aparelho; sem indício de acesso; exposição de 2 h | Não (os 2 foram avisados por cortesia, 01/10) | Sessões do aparelho revogadas no servidor, 01/10 14h40 | Celular fora do estojo do operador | Checklist do operador: celular no estojo entre usos | Felipe | 01/10/2026 | Não exigida (nível 1); nota em `docs/ripd.md` D1 |
