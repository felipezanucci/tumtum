# TumTum — App Android

Primeira implementação do app TumTum (Kotlin + Jetpack Compose, Health Connect, Room),
a partir do handoff do Claude Design. **Branch `app`** — o site continua na `main`.

- **APK de teste:** cada push nesta branch gera um build em
  [Releases](https://github.com/felipezanucci/tumtum/releases) (`tumtum-1.0-bN.apk`).
  Assinatura de debug, só para sideload — não é build de loja.
- **Como compilar e mapa tela → código:** veja [BUILDING.md](BUILDING.md).

Requisitos no aparelho: Android 9+ (minSdk 28) e Health Connect
(embutido no sistema a partir do Android 14).

## Privacidade (LGPD, 26/09)

- **Consentimento por finalidade.** Sete chaves, com os mesmos nomes do
  backend (`domain/ConsentText.kt`, versão do texto `2026-09-26`): `terms` e
  `read_heart_rate` são as duas do uso básico, cada uma com o seu toque; as
  cinco opcionais começam desligadas. A tela (`ui/screens/consent/`) vem
  depois do cadastro e de um primeiro login, antes do diálogo do Health
  Connect; vira porteira quando a conta não tem data de nascimento ou Termos
  aceitos; e abre focada numa chave quando o servidor responde
  `consent_required`. Em Configurações › Privacidade cada chave liga e
  desliga na hora (`PUT /api/consents`).
- **Cadastro:** data de nascimento na roda (nunca digitada) e o aceite dos
  Termos e da Política, com os dois links. Menos de 18 anos: o servidor
  recusa com a frase dele, mostrada como veio.
- **Nada sobe sozinho.** Encerrar a noite só salva no celular. A noite vai
  para o servidor quando a pessoa toca em *Guardar minha noite na TumTum*
  na revela, com a chave "Guardar a noite" ligada (`nights.sendRequested`,
  Room v9). As tentativas de novo, na abertura do app, são só dessas noites.
  *Apagar esta noite* apaga no celular e, se subiu, no servidor.
- **Criptografia local.** O Room é SQLCipher; a chave (32 bytes aleatórios)
  e os tokens da sessão ficam em `EncryptedSharedPreferences` (Keystore).
  Um banco antigo sem criptografia é convertido uma vez com
  `sqlcipher_export`; se a conversão falhar, o arquivo antigo é apagado
  (aceitável enquanto só há builds de teste). `allowBackup="false"`.
- **Minimização:** ao salvar a noite, as leituras cruas, R-R, movimento e o
  log de conexão daquele evento são apagados (fica a série da noite). O
  cache de cards perde o que tem mais de uma hora a cada volta de um
  compartilhamento; o vídeo da pessoa é remuxado sem localização antes de
  ir para um Story (`export/StripMetadata.kt`).
