# TumTum — App Android

Primeira implementação do app TumTum (Kotlin + Jetpack Compose, Health Connect, Room),
a partir do handoff do Claude Design. **Branch `app`** — o site continua na `main`.

- **APK de teste:** cada push nesta branch gera um build em
  [Releases](https://github.com/felipezanucci/tumtum/releases) (`tumtum-1.0-bN.apk`).
  Assinatura de debug, só para sideload — não é build de loja.
- **Como compilar e mapa tela → código:** veja [BUILDING.md](BUILDING.md).

Requisitos no aparelho: Android 9+ (minSdk 28) e Health Connect
(embutido no sistema a partir do Android 14).
