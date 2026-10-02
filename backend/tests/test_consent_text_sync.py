"""One consent text, three codebases (02/10).

On 02/10 the app and the site showed different sentences — different names
for the same purpose, a different rule for "A galera" — under the same text
version, and a consent row's version must point at exactly one text. The
words now live once, in `shared/consent/consent-text.json`;
`scripts/consent_text.py` writes them into the Android strings, the site's
TypeScript and this server's `consent_text.py`. This test is what keeps
them from drifting again: it regenerates in memory and compares.
"""

import importlib.util
from pathlib import Path

from app.services import consent_text, consents

ROOT = Path(__file__).resolve().parents[2]


def _script():
    path = ROOT / "scripts" / "consent_text.py"
    spec = importlib.util.spec_from_file_location("consent_text_script", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def test_every_codebase_carries_the_same_consent_text():
    problems = _script().drift()
    assert problems == [], "\n".join(problems)


def test_the_server_version_is_the_generated_one():
    assert consents.CONSENT_TEXT_VERSION == consent_text.CONSENT_TEXT_VERSION
    assert tuple(consents.PURPOSES) == consent_text.PURPOSES


def test_every_purpose_has_its_sentences_in_both_languages():
    for lang in ("pt-BR", "en"):
        block = consent_text.CONSENT_TEXT[lang]
        assert len(block["facts"]) == 6
        for purpose in consent_text.PURPOSES:
            text = block["purposes"][purpose]
            assert len(text["title"]) > 3
            assert len(text["body"]) > 20
            # The standing state, as every switch says it (02/10, item 11).
            assert text["off"].startswith("Desligado." if lang == "pt-BR" else "Off.")
    pt = consent_text.CONSENT_TEXT["pt-BR"]
    # The promise the keeping of a night rests on since 02/10: automatic, revocable.
    assert "assim que termina" in pt["purposes"]["keep_night"]["body"]
    assert "em até 24 horas" in pt["purposes"]["keep_night"]["off"]
    assert "apague a conta" in pt["purposes"]["terms"]["locked"]
    facts = " ".join(pt["facts"])
    assert "30 minutos" in facts and "7 dias" in facts and "não é um dispositivo médico" in facts
