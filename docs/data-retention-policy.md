# Política de retenção de dados

26/09/2026. Nasceu da auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, itens
D1, D2, L4): havia três prazos escritos e nenhum job que cumprisse algum.
Agora cada categoria tem prazo, e quem o cumpre está na última coluna.

**Princípio:** um dado fica o tempo que a finalidade dele precisa, e nem um
dia a mais (art. 15 e 16 da LGPD). Quando a pessoa apaga a conta, sai tudo o
que é dela, exceto o que a lei manda guardar — e isso está dito abaixo.

Os números vivem no código como constantes ou variáveis de ambiente; mude lá
e aqui juntos, e registre no `docs/decision-log.md`.

**Revisto em 26/09 (noite)** pelo parecer v1.1 (§11): cada prazo tem de ser
justificado pela necessidade real, não por um número redondo. A série bruta
caiu de 30 para 7 dias; o ledger de consentimento, o registro de incidentes e
os backups ganharam regra própria.

## Servidor

| Categoria | Prazo | Quem apaga |
|---|---|---|
| **Série bruta** (`hr_data`) | **7 dias** depois de a noite ser analisada (`hr_sessions.analyzed_at`). `RAW_READINGS_RETENTION_DAYS`, padrão 7. **Por que 7:** os momentos são encontrados na chegada da noite, em segundos; depois disso a série só serve para reanalisar a noite se o detector for corrigido ("Procurar meus momentos") e para a pessoa — e, no piloto, a equipe — conferir a noite na semana seguinte. Sete dias cobrem os dois. Os 30 anteriores não tinham necessidade que os justificasse | Ciclo diário `services/maintenance.py` |
| **Noite resumida** (`hr_sessions`: início, fim, média/máx/mín, qualidade), **momentos** (`peaks`), **cards** | Enquanto a conta existir e o consentimento `keep_night` estiver ativo. Revogado o `keep_night`, as noites guardadas saem do servidor **em até 24 horas** (carência de 23 h e um ciclo de 1 h — desde 28/09; antes, um ciclo diário depois de 24 h de carência podia levar 48). A pessoa apaga uma noite quando quiser (`DELETE /api/health/sessions/{id}`) | A pessoa; exclusão da conta; **revogar `keep_night`**: as noites saem 24 h depois, se não for concedido de novo (`REVOCATION_GRACE`, ciclo diário) |
| **Imagem do card em cache** (Redis `card:image:{id}`, `:og`) | **7 dias**; sai na hora com o card ou a conta | TTL do Redis; `DELETE /api/cards/{id}`; exclusão da conta |
| **Card público** | Só enquanto publicado (`published_at`); despublicar tira do ar | A pessoa |
| **Posts do feed**, reações, vínculo com turnê | Até a pessoa apagar o post ou a conta. Post apagado fica marcado com `deleted_at` [PENDENTE — remoção física] | A pessoa; exclusão da conta |
| **Denúncias e bloqueios** | Enquanto a conta de quem denunciou/bloqueou existir | Exclusão da conta |
| **Conta** (nome, e-mail, hash de senha, data de nascimento) | Enquanto a conta existir | Exclusão da conta (`POST /api/users/me/delete`) |
| **Consentimentos** (`consents`, o ledger) | **Hoje:** enquanto a conta existir (histórico completo, só acrescenta). **Alvo (parecer §4.2, §11):** o **prazo probatório** do próprio registro — o tempo em que a TumTum pode precisar provar que um consentimento foi dado (art. 8º, §2º) —, **separado do dado de saúde**: depois da exclusão da conta ficam só finalidade, base, versão do texto, prova e datas, ligados a um identificador que não leva a mais nada. O ledger não autoriza guardar batimento. [PENDENTE — jurídico: prazo; item 82] | Exclusão da conta (hoje) |
| **Pedidos de titular** (`data_subject_requests`) | Enquanto a conta existir | Exclusão da conta |
| **Quem leu dado de saúde** (`data_access_log`) | Enquanto a conta do titular existir [PENDENTE — prazo máximo; sugestão 180 dias] | Exclusão da conta |
| **Registro de acesso** (`access_log`: IP, rota, status, hora, conta) | **180 dias** — guarda obrigatória de 6 meses (Marco Civil, art. 15). **Não sai com a conta** | Ciclo de hora em hora (`services/maintenance.py`) |
| **Prova de exclusão** (`deletion_log`) | Indefinida. Só a data, nenhum identificador | — |
| **Refresh tokens** | 90 dias do último uso; linhas expiradas saem 24 h depois | Ciclo de hora em hora (`services/maintenance.py`) |
| **Códigos**: cadastro (`signup_codes`), troca de e-mail (`email_changes`), senha (`password_reset_tokens`) | Até 24 h depois de expirar; cadastro não confirmado sai em 1 dia | Ciclo de hora em hora; exclusão da conta (por e-mail) |
| **Lista de espera** (`waitlist_entries`: e-mail, nome, sobrenome, origem) | Até a pessoa pedir para sair; sai também com a conta do mesmo e-mail | Pedido (oi@tumtum.cc); exclusão da conta |
| **Erros no Sentry** | Retenção do plano: [PENDENTE — conferir no Sentry]. Sem PII nem corpo de requisição | Sentry |
| **E-mails enviados (Resend)** | Retenção do log do Resend: [PENDENTE — conferir] | Resend |
| **Backups do banco** | [PENDENTE — prazo do Railway] (`docs/backups.md`). **P1, antes do lançamento público: TTL curto e documentado + tombstone de exclusão** (abaixo) | Railway |
| **Registro de incidentes** (`docs/incident-register.md` e a pasta fora do repositório) | **No mínimo 5 anos** a partir do encerramento, **inclusive os não comunicados** (Resolução CD/ANPD nº 15/2024) | — |

