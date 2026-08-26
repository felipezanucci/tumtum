/**
 * Where footage goes once it exists.
 *
 * The landing page is built around video it does not have yet — a crowd, a
 * fan at the barrier, a stage seen from the floor. Rather than ship the page
 * without its structure, each slot renders a breathing gradient and says, in
 * the corner, what belongs there. The label is visible on purpose: this page
 * goes to people who might supply the footage.
 *
 * To fill one, pass `src`. Nothing else about the page changes.
 */
export function VideoSlot({
  label,
  src,
  className = '',
}: {
  label: string
  src?: string
  className?: string
}) {
  return (
    <div className={`relative overflow-hidden bg-[#050505] ${className}`}>
      {src ? (
        <video
          className="absolute inset-0 h-full w-full object-cover"
          autoPlay
          muted
          loop
          playsInline
          src={src}
        />
      ) : (
        <>
          <div className="absolute inset-0 animate-breathe bg-[radial-gradient(120%_90%_at_30%_80%,rgba(198,255,0,0.10),transparent_55%),radial-gradient(100%_80%_at_75%_20%,rgba(239,255,0,0.06),transparent_50%),linear-gradient(160deg,#0b0b0b,#000_60%)] motion-reduce:animate-none">
            <div className="absolute inset-0 bg-[radial-gradient(rgba(255,255,255,0.05)_1px,transparent_1px)] bg-[length:5px_5px] opacity-50" />
          </div>
          <span className="absolute bottom-3.5 left-4 z-[2] rounded-full border border-dashed border-tumtum-faint px-3 py-1.5 text-[11px] font-headline uppercase tracking-[0.14em] text-tumtum-faint">
            {label}
          </span>
        </>
      )}
    </div>
  )
}
