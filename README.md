# TumTum

TumTum is a live entertainment technology platform that captures how people feel during their most exciting moments — concerts, sports matches, festivals — by monitoring their heart rate and correlating it with the event timeline. Users can collect, relive, and share those emotional highlights on social media.

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

## Project Structure

See CLAUDE.md for detailed project structure.
## Brand

Brand identity v2.0 lives in [`shared/brand/`](shared/brand/) — see
[`BRAND.md`](shared/brand/BRAND.md) for the full guidelines, `tokens.json` / `tokens.css` for the
machine-readable values, and `logo/` for the SVG assets. In the app, render the mark with
`frontend/components/brand/Logo.tsx`.
