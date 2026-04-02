"""Celery tasks for async card generation.

For heavy card generation (e.g., video cards), we offload to Celery workers.
The MVP uses synchronous generation in the API endpoint, but this module
is ready for when we need async processing.
"""

from celery import Celery

from app.config import settings

celery_app = Celery("tumtum", broker=settings.redis_url, backend=settings.redis_url)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
    task_track_started=True,
    task_time_limit=120,  # 2 min max per task
)


@celery_app.task(name="generate_card_async")
def generate_card_async(card_id: str, card_type: str, params: dict) -> dict:
    """Async card generation task.

    This task is dispatched when generating resource-intensive cards
    (e.g., video cards, comparison cards with dual HR curves).

    For Phase 0 MVP, card generation happens synchronously in the API.
    This task will be activated in Phase 1 when we add video generation.
    """
    # Import here to avoid circular imports in Celery worker
    from app.services.card_generator import generate_solo_card, generate_comparison_card

    if card_type == "solo":
        image_bytes = generate_solo_card(**params)
    elif card_type == "comparison":
        image_bytes = generate_comparison_card(**params)
    else:
        return {"status": "failed", "error": f"Unknown card type: {card_type}"}

    # In production: upload to R2 and return URL
    return {
        "status": "ready",
        "card_id": card_id,
        "image_size": len(image_bytes),
    }
