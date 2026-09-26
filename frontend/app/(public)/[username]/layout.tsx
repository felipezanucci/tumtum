import type { Metadata } from 'next'

/**
 * A person's public page stays out of search engines (26/09, LGPD audit
 * G5): it carries a name next to heartbeat numbers. The page itself is a
 * client component and cannot export metadata, so this layout does.
 */
export const metadata: Metadata = {
  robots: { index: false, follow: false },
}

export default function PublicProfileLayout({ children }: { children: React.ReactNode }) {
  return children
}
