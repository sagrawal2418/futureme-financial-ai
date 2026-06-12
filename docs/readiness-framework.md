# Mission Readiness Framework

Every mission receives one 0-100 readiness score supported by six visible dimensions.

## Dimensions

| Dimension | Source |
| --- | --- |
| Financial Readiness | Shared financial health score |
| Cash Flow Readiness | Monthly surplus relative to the mission need |
| Risk Readiness | Inverse of the shared household risk score |
| Emergency Fund Readiness | Liquid runway relative to the mission reserve target |
| Debt Readiness | Revolving-debt pressure relative to monthly income |
| Goal Readiness | Linked goal probability or life-readiness result |

The mission score anchors to the linked Life Readiness or Goal Probability result, then incorporates the six-factor average. Scores are constrained to 0-100 and calculated only in shared Kotlin.

## Mission-Specific Targets

Cash-flow and reserve targets vary by mission. A business mission uses an 18-month runway; the emergency-fund mission uses 12 months; other missions use a six-month planning floor. Child, parent-support, and business missions also use higher mission-specific cash-flow targets.

## Explanation

Factors at or above 75 become strengths. Factors below 60 become weaknesses. Factors below 45 can become blockers. Existing blockers from Life Readiness and Goal Probability are preserved and deduplicated.

## Confidence

Confidence comes from the linked Life Readiness result when available. Missions without a linked readiness category use medium confidence until richer inputs are available.

## Boundaries

Readiness is an educational planning signal. It is not approval, eligibility, underwriting, tax advice, legal advice, or a guarantee that a life event will succeed.
