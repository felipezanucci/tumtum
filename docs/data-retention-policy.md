# Política de retenção de dados

26/09/2026. Nasceu da auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, itens
D1, D2, L4): havia três prazos escritos e nenhum job que cumprisse algum.
Agora cada categoria tem prazo, e quem o cumpre está na última coluna.

**Princípio:** um dado fica o tempo que a finalidade dele precisa, e nem um
dia a mais (art. 15 e 16 da LGPD). Quando a pessoa apaga a conta, sai tudo o
que é dela, exceto o que a lei manda guardar — e isso está dito abaixo.

Os números vivem no código como constantes ou variáveis de ambiente; mude lá
e aqui juntos, e registre no `docs/decision-log.md`.

## Servidor

| Categoria | Prazo | Quem apaga |
|---|---|---|
| **Série bruta** (`hr_data`) | **30 dias** depois de a noite ser analisada (`hr_sessions.analyzed_at`). `RAW_READINGS_RETENTION_DAYS`, padrão 30 | Ciclo diário `services/maintenance.py` |
| **Noite resumida** (`hr_sessions`: início, fim, média/máx/mín, qualidade), **momentos** (`peaks`), **cards** | Enquanto a conta existir e o consentimento `keep_night` estiver ativo. A pessoa apaga uma noite quando quiser (`DELETE /api/health/sessions/{id}`) | A pessoa; exclusão da conta. **Revogar `keep_night` ainda não apaga as já guardadas** [PENDENTE — decisão, `docs/ropa.md` §4] |
| **Imagem do card em cache** (Redis `card:image:{id}`, `:og`) | **7 dias**; sai na hora com o card ou a conta | TTL do Redis; `DELETE /api/cards/{id}`; exclusão da conta |
| **Card público** | Só enquanto publicado (`published_at`); despublicar tira do ar | A pessoa |
| **Posts do feed**, reações, vínculo com turnê | Até a pessoa apagar o post ou a conta. Post apagado fica marcado com `deleted_at` [PENDENTE — remoção física] | A pessoa; exclusão da conta |
| **Denúncias e bloqueios** | Enquanto a conta de quem denunciou/bloqueou existir | Exclusão da conta |
| **Conta** (nome, e-mail, hash de senha, data de nascimento) | Enquanto a conta existir | Exclusão da conta (`POST /api/users/me/delete`) |
| **Consentimentos** (`consents`) | Enquanto a conta existir (histórico completo, só acrescenta) | Exclusão da conta |
| **Pedidos de titular** (`data_subject_requests`) | Enquanto a conta existir | Exclusão da conta |
| **Quem leu dado de saúde** (`data_access_log`) | Enquanto a conta do titular existir [PENDENTE — prazo máximo; sugestão 180 dias] | Exclusão da conta |
| **Registro de acesso** (`access_log`: IP, rota, status, hora, conta) | **180 dias** — guarda obrigatória de 6 meses (Marco Civil, art. 15). **Não sai com a conta** | Ciclo diário |
| **Prova de exclusão** (`deletion_log`) | Indefinida. Só a data, nenhum identificador | — |
| **Refresh tokens** | 90 dias do último uso; linhas expiradas saem 24 h depois | Ciclo diário |
| **Códigos**: cadastro (`signup_codes`), troca de e-mail (`email_changes`), senha (`password_reset_tokens`) | Até 24 h depois de expirar; cadastro não confirmado sai em 1 dia | Ciclo diário; exclusão da conta (por e-mail) |
| **Lista de espera** (`waitlist_entries`: e-mail, nome, sobrenome, origem) | Até a pessoa pedir para sair; sai também com a conta do mesmo e-mail | Pedido (oi@tumtum.cc); exclusão da conta |
| **Erros no Sentry** | Retenção do plano: [PENDENTE — conferir no Sentry]. Sem PII nem corpo de requisição | Sentry |
| **E-mails enviados (Resend)** | Retenção do log do Resend: [PENDENTE — conferir] | Resend |
| **Backups do banco** | [PENDENTE — prazo do Railway] (`docs/backups.md`) | Railway |

## Celular (`cc.tumtum.app`)

| Categoria | Prazo |
|---|---|
| Log bruto da captura (`ble_samples`, `rr_intervals`, `motion`, `connection_events`) | Apagado quando a noite é guardada |
| Noite no aparelho (`samples`, `moments`) | Até a pessoa apagar a noite ou a conta. Banco criptografado, fora do backup do Android |
| Arquivos de card compartilhados (`cacheDir/cards`) | 1 hora depois do compartilhamento; tudo na exclusão da conta |
| Exportações (`cacheDir/exports`), avatar (`filesDir/avatar_*`), fotos de card | Na exclusão da conta |
| Cards que a pessoa salvou na galeria ou postou | São dela. A TumTum não apaga |

## Piloto

Todo dado do piloto: **30 dias após o evento** (`docs/pilot-data-retention.md`).
Termos assinados: 5 anos após o descarte, fora do repositório.

## Incidentes

Registro de incidente: no mínimo 5 anos (`docs/incident-response.md`).
