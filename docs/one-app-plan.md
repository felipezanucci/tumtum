# One app — bringing the proven pipeline into the designed app

**Decided:** 2026-09-18, by Felipe: *"a gente precisa centralizar tudo em um
app só… vamos considerar esse app que já tem toda experiência desenhada e
layout aprovado."*
**The app:** `cc.tumtum.app`, today on branch `app` — the first
implementation of the Claude Design app handoff (01–02/09).
**What it absorbs:** the parts of `cc.tumtum.capture` (`android/`) that are
proven and that it does not have — a server account, the upload of a night,
the detector, the event with a timeline, the Play release.
**Why it is written down:** two apps were built in different sessions from
different briefs, and the decision log did not know (entry of 2026-09-18,
item 33). This is the map of the merge, so a session that starts cold can
pick up any stage.

---

## 1. What each app has today

| | `cc.tumtum.app` (designed) | `cc.tumtum.capture` (proven) |
|---|---|---|
| Screens | Onboarding, account, the quiet permission screen, watch sources, live capture, **the reveal**, gallery, skins, card, public profile, feed, settings | Sign-in, events, capture, night (curve + moments), card, watch import |
| Capture | BLE 0x180D with a foreground service, reconnection, wake lock, battery-exemption gate, motion at 1 Hz, double timestamps, ZIP export | BLE 0x180D with a foreground service; proven six hours at Realness |
| Health Connect | Reads, measures cadence per source | Reads, measures cadence, the honest "dá para procurar" verdict |
| Moments | `NightAnalyzer.moments()`: **the 8 highest samples at least 10 min apart** — a top-N, not a detector; blind to everything the median detector was built for | Backend `detect_peaks()`: median baseline, hysteresis, minimum rise, causal naming (rebuilt 17/09, 19 tests) |
| Event | Name + venue typed on the phone | Chosen from the server's list; timeline entries name the moments |
| Storage | Room on the phone, nothing leaves it | Postgres/Timescale on Railway; five people's nights in one place |
| Card | Rendered on the phone at 1080×1920, share sheet | Rendered on the server, share sheet, **public link** `tumtum.cc/cards/…` |
| Account | Local (name, handle, avatar) | Server (JWT with known expiry, password reset by e-mail) |
| Social | `FakeSocialRepository` | — |
| Protocol | **Reveal lock**: a night opens at 10:00 the next morning | — |
| Build | Debug APK per push, GitHub Release `app-b<N>`, committed debug key; `release` block with R8 + shrink, unsigned | Debug APK + **release `.aab` signed with the upload key** (17/09) |
| History | 19 commits, **no common ancestor with `main`** — a separate tree | `main` |

The line that matters most: **the designed app has never sent a byte to a
server.** Everything it shows comes from the phone, and everything the
pilot needs to compare five people, name a goal, or survive a lost phone
lives on the server side of the other app.

---

## 2. Stages

Each stage ends at a gate a person can check on a phone. Estimates are
working sessions; calendar follows the device loops.

### Etapa 0 — One repository · ½ session

The `app` tree moves into `main` as `android/`; today's `android/`
(capture) moves to `android-capture/` and stays as the reference until every
piece below is ported, then goes. One workflow builds the debug APK on
every push and the release `.aab` when the upload-key secrets exist — the
job written on 17/09, pointed at the new tree. `versionCode` keeps coming
from the run number so builds install over each other.

**Gate:** CI green on `main` producing `cc.tumtum.app`; the APK installs
over the build Felipe has.

### Etapa 1 — A real account · 1 session

Port `TumtumApi.kt` (HttpURLConnection, zero dependencies, a token that
knows its own expiry — the capture app's lesson of 27/08). "Criar conta"
and sign-in talk to `/api/auth`; the local profile (name, handle, avatar)
stays local for now.

**Gate:** sign in on a phone; the token survives a restart; a wrong
password says so.

### Etapa 2 — The night goes up, and the moments come back · 1–2 sessions

When an event closes, the samples upload to `POST /api/health/sessions`
(the endpoint the strap already feeds), the app calls `analyze`, and the
moments the server returns become the night's moments in Room. The reveal
screen shows the server's moments; `NightAnalyzer.moments()` stays only as
the offline fallback and says so on screen. Upload retries, and the night
is never lost to a failed upload — the snapshot on the phone is the
source of truth until the server confirms.

**Gate:** a two-minute rehearsal capture appears on the server with the
same peak the phone shows, and the moments on the reveal are the server's.

### Etapa 3 — The event has a name · 1 session

Before capturing, the event is chosen from `/api/events` (or created
there). A **"marcar momento"** button posts the current UTC time to
`/api/events/{id}/timeline` — kick-off, goal, half-time — so the server's
correlator names the moments (the timeline integrations are dead code,
item 28; a button is the honest replacement).

**Gate:** a rehearsal with one marked "gol" comes back as a moment named
"gol".

### Etapa 4 — Play · ½ session + Google's time

Release signing in the app's `build.gradle.kts` with the upload key of
17/09; R8 and shrinking **tested on a phone first** (Room, Compose and a
foreground service are exactly what an untested minified build breaks);
the `.aab` to the internal testing track; package **`cc.tumtum.app`**,
which is the name the Play Console must be given on "Create app" — it
cannot change afterwards.

**Gate:** a tester installs from the Play link and captures.

### Later — not for the pilot

- The public card link: the phone-rendered PNG uploads to the server so
  `tumtum.cc/cards/{id}` shows it.
- `FakeSocialRepository` → the server; the feed and "A galera" (card 04).
- `DELETE /api/users/me` behind the settings screen's "apagar" (item 32).
- The reveal lock against the pilot's debrief: a night that opens at 10:00
  the next day cannot be discussed at the venue. Keep the protocol or make
  it a per-event setting — a product call, not a code one.

---

## 3. Estimate and the calendar

| | |
|---|---|
| Sessions | 4–5 |
| Calendar | ~2 weeks, with the device loops |
| First match candidate | 10/10 — 3 weeks away |
| Developer verification in Brazil | 30/09 — met by registering `cc.tumtum.app` + its certificate the moment the account is verified, independent of the code |

It fits, with room for one surprise. It does not fit two.

---

## 4. What the decision changes in what was done this week

| Done for `cc.tumtum.capture` | Now |
|---|---|
| Detector rebuilt on the backend | **Unchanged and central** — Etapa 2 is what makes the designed app use it |
| Upload key + release build + `.aab` job | Key reused; the build config and the job move to the new tree in Etapa 0/4 |
| Play Console plan | Same account, same steps; the package on "Create app" is **`cc.tumtum.app`** |
| Privacy page | Still true — one Health Connect permission, the event window, nothing sold; after Etapa 2 it must add that nights are stored on our servers, which it already says |
| App 0.2.1 empty state | Belongs to the app being retired |
| Open item 23 (Mi Band) and the pilot-event shortlist | Unchanged |

---

## 5. Risks

- **A minified release build.** The designed app's `release` block turns
  R8 and shrinking on and has never been built. Test on a phone before it
  goes anywhere near the Play track.
- **Two moment finders disagreeing.** Until Etapa 2 lands, the phone's
  top-8 and the server's detector will name different moments for the
  same night. The screen must say which one it is showing.
- **Local nights are gone if the phone is.** Etapa 2 is also the backup.
- **The reveal lock vs. the pilot protocol** — decide before the first
  event, not at it.
