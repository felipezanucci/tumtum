# App signing fingerprints — cc.tumtum.app

Every share integration (Meta, TikTok, Snap) and some APIs ask which
certificates sign the app. These are **public** values — they are printed in
every copy of the app — and they are collected here so the next portal does
not cost another afternoon in the Play Console. Recorded 24/09/2026.

Two keys sign TumTum, depending on where the APK came from:

| Source | Certificate | Notes |
|---|---|---|
| Test APKs (`releases/download/app-bN/…`) | `android/app/debug.keystore` | Committed, so every build updates in place |
| Google Play | `deployment_cert.der` | **The app signing key** — the one Play signs delivered APKs with, and the one in the Digital Asset Links JSON |
| Google Play (hybrid, Beta) | `hybrid_classical_cert.der` | The classical half of Play's post-quantum "hybrid" key; registered too, to be safe |
| Google Play (hybrid, Beta) | `hybrid_pqc_cert.der` | Post-quantum half. No portal uses it yet — **not registered** |

The Play certificates come from Play Console → Protegido com o Google Play →
Proteção da Google Play Store → Gerencie a Assinatura de Apps → **Baixar
certificados** (a zip). The console no longer prints MD5 for them; it was
computed from the downloaded `.der` files.

## Test APKs (debug.keystore)

- MD5: `E8:E3:10:BC:ED:63:8A:3B:3F:6E:06:62:E6:DF:88:37` — `e8e310bced638a3b3f6e0662e6df8837`
- SHA-1: `39:FE:E4:FF:99:B0:31:A7:F6:6B:01:A4:32:EA:7C:4A:8D:13:83:0A`
- SHA-256: `43:7A:23:22:03:FF:34:A4:9E:7E:CE:86:FF:4A:24:3C:40:8D:37:3A:CC:5D:B3:F5:BD:C8:2F:14:A8:E5:13:54`
- Facebook key hash: `Of7k/5mwMaf2awGkMup8So0Tgwo=`

## Google Play — app signing key (deployment_cert)

- MD5: `EB:49:43:3E:D4:7F:85:1E:D5:5C:95:D4:22:6E:90:60` — `eb49433ed47f851ed55c95d4226e9060`
- SHA-1: `D6:FE:DD:F8:99:2F:09:E3:83:D9:E6:3C:AF:0C:E2:64:58:5C:5A:15`
- SHA-256: `43:C6:4A:42:E3:6C:8E:D7:C2:D3:CB:76:05:52:97:80:C4:17:D1:C7:E1:CF:BE:A9:1F:78:D5:A9:72:88:47:4C`
- Facebook key hash: `1v7d+JkvCeOD2eY8rwziZFhcWhU=`

## Google Play — hybrid classical key

- MD5: `DD:FB:91:37:17:56:9C:18:01:E1:5B:17:BC:D5:5F:78` — `ddfb913717569c1801e15b17bcd55f78`
- SHA-1: `F8:CE:37:95:A0:BA:31:43:AA:EE:01:D5:FC:67:E7:CF:5B:15:13:FC`
- SHA-256: `8E:6E:C7:D8:24:AD:AF:DA:22:8E:71:BF:E6:E2:F5:24:1F:B2:25:6B:4B:A5:C1:5C:EF:86:EA:15:D9:92:02:43`
- Facebook key hash: `+M43laC6MUOq7gHV/Gfnz1sVE/w=`

## Where each is registered

| Portal | Field | Registered |
|---|---|---|
| Meta (App ID 1076995595141587) | Hashes chave | test + both Play classical |
| TikTok | App signature (MD5) | test + both Play classical |
| TikTok | Signing certificate fingerprints (SHA-256) | test + both Play classical |
| Snap | — | Creative Kit Lite asks for none |
