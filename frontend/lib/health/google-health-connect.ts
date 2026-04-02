/**
 * Google Health Connect integration.
 *
 * In Phase 0, HR data from Android/Wear OS devices is synced via:
 * 1. Server-side: Google Fit REST API (handled by backend health_sync service)
 * 2. Client-side: Google Health Connect on-device API (via Android bridge)
 *
 * For the PWA MVP, we primarily rely on server-side sync after the user
 * grants OAuth access to Google Fit. The client triggers sync via the
 * /api/health/sync endpoint.
 */

const GOOGLE_FIT_SCOPES = [
  'https://www.googleapis.com/auth/fitness.heart_rate.read',
  'https://www.googleapis.com/auth/fitness.activity.read',
]

/**
 * Initiate Google OAuth flow for Health Connect / Google Fit access.
 * Returns the OAuth authorization URL to redirect the user to.
 */
export function getGoogleFitAuthUrl(clientId: string, redirectUri: string): string {
  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: redirectUri,
    response_type: 'code',
    scope: GOOGLE_FIT_SCOPES.join(' '),
    access_type: 'offline',
    prompt: 'consent',
  })
  return `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`
}

/**
 * Exchange an authorization code for access + refresh tokens.
 * This should be done server-side for security, but we provide the interface here.
 */
export async function exchangeGoogleCode(
  code: string,
  clientId: string,
  clientSecret: string,
  redirectUri: string,
): Promise<{ access_token: string; refresh_token: string }> {
  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      code,
      client_id: clientId,
      client_secret: clientSecret,
      redirect_uri: redirectUri,
      grant_type: 'authorization_code',
    }),
  })

  if (!response.ok) {
    throw new Error('Falha ao trocar código de autorização do Google')
  }

  return response.json()
}

/**
 * Check if Health Connect is available on the device (Android).
 */
export function isHealthConnectAvailable(): boolean {
  if (typeof window === 'undefined') return false
  // Check for Android bridge (future: Capacitor/TWA bridge)
  return 'Android' in window || navigator.userAgent.includes('Android')
}
