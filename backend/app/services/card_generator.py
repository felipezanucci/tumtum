"""Share card image generator.

Generates visually stunning share cards using Pillow.
Cards are designed for Instagram Stories (1080x1920) and feed (1080x1080).

Card types:
- Solo: user's HR curve + peak moment + event info
- Comparison: user's HR vs artist's HR (future feature)
"""

import io
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from app.services import card_curve

FONT_DIR = Path(__file__).resolve().parent.parent / "assets" / "fonts"

# Weights named for the roles the brand manual assigns them.
_WEIGHTS = {"body": 400, "label": 500, "headline": 600, "hero": 700}


def _font(role: str, size: int) -> ImageFont.FreeTypeFont:
    """Instrument Sans at a brand role, falling back rather than crashing.

    The wordmark is deliberately not special-cased here: it is Chosmos and ships
    as an official vector. Until that asset exists it is set in Instrument Sans
    Bold, which is provisional and not brand-correct.
    """
    try:
        return ImageFont.truetype(
            str(FONT_DIR / f"InstrumentSans-{_WEIGHTS[role]}.ttf"), size
        )
    except OSError:
        return ImageFont.load_default()


# Brand manual MVP v0.1: two neutrals and one acid pair. Nothing from the
# previous red/cyan palette survives.
TUMTUM_BLACK = (0, 0, 0)  # #000000 — canvas
TUMTUM_WHITE = (255, 255, 255)  # #FFFFFF — supporting information
TUMTUM_LIME = (198, 255, 0)  # #C6FF00 — primary accent
TUMTUM_YELLOW = (239, 255, 0)  # #EFFF00 — secondary accent

# The palette has no greys, so separation on the black canvas comes from white
# at low alpha rather than from a new hue. Pillow draws without an alpha
# channel here, so these are the pre-composited equivalents over black.
TUMTUM_SURFACE = (13, 13, 13)  # white @ 5%
TUMTUM_BORDER = (36, 36, 36)  # white @ 14%
TUMTUM_MUTED = (153, 153, 153)  # white @ 60%


# Card dimensions
STORY_SIZE = (1080, 1920)
FEED_SIZE = (1080, 1080)
# Link previews are landscape everywhere. A 9:16 card sent to a preview slot
# gets cropped or shrunk, so shared links get a variant built for the slot.
OG_SIZE = (1200, 630)

# A Story is posted inside someone else's interface. The profile header covers
# the top of the frame and the reply bar the bottom, so anything placed there is
# simply not seen — including, before this, the wordmark and the whole event
# footer. The exact overlay differs between Instagram, TikTok and WhatsApp, so
# these are deliberately conservative.
STORY_SAFE_TOP = 250
STORY_SAFE_BOTTOM = 340


BRAND_DIR = Path(__file__).resolve().parent.parent / "assets" / "brand"


