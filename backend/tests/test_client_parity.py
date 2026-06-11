from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]

CLIENT_SOURCES = {
    "android": (
        ROOT / "apps/android/src/main/kotlin/com/futureme/financialai"
    ).rglob("*.kt"),
    "ios": (ROOT / "apps/ios/FutureMeFinancial").glob("*.swift"),
    "web": (ROOT / "apps/web/src").rglob("*.tsx"),
}

CAPABILITY_MARKERS = {
    "proactive insights": ("proactive insights",),
    "financial gps trajectory": (
        "financial gps",
        "current trajectory",
        "improved trajectory",
    ),
    "goal readiness detail": (
        "goal readiness",
        "blockers",
        "recommended actions",
    ),
    "life-event planning": ("life event planner", "plan this event"),
    "money-leak detector": ("money leak", "five-year impact"),
    "scenario lab": ("scenario", "simulate"),
    "risk explanation": ("risk score", "risk"),
    "dynamic comparison": ("option a", "option b", "compare"),
    "assistant": ("ask futureme",),
}


class ClientFeatureParityTest(unittest.TestCase):
    def test_every_client_exposes_the_version_two_capability_contract(self) -> None:
        for client, paths in CLIENT_SOURCES.items():
            source = "\n".join(
                path.read_text(encoding="utf-8")
                for path in sorted(paths)
            ).lower()
            for capability, markers in CAPABILITY_MARKERS.items():
                with self.subTest(client=client, capability=capability):
                    for marker in markers:
                        if marker not in source:
                            self.fail(
                                f"{client} is missing the '{marker}' marker "
                                f"for {capability}"
                            )


if __name__ == "__main__":
    unittest.main()