## Celular (`cc.tumtum.app`)

| Categoria | Prazo |
|---|---|
| Log bruto da captura (`ble_samples`, `rr_intervals`, `motion`, `connection_events`) | Apagado quando a noite é guardada. **Noite nunca guardada:** fica até a pessoa apagar a noite [PENDENTE — decisão: alinhar aos 7 dias do servidor] |
| Noite no aparelho (`samples`, `moments`) | Até a pessoa apagar a noite ou a conta. Banco criptografado, fora do backup do Android |
| Arquivos de card compartilhados (`cacheDir/cards`) | 1 hora depois do compartilhamento; tudo na exclusão da conta |
| Exportações (`cacheDir/exports`), avatar (`filesDir/avatar_*`), fotos de card | Na exclusão da conta |
| Cards que a pessoa salvou na galeria ou postou | São dela. A TumTum não apaga |

## Piloto

Todo dado do piloto: **30 dias após o evento** (`docs/pilot-data-retention.md`).
No servidor, a série bruta sai antes, em 7 dias, pela regra geral. **A série
crua nos ZIPs de operador fica os 30 dias** [PENDENTE — decisão: alinhar a 7
ou justificar]. Termos assinados: 5 anos após o descarte, fora do repositório.

## Incidentes

Registro de incidente: no mínimo 5 anos, comunicados ou não, com a
justificativa da avaliação (`docs/incident-register.md`,
`docs/incident-response.md` §7).

## Backups: TTL e tombstone (P1)

Um backup guarda o que a pessoa já apagou, e "apagar a conta" só é verdade
depois que o último backup com ela expira (`docs/backups.md`). Duas coisas
antes do lançamento público (parecer §11 e teste "Backup restore" do §24):

1. **TTL curto e documentado.** O prazo dos backups do Railway conferido,
   encurtado se der, e escrito na política e na tela de apagar conta.
2. **Tombstone de exclusão.** Hoje, se um backup for restaurado, **não há
   como saber quem refazer**: o `deletion_log` guarda só a data, de propósito,
   e os pedidos do titular saem com a conta. Falta uma lista mínima das contas
   apagadas — um identificador que só serve para isso, sem nome, e-mail ou
   batimento —, guardada pelo mesmo prazo dos backups e usada por um passo
   obrigatório de toda restauração: reaplicar as exclusões antes de o serviço
   voltar. Teste de aceite: restaurar um backup num ambiente de teste e
   provar que uma conta excluída não reaparece.