def _paste_wordmark(img: Image.Image, centre_x: int, top: int, height: int) -> None:
    """Stamp the official wordmark, scaled by height so the shape never changes.

    The manual forbids redrawing, stretching or slanting it, and forbids the
    Acid Lime and Toxic Yellow versions — so this pastes the white master and
    nothing recolours it. If the asset is missing the card simply carries no
    wordmark, which is better than substituting a lookalike.
    """
    try:
        mark = Image.open(BRAND_DIR / "tumtum-wordmark-white.png").convert("RGBA")
    except OSError:
        return
    width = round(mark.width * height / mark.height)
    mark = mark.resize((width, height), Image.LANCZOS)
    img.paste(mark, (centre_x - width // 2, top), mark)


# Share card 01 — "Só o momento". The manual's universal default: the only
# format that needs nothing but the user's own moment, so it is always
# available. The data dominates and the wordmark is small and never the
# subject. It now carries the night's curve when the caller has one; see
# app/services/card_curve.py for why that reverses an earlier decision.


def moment_copy(
    peak_bpm: int,
    avg_bpm: int | None,
    moment_time: str | None,
) -> tuple[str, str]:
    """Two lines built from facts this card can actually prove.

    This replaces a module-level constant — ``EU TAVA TRANQUILO. / AÍ VEIO
    ISSO.`` — that every card ever generated carried. It failed twice at once.
    A line identical on everybody's card says nothing about anybody, and a
    viral object has to be specific to the person holding it. Worse, "eu tava
    tranquilo" asserted a **calm baseline nobody measured**: if the person's
    night ran high the largest text on the card was simply false, which is
    this project's oldest bug class printed on a public object.

    So every branch here states something the card's own numbers establish,
    and the fallbacks claim less rather than inventing more. The night's
    average is the reference because it is the honest one available — the
    detector's local baseline is not stored on a peak, and a card must never
    reach for a number it does not have.
    """
    at = moment_time.upper() if moment_time else None

    # The night as the reference, and the moment as the departure from it.
    # Only when there is a rise: "até" promises one, and a promise the data
    # does not keep is the thing this function exists to stop.
    if avg_bpm and peak_bpm > avg_bpm:
        return (
            f"{avg_bpm} A NOITE INTEIRA.",
            f"ATÉ {at}." if at else "ATÉ ESSE MOMENTO.",
        )

    # No average, or no rise above it. State the moment and nothing else.
    return ("SEU CORAÇÃO,", f"ÀS {at}." if at else "NAQUELE MOMENTO.")


def _draw_curve(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    curve: list[int | None],
    peak_slot: int | None,
    avg_bpm: int | None,
) -> None:
    """The night's shape, with the moment marked. Evidence, not decoration.

    Breaks in the capture are drawn as breaks — `card_curve.segments` hands
    them over already split, and joining them here would redraw the exact lie
    that the quality score was fixed to stop telling.
    """
    x0, y0, x1, y1 = box
    readings = [v for v in curve if v is not None]
    if len(readings) < 2:
        return

    lo, hi = min(readings), max(readings)
    if avg_bpm:
        lo, hi = min(lo, avg_bpm), max(hi, avg_bpm)
    lo, hi = lo - 3, hi + 4
    span = max(hi - lo, 1)
    slots = max(len(curve) - 1, 1)

    def px(i: int, bpm: int) -> tuple[float, float]:
        return (x0 + i * (x1 - x0) / slots, y1 - (bpm - lo) / span * (y1 - y0))

    # The night's average, as a quiet reference the curve is read against.
    if avg_bpm:
        avg_y = px(0, avg_bpm)[1]
        for x in range(x0, x1, 22):
            draw.line(
                [(x, avg_y), (min(x + 11, x1), avg_y)], fill=TUMTUM_BORDER, width=3
            )
        draw.text(
            (x0, avg_y - 12),
            f"MÉDIA {avg_bpm}",
            fill=TUMTUM_MUTED,
            font=_font("label", 28),
            anchor="lb",
        )

    for run in card_curve.segments(curve):
        pts = [px(i, v) for i, v in run]
        if len(pts) == 1:
            x, y = pts[0]
            draw.ellipse([x - 3, y - 3, x + 3, y + 3], fill=TUMTUM_LIME)
        else:
            draw.line(pts, fill=TUMTUM_LIME, width=5, joint="curve")

    if peak_slot is not None and curve[peak_slot] is not None:
        x, y = px(peak_slot, curve[peak_slot])
        draw.line([(x, y0 - 18), (x, y - 16)], fill=TUMTUM_YELLOW, width=3)
        draw.ellipse([x - 14, y - 14, x + 14, y + 14], fill=TUMTUM_YELLOW)


def _fit_text(
    draw: ImageDraw.ImageDraw, text: str, role: str, size: int, max_width: int
) -> ImageFont.FreeTypeFont:
    """Shrink until the line fits, so a long artist name never runs off the card."""
    while size > 12:
        font = _font(role, size)
        if draw.textlength(text, font=font) <= max_width:
            return font
        size -= 2
    return _font(role, 12)


def generate_moment_card(
    user_name: str,
    event_name: str,
    event_date: str,
    peak_bpm: int,
    moment_label: str | None = None,
    moment_time: str | None = None,
    format: str = "story",
    avg_bpm: int | None = None,
    curve: list[int | None] | None = None,
    peak_slot: int | None = None,
) -> bytes:
    """Build share card 01, "Só o momento".

    Args:
        user_name: Display name, shown small as attribution
        event_name: Artist, teams or festival
        event_date: Already formatted for display
        peak_bpm: The number the whole card exists to show
        moment_label: What was happening — a song, a penalty, a goal
        moment_time: Clock time of the moment, e.g. "22h47"
        format: "story" (1080x1920), "feed" (1080x1080) or "og" (1200x630)
        avg_bpm: The night's average, the reference the peak is read against
        curve: The night on a uniform time grid, `None` where nothing was
            captured. Omit it and the card draws without evidence rather than
            drawing an invented line.
        peak_slot: Index into `curve` where the moment falls
    """
    if format == "og":
        return _moment_card_landscape(
            user_name,
            event_name,
            event_date,
            peak_bpm,
            moment_label,
            moment_time,
            avg_bpm,
        )

    size = FEED_SIZE if format == "feed" else STORY_SIZE
    w, h = size
    img = Image.new("RGB", size, TUMTUM_BLACK)
    draw = ImageDraw.Draw(img)
    margin = 72
    inner = w - margin * 2

    # A feed post owns its whole frame; a Story does not.
    safe_top = STORY_SAFE_TOP if format == "story" else 64
    safe_bottom = STORY_SAFE_BOTTOM if format == "story" else 64

    # Sitting flush against the safe line reads as cut the moment another app
    # draws its overlay a few pixels lower, so everything keeps a margin.
    breathe = 28
    _paste_wordmark(img, w // 2, safe_top + breathe, 30)

    # The copy calibrates the tone; the number carries the card. Both lines are
    # built from this night's own figures — see `moment_copy`.
    copy_size = 58 if format == "story" else 46
    copy_y = safe_top + breathe + (110 if format == "story" else 82)
    for line in moment_copy(peak_bpm, avg_bpm, moment_time):
        font = _fit_text(draw, line, "hero", copy_size, inner)
        draw.text((margin, copy_y), line, fill=TUMTUM_WHITE, font=font, anchor="lt")
        copy_y += round(copy_size * 1.14)

    has_curve = bool(curve) and sum(v is not None for v in curve) >= 2
    foot_top = h - safe_bottom - breathe - 190

    # The peak, as large as the card can carry it. With a curve below it the
    # number moves off centre and sets flush left: the two together are the
    # composition, and a centred number would leave the curve orphaned.
    number = str(peak_bpm)
    if has_curve:
        num_size = 380 if format == "story" else 240
        num_font = _fit_text(draw, number, "hero", num_size, inner - 160)
        num_top = copy_y + (34 if format == "story" else 18)
        draw.text(
            (margin - 12, num_top), number, fill=TUMTUM_LIME, font=num_font, anchor="lt"
        )
        nb = draw.textbbox((margin - 12, num_top), number, font=num_font, anchor="lt")
        draw.text(
            (nb[2] + 18, nb[3] - 24),
            "bpm",
            fill=TUMTUM_YELLOW,
            font=_font("hero", 54 if format == "story" else 40),
            anchor="ls",
        )
        y = nb[3] + 20
    else:
        num_size = 460 if format == "story" else 300
        num_font = _fit_text(draw, number, "hero", num_size, inner)
        # Centre the number in what is left between the copy and the footer,
        # so it sits in the part of the frame the viewer actually sees.
        num_y = (copy_y + foot_top) // 2
        draw.text((w // 2, num_y), number, fill=TUMTUM_LIME, font=num_font, anchor="mm")
        y = draw.textbbox((w // 2, num_y), number, font=num_font, anchor="mm")[3] + 12
        draw.text(
            (w // 2, y), "bpm", fill=TUMTUM_YELLOW, font=_font("hero", 46), anchor="mt"
        )
        y += 96

    if moment_label:
        label = f'durante "{moment_label}"'
        anchor = "lt" if has_curve else "mt"
        draw.text(
            (margin if has_curve else w // 2, y),
            label,
            fill=TUMTUM_WHITE,
            font=_fit_text(draw, label, "body", 40, inner),
            anchor=anchor,
        )
        y += 62

    if moment_time and not has_curve:
        # With a curve the time is already in the copy and on the marker;
        # printing it a third time is clutter, not emphasis.
        draw.text(
            (w // 2, y),
            moment_time,
            fill=TUMTUM_MUTED,
            font=_font("body", 32),
            anchor="mt",
        )

    if has_curve:
        top = y + (74 if format == "story" else 40)
        bottom = foot_top - (60 if format == "story" else 30)
        if bottom - top > 120:
            _draw_curve(
                draw, (margin, top, w - margin, bottom), curve, peak_slot, avg_bpm
            )

    # Event context sits at the foot: it identifies the night, it is not the
    # point — but it has to be above the reply bar to be read at all.
    foot = h - safe_bottom - breathe
    draw.text(
        (w // 2, foot),
        f"@{user_name}",
        fill=TUMTUM_MUTED,
        font=_font("body", 30),
        anchor="mb",
    )
    foot -= 54
    if event_date:
        draw.text(
            (w // 2, foot),
            event_date,
            fill=TUMTUM_MUTED,
            font=_font("body", 30),
            anchor="mb",
        )
        foot -= 50
    draw.text(
        (w // 2, foot),
        event_name,
        fill=TUMTUM_WHITE,
        font=_fit_text(draw, event_name, "headline", 46, inner),
        anchor="mb",
    )

    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    return buf.getvalue()


def _moment_card_landscape(
    user_name: str,
    event_name: str,
    event_date: str,
    peak_bpm: int,
    moment_label: str | None,
    moment_time: str | None,
    avg_bpm: int | None = None,
) -> bytes:
    """The same moment, laid out for a link preview.

    A 9:16 card dropped into a preview slot is cropped or shrunk by every
    platform, so this is a separate layout rather than the portrait one
    squeezed: the stack is measured and centred, which is what keeps 630px of
    height from collapsing into overlapping text.
    """
    w, h = OG_SIZE
    margin = 56
    inner = w - margin * 2
    img = Image.new("RGB", OG_SIZE, TUMTUM_BLACK)
    draw = ImageDraw.Draw(img)

    _paste_wordmark(img, w // 2, 34, 22)

    copy_font = _font("hero", 30)
    for i, line in enumerate(moment_copy(peak_bpm, avg_bpm, moment_time)):
        draw.text(
            (margin, 92 + i * 36), line, fill=TUMTUM_WHITE, font=copy_font, anchor="lt"
        )

    # Measure the middle stack, then centre it in the space that is left.
    number = str(peak_bpm)
    num_font = _fit_text(draw, number, "hero", 190, inner)
    num_h = draw.textbbox((0, 0), number, font=num_font, anchor="lt")[3]
    bpm_font = _font("hero", 28)
    label = f'durante "{moment_label}"' if moment_label else None
    label_font = _fit_text(draw, label, "body", 26, inner) if label else None

    stack = num_h + 8 + 32 + (34 if label else 0)
    top = 178 + max(0, (h - 178 - 132 - stack) // 2)

    draw.text((w // 2, top), number, fill=TUMTUM_LIME, font=num_font, anchor="mt")
    y = top + num_h + 8
    draw.text((w // 2, y), "bpm", fill=TUMTUM_YELLOW, font=bpm_font, anchor="mt")
    y += 32
    if label:
        draw.text((w // 2, y), label, fill=TUMTUM_WHITE, font=label_font, anchor="mt")

    foot = h - margin
    context = " · ".join(x for x in (event_date, moment_time) if x)
    if context:
        draw.text(
            (w // 2, foot),
            context,
            fill=TUMTUM_MUTED,
            font=_font("body", 22),
            anchor="mb",
        )
        foot -= 34
    draw.text(
        (w // 2, foot),
        event_name,
        fill=TUMTUM_WHITE,
        font=_fit_text(draw, event_name, "headline", 32, inner),
        anchor="mb",
    )

    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    return buf.getvalue()


def generate_solo_card(
    user_name: str,
    event_name: str,
    event_date: str,
    peak_bpm: int,
    avg_bpm: int,
    max_bpm: int,
    matched_label: str | None = None,
    hr_data: list[dict] | None = None,
    format: str = "story",
) -> bytes:
    """Generate a solo share card image.

    Args:
        user_name: Display name
        event_name: Event name
        event_date: Formatted date string
        peak_bpm: Peak BPM value to highlight
        avg_bpm: Average BPM
        max_bpm: Max BPM
        matched_label: What was happening at peak (e.g. song name)
        hr_data: Optional HR data points for mini curve
        format: "story" (1080x1920) or "feed" (1080x1080)

    Returns:
        PNG image bytes
    """
    size = STORY_SIZE if format == "story" else FEED_SIZE
    img = Image.new("RGB", size, TUMTUM_BLACK)
    draw = ImageDraw.Draw(img)

    w, h = size

    # Try to load fonts, fallback to default
    font_large = _font("hero", 120)
    font_medium = _font("hero", 42)
    font_small = _font("body", 32)
    font_label = _font("body", 28)

    # The canvas is flat black. The previous version washed a gradient of the
    # accent across it by mixing only the accent's red channel — harmless while
    # the accent was red, meaningless once it became lime. Accents concentrate
    # emphasis in this system; they are not a background wash.

    # Logo
    _paste_wordmark(img, w // 2, 72, 34)

    # Event name
    y_offset = 200 if format == "story" else 160
    draw.text(
        (w // 2, y_offset), event_name, fill=TUMTUM_WHITE, font=font_medium, anchor="mt"
    )
    draw.text(
        (w // 2, y_offset + 60),
        event_date,
        fill=TUMTUM_MUTED,
        font=font_small,
        anchor="mt",
    )

    # HR mini curve (if data provided)
    if hr_data and len(hr_data) > 5:
        curve_y_start = y_offset + 140
        curve_height = 300 if format == "story" else 200
        _draw_hr_curve(img, draw, hr_data, 60, curve_y_start, w - 120, curve_height)

    # Peak BPM highlight
    peak_y = (h // 2) + (100 if format == "story" else 50)
    draw.text(
        (w // 2, peak_y), str(peak_bpm), fill=TUMTUM_LIME, font=font_large, anchor="mm"
    )
    draw.text(
        (w // 2, peak_y + 80), "BPM", fill=TUMTUM_YELLOW, font=font_medium, anchor="mt"
    )

    # Matched label
    if matched_label:
        draw.text(
            (w // 2, peak_y + 150),
            f'durante "{matched_label}"',
            fill=TUMTUM_WHITE,
            font=font_small,
            anchor="mt",
        )

    # Stats bar
    stats_y = h - (400 if format == "story" else 200)
    stats = [
        ("Média", f"{avg_bpm} bpm"),
        ("Máximo", f"{max_bpm} bpm"),
    ]
    stat_width = w // len(stats)
    for i, (label, value) in enumerate(stats):
        x = stat_width * i + stat_width // 2
        draw.text((x, stats_y), value, fill=TUMTUM_WHITE, font=font_medium, anchor="mt")
        draw.text(
            (x, stats_y + 55), label, fill=TUMTUM_MUTED, font=font_label, anchor="mt"
        )

    # User attribution
    draw.text(
        (w // 2, h - 120),
        f"@{user_name}",
        fill=TUMTUM_MUTED,
        font=font_small,
        anchor="mt",
    )

    # Divider lines
    draw.line([(60, stats_y - 30), (w - 60, stats_y - 30)], fill=TUMTUM_BORDER, width=2)

    # Output
    buffer = io.BytesIO()
    img.save(buffer, format="PNG", quality=95)
    return buffer.getvalue()


def generate_comparison_card(
    user_name: str,
    artist_name: str,
    event_name: str,
    event_date: str,
    user_peak_bpm: int,
    artist_peak_bpm: int,
    sync_percentage: int,
    format: str = "story",
) -> bytes:
    """Generate a comparison share card (user vs artist HR).

    This is a future feature placeholder — artists will share their HR data
    so fans can compare their heartbeats.
    """
    size = STORY_SIZE if format == "story" else FEED_SIZE
    img = Image.new("RGB", size, TUMTUM_BLACK)
    draw = ImageDraw.Draw(img)

    w, h = size

    font_large = _font("hero", 120)
    font_medium = _font("hero", 42)
    font_small = _font("body", 32)

    # Background
    for y in range(h):
        alpha = y / h
        r = int(TUMTUM_BLACK[0] * (1 - alpha * 0.2))
        g = int(TUMTUM_BLACK[1] * (1 - alpha * 0.2))
        b = int(TUMTUM_BLACK[2] * (1 - alpha * 0.2) + 20 * alpha * 0.1)
        draw.line([(0, y), (w, y)], fill=(r, g, b))

    # Logo
    _paste_wordmark(img, w // 2, 72, 34)

    # Event
    draw.text(
        (w // 2, 180), event_name, fill=TUMTUM_WHITE, font=font_medium, anchor="mt"
    )
    draw.text(
        (w // 2, 240), event_date, fill=TUMTUM_MUTED, font=font_small, anchor="mt"
    )

    # Sync percentage (center)
    center_y = h // 2
    draw.text(
        (w // 2, center_y - 60),
        f"{sync_percentage}%",
        fill=TUMTUM_WHITE,
        font=font_large,
        anchor="mm",
    )
    draw.text(
        (w // 2, center_y + 20),
        "em sincronia",
        fill=TUMTUM_MUTED,
        font=font_small,
        anchor="mt",
    )

    # User vs Artist
    col_left = w // 4
    col_right = 3 * w // 4
    vs_y = center_y + 150

    draw.text(
        (col_left, vs_y),
        str(user_peak_bpm),
        fill=TUMTUM_LIME,
        font=font_large,
        anchor="mt",
    )
    draw.text(
        (col_left, vs_y + 90),
        "Seu pico",
        fill=TUMTUM_MUTED,
        font=font_small,
        anchor="mt",
    )

    draw.text(
        (col_right, vs_y),
        str(artist_peak_bpm),
        fill=TUMTUM_WHITE,
        font=font_large,
        anchor="mt",
    )
    draw.text(
        (col_right, vs_y + 90),
        artist_name,
        fill=TUMTUM_MUTED,
        font=font_small,
        anchor="mt",
    )

    draw.text(
        (w // 2, vs_y + 40), "vs", fill=TUMTUM_MUTED, font=font_medium, anchor="mm"
    )

    # User
    draw.text(
        (w // 2, h - 120),
        f"@{user_name}",
        fill=TUMTUM_MUTED,
        font=font_small,
        anchor="mt",
    )

    buffer = io.BytesIO()
    img.save(buffer, format="PNG", quality=95)
    return buffer.getvalue()


def _draw_hr_curve(
    img: Image.Image,
    draw: ImageDraw.ImageDraw,
    hr_data: list[dict],
    x_start: int,
    y_start: int,
    width: int,
    height: int,
) -> None:
    """Draw the user's own beat over time.

    Not an ECG trace: the brand manual allows a time series only when it is the
    user's real data and the chart is product information, and forbids anything
    that reads as medical waveform.
    """
    if len(hr_data) < 2:
        return

    bpm_values = [d["bpm"] for d in hr_data]
    min_bpm = max(min(bpm_values) - 5, 30)
    max_bpm = min(max(bpm_values) + 5, 250)
    bpm_range = max_bpm - min_bpm if max_bpm > min_bpm else 1

    # Downsample to ~100 points for drawing
    step = max(1, len(bpm_values) // 100)
    sampled = bpm_values[::step]

    points = []
    for i, bpm in enumerate(sampled):
        x = x_start + (i / (len(sampled) - 1)) * width
        y = y_start + height - ((bpm - min_bpm) / bpm_range) * height
        points.append((x, y))

    if len(points) >= 2:
        area_points = [
            *points,
            (points[-1][0], y_start + height),
            (points[0][0], y_start + height),
        ]
        overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
        ImageDraw.Draw(overlay).polygon(area_points, fill=(*TUMTUM_LIME, 38))
        img.alpha_composite(overlay) if img.mode == "RGBA" else img.paste(
            Image.alpha_composite(img.convert("RGBA"), overlay).convert(img.mode)
        )

        # Redraw on the composited surface: the line is the evidence.
        draw = ImageDraw.Draw(img)
        for i in range(len(points) - 1):
            draw.line([points[i], points[i + 1]], fill=TUMTUM_LIME, width=3)
