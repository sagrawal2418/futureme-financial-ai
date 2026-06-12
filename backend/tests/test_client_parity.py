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
    "five-tab navigation": ("home", "missions", "insights", "coach", "profile"),
    "mission execution": ("missionexecution",),
    "financial explainability": ("financialexplainability",),
    "money-leak intelligence": ("moneyleaks",),
    "ai quality evaluation": ("aievaluationdashboard",),
    "realistic personas": ("customerpersonas",),
    "executive demo story": ("executivedemostory",),
}


class ClientFeatureParityTest(unittest.TestCase):
    def test_every_client_exposes_the_simplified_product_contract(self) -> None:
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
