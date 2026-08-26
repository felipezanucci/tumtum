/**
 * Social sharing utilities.
 *
 * Uses the Web Share API when available (mobile browsers),
 * with fallbacks to platform-specific share URLs.
 */

export interface ShareData {
  title: string
  text: string
  url: string
  imageUrl?: string
}

/**
 * Check if Web Share API is available (typically mobile browsers).
 */
declare global {
  interface Window {
    /**
     * Present only inside the TumTum Android app's WebView. A WebView has no
     * navigator.share, so without this the page falls back to the platform
     * list — a dead end inside the one place that can open the real Android
     * share sheet natively. The app injects this bridge; the page prefers it.
     */
    TumTumAndroid?: { shareImage(imageUrl: string, text: string): void }
  }
}

function androidBridge() {
  return typeof window !== 'undefined' ? window.TumTumAndroid : undefined
}

export function canNativeShare(): boolean {
  if (androidBridge()?.shareImage) return true
  return typeof navigator !== 'undefined' && !!navigator.share
}

/**
 * Share via the native Web Share API.
 */
export async function nativeShare(data: ShareData): Promise<boolean> {
  if (!canNativeShare()) return false

  // Inside the app, hand the whole thing to Android: it downloads the image
  // and opens the system sheet with the picture attached — the same outcome
  // the Web Share path below builds by hand, without needing an API the
  // WebView does not have.
  const bridge = androidBridge()
  if (bridge?.shareImage) {
    bridge.shareImage(data.imageUrl ?? '', [data.text, data.url].filter(Boolean).join(' '))
    return true
  }

  // Share the picture, not a link to it. Instagram and TikTok take an image;
  // handed a URL they have nothing to post. The card is the whole point of
  // sharing, so a link-only share is a share that mostly does not happen.
  if (data.imageUrl) {
    const file = await fetchAsFile(data.imageUrl)
    if (file) {
      // Deliberately no `url` key. Handed both a file and a url, Android
      // builds a "share a link, with a picture" sheet — it offers to copy the
      // image with the link, and the apps it lists are the ones that take
      // links. Instagram and WhatsApp want a picture. The link still travels,
      // inside the text, where an app that reads captions will show it.
      const withLink = data.url ? `${data.text} ${data.url}` : data.text
      const payloads = [
        { files: [file], text: withLink },
        { files: [file], text: data.text },
        { files: [file] },
      ]
      for (const payload of payloads) {
        if (!navigator.canShare?.(payload)) continue
        try {
          await navigator.share(payload)
          return true
        } catch (error) {
          // A cancelled sheet is a decision, not a failure to fall back from.
          if (error instanceof DOMException && error.name === 'AbortError') return false
          break
        }
      }
    }
  }

  try {
    await navigator.share({
      title: data.title,
      text: data.text,
      url: data.url,
    })
    return true
  } catch {
    // User cancelled or share failed
    return false
  }
}

/** Download an image so it can be handed to the share sheet as a file. */
async function fetchAsFile(url: string): Promise<File | null> {
  try {
    const response = await fetch(url)
    if (!response.ok) return null
    const blob = await response.blob()
    if (!blob.type.startsWith('image/')) return null
    return new File([blob], 'tumtum.png', { type: blob.type })
  } catch {
    // Offline, or the image host refused us. The link share still works.
    return null
  }
}

/**
 * Generate platform-specific share URLs.
 */
export function getShareUrl(
  platform: 'instagram' | 'tiktok' | 'x' | 'whatsapp' | 'link',
  data: ShareData,
): string {
  const encodedText = encodeURIComponent(`${data.text} ${data.url}`)
  const encodedUrl = encodeURIComponent(data.url)

  switch (platform) {
    case 'x':
      return `https://twitter.com/intent/tweet?text=${encodedText}`

    case 'whatsapp':
      return `https://wa.me/?text=${encodedText}`

    case 'instagram':
      // Instagram doesn't support URL-based sharing for feed posts.
      // User needs to save the image and share manually.
      // For Stories, we'd use the Instagram Stories deep link (mobile only).
      return `instagram://story-camera`

    case 'tiktok':
      // TikTok doesn't support URL-based sharing.
      // User saves the card and uploads manually.
      return data.url

    case 'link':
      return data.url

    default:
      return data.url
  }
}

/**
 * Copy a URL to clipboard.
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    return false
  }
}

/**
 * Download an image from a URL.
 */
export function downloadImage(url: string, filename: string = 'tumtum-card.png'): void {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
