"""The timeline entry response must read the JSON column, not SQLAlchemy's registry.

``EventTimeline.metadata_`` is the column; ``EventTimeline.metadata`` is the
declarative ``MetaData`` object every model carries. Until 2026-09-18 the
response schema read the latter, so the first mark ever posted from the app
came back as a 500 and the night behind it never uploaded.
"""

import datetime as dt
import importlib
import pkgutil
import uuid

import app.models as models_pkg
from app.models.event_timeline import EventTimeline
from app.schemas.event import TimelineEntryResponse

for module in pkgutil.iter_modules(models_pkg.__path__):
    importlib.import_module(f"app.models.{module.name}")


def _entry(metadata: dict | None) -> EventTimeline:
    return EventTimeline(
        id=uuid.uuid4(),
        event_id=uuid.uuid4(),
        timestamp=dt.datetime(2026, 9, 18, 17, 51, tzinfo=dt.UTC),
        label="GOL",
        entry_type="goal",
        metadata_=metadata,
    )


def test_response_reads_the_column_not_the_registry():
    response = TimelineEntryResponse.model_validate(_entry(None))
    assert response.label == "GOL"
    assert response.metadata is None


def test_response_carries_the_stored_metadata():
    response = TimelineEntryResponse.model_validate(_entry({"minute": 73}))
    assert response.metadata == {"minute": 73}
    assert response.model_dump()["metadata"] == {"minute": 73}
