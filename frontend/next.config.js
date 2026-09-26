// The same fallback as lib/api.ts, so `next dev` proxies to the local API.
const API_BASE = (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000').replace(/\/+$/, '')

/** The API's origin alone, for connect-src: a path in the env var must not narrow it. */
function originOf(url) {
  try {
    return new URL(url).origin
  } catch {
    return url
  }
}
const API_ORIGIN = originOf(API_BASE)

/**
 * Content-Security-Policy (security review, 26/09). `'unsafe-inline'` stays
 * for scripts and styles because the App Router inlines its bootstrap and
 * flight data as <script> tags and next/font inlines its @font-face; a nonce
 * would need middleware on every request. `'unsafe-eval'` is never added.
 * `img-src https:` is for avatars and card images from the API; the site's
 * own photos and videos are local. `next dev` needs eval for Fast Refresh, so
 * the policy is sent only by the production server (`next build && next start`,
 * and Vercel); every other header is sent always.
 */
const CSP = [
  "default-src 'self'",
  "script-src 'self' 'unsafe-inline'",
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob: https:",
  "media-src 'self'",
  "font-src 'self' data:",
  `connect-src 'self' ${API_ORIGIN}`,
  "frame-ancestors 'none'",
  "base-uri 'self'",
  "form-action 'self'",
].join('; ')

const SECURITY_HEADERS = [
  ...(process.env.NODE_ENV === 'production' ? [{ key: 'Content-Security-Policy', value: CSP }] : []),
  { key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains' },
  { key: 'X-Content-Type-Options', value: 'nosniff' },
  { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
  { key: 'X-Frame-Options', value: 'DENY' },
  { key: 'Permissions-Policy', value: 'camera=(), microphone=(), geolocation=()' },
]

/** @type {import('next').NextConfig} */
const nextConfig = {
  // The site's session calls go through the site itself (26/09). The API
  // keeps the web refresh token in an httpOnly cookie; served from the Railway
  // host it would be a third-party cookie on tumtum.cc, and Safari blocks
  // those by default — every iPhone would lose its session with the tab.
  // Proxied here, the cookie is set by tumtum.cc and is first-party. Only
  // /api/auth/* goes this way; everything else calls the API directly with
  // the bearer token. A custom domain (api.tumtum.cc, same site as the web)
  // would make this rewrite unnecessary.
  async rewrites() {
    return [
      {
        source: '/api/auth/:path*',
        destination: `${API_BASE}/api/auth/:path*`,
      },
    ]
  },
  // No remotePatterns (security review, 26/09): next/image serves local
  // assets only, so the optimizer cannot be used to fetch arbitrary hosts.
  // A remote avatar is a plain <img> in components/ui/Avatar.tsx.
  async headers() {
    return [
      {
        source: '/:path*',
        headers: SECURITY_HEADERS,
      },
      // PWA headers
      {
        source: '/manifest.json',
        headers: [
          { key: 'Cache-Control', value: 'public, max-age=604800' },
        ],
      },
    ]
  },
}

module.exports = nextConfig
