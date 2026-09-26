// The same fallback as lib/api.ts, so `next dev` proxies to the local API.
const API_BASE = (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000').replace(/\/+$/, '')

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
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '**' },
    ],
  },
  // PWA headers
  async headers() {
    return [
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
