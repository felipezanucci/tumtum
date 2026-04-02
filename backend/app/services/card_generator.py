"""Share card image generator.

Generates visually stunning share cards using Pillow.
Cards are designed for Instagram Stories (1080x1920) and feed (1080x1080).

Card types:
- Solo: user's HR curve + peak moment + event info
- Comparison: user's HR vs artist's HR (future feature)
"""

import io
import uuid
from datetime import datetime

from PIL import Image, ImageDraw, ImageFont


# Brand colors
TUMTUM_RED = (192, 57, 43)       # #C0392B
TUMTUM_RED_SEC = (231, 76, 60)   # #E74C3C
TUMTUM_DARK = (8, 8, 12)         # #08080C
TUMTUM_SURFACE = (17, 17, 24)    # #111118
TUMTUM_BORDER = (26, 26, 36)     # #1A1A24
TUMTUM_MUTED = (107, 107, 128)   # #6B6B80
TUMTUM_TEXT = (240, 240, 245)    # #F0F0F5
TUMTUM_ACCENT = (0, 210, 255)    # #00D2FF

# Card dimensions
STORY_SIZE = (1080, 1920)
FEED_SIZE = (1080, 1080)


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
    img = Image.new("RGB", size, TUMTUM_DARK)
    draw = ImageDraw.Draw(img)

    w, h = size

    # Try to load fonts, fallback to default
    try:
        font_logo = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf", 48)
        font_large = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 120)
        font_medium = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 42)
        font_small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 32)
        font_label = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 28)
    except (OSError, IOError):
        font_logo = ImageFont.load_default()
        font_large = ImageFont.load_default()
        font_medium = ImageFont.load_default()
        font_small = ImageFont.load_default()
        font_label = ImageFont.load_default()

    # Background gradient overlay
    for y in range(h):
        alpha = y / h
        r = int(TUMTUM_DARK[0] * (1 - alpha * 0.3) + TUMTUM_RED[0] * alpha * 0.15)
        g = int(TUMTUM_DARK[1] * (1 - alpha * 0.3))
        b = int(TUMTUM_DARK[2] * (1 - alpha * 0.3))
        draw.line([(0, y), (w, y)], fill=(r, g, b))

    # Logo
    draw.text((w // 2, 80), "TUMTUM", fill=TUMTUM_RED, font=font_logo, anchor="mt")

    # Event name
    y_offset = 200 if format == "story" else 160
    draw.text((w // 2, y_offset), event_name, fill=TUMTUM_TEXT, font=font_medium, anchor="mt")
    draw.text((w // 2, y_offset + 60), event_date, fill=TUMTUM_MUTED, font=font_small, anchor="mt")

    # HR mini curve (if data provided)
    if hr_data and len(hr_data) > 5:
        curve_y_start = y_offset + 140
        curve_height = 300 if format == "story" else 200
        _draw_hr_curve(draw, hr_data, 60, curve_y_start, w - 120, curve_height)

    # Peak BPM highlight
    peak_y = (h // 2) + (100 if format == "story" else 50)
    draw.text((w // 2, peak_y), str(peak_bpm), fill=TUMTUM_RED, font=font_large, anchor="mm")
    draw.text((w // 2, peak_y + 80), "BPM", fill=TUMTUM_RED_SEC, font=font_medium, anchor="mt")

    # Matched label
    if matched_label:
        draw.text(
            (w // 2, peak_y + 150),
            f"durante \"{matched_label}\"",
            fill=TUMTUM_TEXT,
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
        draw.text((x, stats_y), value, fill=TUMTUM_TEXT, font=font_medium, anchor="mt")
        draw.text((x, stats_y + 55), label, fill=TUMTUM_MUTED, font=font_label, anchor="mt")

    # User attribution
    draw.text((w // 2, h - 120), f"@{user_name}", fill=TUMTUM_MUTED, font=font_small, anchor="mt")

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
    img = Image.new("RGB", size, TUMTUM_DARK)
    draw = ImageDraw.Draw(img)

    w, h = size

    try:
        font_logo = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf", 48)
        font_large = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 80)
        font_medium = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 42)
        font_small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 32)
    except (OSError, IOError):
        font_logo = font_large = font_medium = font_small = ImageFont.load_default()

    # Background
    for y in range(h):
        alpha = y / h
        r = int(TUMTUM_DARK[0] * (1 - alpha * 0.2))
        g = int(TUMTUM_DARK[1] * (1 - alpha * 0.2))
        b = int(TUMTUM_DARK[2] * (1 - alpha * 0.2) + 20 * alpha * 0.1)
        draw.line([(0, y), (w, y)], fill=(r, g, b))

    # Logo
    draw.text((w // 2, 80), "TUMTUM", fill=TUMTUM_RED, font=font_logo, anchor="mt")

    # Event
    draw.text((w // 2, 180), event_name, fill=TUMTUM_TEXT, font=font_medium, anchor="mt")
    draw.text((w // 2, 240), event_date, fill=TUMTUM_MUTED, font=font_small, anchor="mt")

    # Sync percentage (center)
    center_y = h // 2
    draw.text((w // 2, center_y - 60), f"{sync_percentage}%", fill=TUMTUM_ACCENT, font=font_large, anchor="mm")
    draw.text((w // 2, center_y + 20), "em sincronia", fill=TUMTUM_MUTED, font=font_small, anchor="mt")

    # User vs Artist
    col_left = w // 4
    col_right = 3 * w // 4
    vs_y = center_y + 150

    draw.text((col_left, vs_y), str(user_peak_bpm), fill=TUMTUM_RED, font=font_large, anchor="mt")
    draw.text((col_left, vs_y + 90), "Seu pico", fill=TUMTUM_MUTED, font=font_small, anchor="mt")

    draw.text((col_right, vs_y), str(artist_peak_bpm), fill=TUMTUM_ACCENT, font=font_large, anchor="mt")
    draw.text((col_right, vs_y + 90), artist_name, fill=TUMTUM_MUTED, font=font_small, anchor="mt")

    draw.text((w // 2, vs_y + 40), "vs", fill=TUMTUM_MUTED, font=font_medium, anchor="mm")

    # User
    draw.text((w // 2, h - 120), f"@{user_name}", fill=TUMTUM_MUTED, font=font_small, anchor="mt")

    buffer = io.BytesIO()
    img.save(buffer, format="PNG", quality=95)
    return buffer.getvalue()


def _draw_hr_curve(
    draw: ImageDraw.ImageDraw,
    hr_data: list[dict],
    x_start: int,
    y_start: int,
    width: int,
    height: int,
) -> None:
    """Draw a simplified HR curve on the image."""
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
        # Draw filled area
        area_points = points + [(points[-1][0], y_start + height), (points[0][0], y_start + height)]
        draw.polygon(area_points, fill=(192, 57, 43, 30))

        # Draw line
        for i in range(len(points) - 1):
            draw.line([points[i], points[i + 1]], fill=TUMTUM_RED, width=3)
