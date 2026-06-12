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
    "highest impact action": ("highest impact action",),
    "ranked opportunities": ("ranked opportunities",),
    "financial health explainability": ("why my score changed",),
    "scenario impact heatmap": ("scenario impact heatmap",),
    "monthly financial review": ("monthly financial review",),
    "decision journal": ("decision journal",),
    "future outcome attribution": ("what improved my future",),
    "life readiness dashboard": ("life readiness dashboard",),
    "readiness improvement plan": ("readiness improvement plan",),
    "life decision simulator": ("life decision simulator", "readiness impact"),
    "life timeline": ("life timeline",),
    "ai coach": ("ai coach",),
    "executive banking demo": ("executive banking demo",),
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
}


class ClientFeatureParityTest(unittest.TestCase):
    def test_every_client_exposes_the_product_capability_contract(self) -> None:
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
