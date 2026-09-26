# android-capture — `cc.tumtum.capture` (referência aposentada)

Este é o app de captura que provou o pipeline (Realness, 29/08: seis horas
com a cinta, 26.999 de 27.000 leituras). Desde 18/09 **o app é
`cc.tumtum.app`**, em `android/`, que absorveu este pipeline em etapas
(`docs/one-app-plan.md`). Este diretório fica só como **referência de
código** até a última peça ser portada, e então sai.

**Não é para participantes.** Nenhum fã, nenhum participante do piloto e
nenhuma pessoa além do próprio Felipe instala este APK. Ele não tem a tela
de consentimento por finalidade, não tem verificação de idade, não tem
"guardar a noite" como ato explícito, guarda as leituras sem criptografia
no aparelho, e é assinado com a chave de debug que está neste repositório
público. O participante recebe só o build do app principal pela faixa de
teste interno do Play (`docs/pilot-consent-template.md`). Decisão de
26/09/2026, da auditoria LGPD (`RELATORIO-AUDITORIA-LGPD.md`, AL-11 e AL-13).

O que mudou aqui em 26/09: a notificação de captura não mostra mais o bpm ao
vivo (só a contagem de leituras) e fica `VISIBILITY_PRIVATE` na tela de
bloqueio; e o texto do relógio deixou de falar em "treino".

O workflow `build-capture-apk.yml` só gera um artefato de CI (nunca uma
release pública).
