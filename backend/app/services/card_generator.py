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
