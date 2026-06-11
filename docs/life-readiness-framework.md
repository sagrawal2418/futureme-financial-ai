# Life Readiness Framework

FutureMe Financial answers one question: “How ready am I for my next life decision?”

The framework converts the household’s shared financial profile into deterministic, decision-specific readiness results. Android, iOS, web, and the AI Coach consume the same output from `LifeReadinessEngine`.

## Readiness Categories

- Home Purchase Readiness
- Child Readiness
- Relocation Readiness
- Retirement Readiness
- Business Startup Readiness
- Parent Support Readiness
- Education Funding Readiness

Every result contains a 0-100 score, readiness level, strengths, weaknesses, blockers, confidence level, recommended actions, trend, estimated months to readiness, and projected ready date.

## Readiness Levels

| Score | Level | Meaning |
| --- | --- | --- |
| 0-39 | Not Ready | The modeled commitment would create an unacceptable cash-flow, reserve, debt, or risk gap. |
| 40-59 | Needs Preparation | The decision may become supportable after material preparation. |
| 60-79 | Almost Ready | The household has a viable foundation but should close named blockers first. |
| 80-100 | Ready | The current deterministic model supports the decision. |

A score is a planning signal, not an approval, guarantee, or substitute for professional advice.

## Scoring

The engine derives reusable factors from the canonical financial profile:

- Monthly cash-flow surplus
- Essential-expense emergency runway
- Liquid savings
- Credit-card debt pressure
- Single- or dual-income resilience
- Retirement contribution and investment progress
- Net-worth progress

Each category applies a different weighted combination. For example, home readiness emphasizes liquid savings, cash flow, reserves, and debt resilience. Business readiness places more weight on an 18-month runway and startup capital. Retirement readiness emphasizes invested assets and contribution pace.

Factors are normalized to 0-100 against explicit planning targets, combined by category weights, rounded, and constrained to 0-100. Financial values and scores are calculated in shared Kotlin code. Presentation clients do not recreate formulas.

## Strengths, Weaknesses, and Blockers

Strengths describe modeled capacity already present, such as positive cash flow, a six-month reserve, two incomes, or an established investment base.

Weaknesses identify conditions that reduce flexibility but may not prevent the decision. Blockers identify prerequisites that should be resolved before commitment, such as an incomplete closing reserve, insufficient childcare capacity, unconfirmed relocation compensation, or inadequate business runway.

Recommended actions are category-specific and ordered for practical execution.

## Trends and Ready Dates

The trend compares today’s score with the score from a deterministic six-month projected profile. The projection allocates portions of positive surplus to savings, card payoff, and investing.

- Improving: projected gain of at least two points
- Stable: projected change between minus one and plus one
- Declining: projected loss of at least two points

The projected ready date estimates when the score can reach 80 using a category-specific monthly score-gain assumption. Improvement plans can use a higher target, 85 by default.

## Improvement Plans

An improvement plan includes:

- Current score and target score
- Score gap
- Recommended action sequence
- Modeled monthly commitment
- Estimated timeline in months
- Projected target date

The plan remains deterministic and traceable to the readiness result. It does not ask the language model to invent a target or timeline.

## Life Decision Simulation

The Life Decision Simulator combines a scenario result with the relevant readiness category. It reports:

- Readiness before, after, and point impact
- Monthly cash-flow impact
- Five-year net-worth impact
- Risk before, after, and change
- Timeline change in months
- Summary and recommended actions

Supported examples include buying a home, relocating, losing a job, having another child, a spouse stopping work, increasing investments, paying off debt, refinancing, and starting a business.

## Confidence Levels

Confidence describes the quality and stability of the assumptions, not the probability that an event will succeed.

- High: the model is primarily driven by current balances, recurring cash flow, and bounded near-term costs.
- Medium: the model depends more on long-range returns, care costs, or future contribution behavior.
- Low: the model depends on uncertain external outcomes, such as startup revenue and operating costs.

Confidence should fall when required inputs are missing, stale, or highly variable. Future versions can add data freshness and assumption-quality metadata without changing the client contract.

## Timeline

The Life Timeline displays today, six months, one year, three years, and five years. Each point includes:

- Net worth
- Readiness scores
- Completed goals
- Debt balance
- Investment balance

All horizons use the same deterministic profile and projection assumptions as the rest of the product.

## AI Coaching Philosophy

The FutureMe AI Coach behaves as a financial strategist, not a general chatbot.

1. Calculators calculate; AI explains.
2. The coach receives structured readiness, improvement-plan, scenario, and risk output.
3. It identifies blockers, tradeoffs, and the highest-leverage next action.
4. It never replaces shared balances, scores, projections, or dates with invented arithmetic.
5. It uses calm, non-judgmental language and makes uncertainty visible.
6. It keeps recommendations educational and does not present them as financial, tax, legal, or investment advice.

Typical questions include:

- What is preventing me from buying a home?
- How can I become ready for another child?
- What decision improves my future the most?
- What is my weakest readiness category?
- What should I focus on this month?
