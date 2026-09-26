# Tumtum

TumTum is a live entertainment technology platform that shows when people's hearts rose during their most exciting moments — concerts, sports matches, festivals — by recording their heart rate during the event, with their consent, and matching it to the event timeline. Users can collect, relive, and share those moments on social media. TumTum is not a medical device and does not interpret health.

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Node.js 18+ (for frontend development)
- Python 3.11+ (for backend development)

### Local Development

1. Clone the repository
2. Start the services: `docker-compose up -d`
3. Install frontend dependencies: `cd frontend && npm install`
4. Start frontend: `npm run dev`
5. For backend development, install dependencies: `cd backend && pip install -r requirements.txt`
6. Run backend: `uvicorn app.main:app --reload`

Running the backend outside Docker needs a `SECRET_KEY` of at least 32
characters, or `ALLOW_WEAK_SECRET_KEY=true` for a throwaway local key: since
26/09/2026 the server refuses to start on a known placeholder.

## Project Structure

See CLAUDE.md for detailed project structure.

## Privacy

TumTum handles heart rate, which is sensitive personal data under the LGPD.
Three rules hold for everyone who works in this repository:

- **No real personal data in the tree.** Test fixtures are synthetic, test
  e-mails use `@exemplo.com`, and contacts live outside the repository. The
  `privacy-scan` job in `.github/workflows/ci.yml` fails on e-mail addresses
  outside its allowlist, on committed secrets, and on `allowBackup="true"`.
- **Debug APKs are for the founder's own test phone.** Participants install
  the app only from the Play internal-testing track.
- **Every processing of personal data is on record** in `docs/ropa.md`, with
  its legal basis and retention (`docs/data-retention-policy.md`). The audit
  that set this up is `RELATORIO-AUDITORIA-LGPD.md`; the encarregado and the
  incident plan are `docs/dpo.md` and `docs/incident-response.md`.
