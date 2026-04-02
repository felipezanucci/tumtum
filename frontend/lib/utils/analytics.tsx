'use client'

import { useEffect } from 'react'

const POSTHOG_KEY = process.env.NEXT_PUBLIC_POSTHOG_KEY || ''
const POSTHOG_HOST = process.env.NEXT_PUBLIC_POSTHOG_HOST || 'https://app.posthog.com'

let posthogLoaded = false

function loadPostHog() {
  if (posthogLoaded || !POSTHOG_KEY || typeof window === 'undefined') return
  posthogLoaded = true

  // Load PostHog snippet
  const script = document.createElement('script')
  script.innerHTML = `
    !function(t,e){var o,n,p,r;e.__SV||(window.posthog=e,e._i=[],e.init=function(i,s,a){function g(t,e){var o=e.split(".");2==o.length&&(t=t[o[0]],e=o[1]),t[e]=function(){t.push([e].concat(Array.prototype.slice.call(arguments,0)))}}(p=t.createElement("script")).type="text/javascript",p.async=!0,p.src=s.api_host+"/static/array.js",(r=t.getElementsByTagName("script")[0]).parentNode.insertBefore(p,r);var u=e;for(void 0!==a?u=e[a]=[]:a="posthog",u.people=u.people||[],u.toString=function(t){var e="posthog";return"posthog"!==a&&(e+="."+a),t||(e+=" (stub)"),e},u.people.toString=function(){return u.toString(1)+".people (stub)"},o="capture identify alias people.set people.set_once set_config register register_once unregister opt_out_capturing has_opted_out_capturing opt_in_capturing reset isFeatureEnabled onFeatureFlags getFeatureFlag getFeatureFlagPayload reloadFeatureFlags group updateEarlyAccessFeatureEnrollment getEarlyAccessFeatures getActiveMatchingSurveys getSurveys".split(" "),n=0;n<o.length;n++)g(u,o[n]);e._i.push([i,s,a])},e.__SV=1)}(document,window.posthog||[]);
    posthog.init('${POSTHOG_KEY}',{api_host:'${POSTHOG_HOST}'});
  `
  document.head.appendChild(script)
}

export function trackEvent(eventName: string, properties?: Record<string, unknown>) {
  if (typeof window !== 'undefined' && (window as any).posthog) {
    ;(window as any).posthog.capture(eventName, properties)
  }
}

export function identifyUser(userId: string, traits?: Record<string, unknown>) {
  if (typeof window !== 'undefined' && (window as any).posthog) {
    ;(window as any).posthog.identify(userId, traits)
  }
}

export function PostHogProvider({ children }: { children: React.ReactNode }) {
  useEffect(() => {
    loadPostHog()
  }, [])

  return <>{children}</>
}
