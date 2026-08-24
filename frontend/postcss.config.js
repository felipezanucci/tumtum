/**
 * Without this file Next.js never runs Tailwind's PostCSS plugin: the
 * @tailwind directives in globals.css are emitted verbatim, no utility classes
 * are generated, and the build still succeeds — which is why every deployment
 * so far rendered as unstyled HTML.
 */
module.exports = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
