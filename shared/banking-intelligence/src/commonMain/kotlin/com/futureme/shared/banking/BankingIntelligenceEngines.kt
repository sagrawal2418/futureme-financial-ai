package com.futureme.shared.banking

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.models.ActionDifficulty
import com.futureme.shared.models.BankingVisionDemo
import com.futureme.shared.models.BankingVisionStep
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.DecisionJournalEntry
import com.futureme.shared.models.DecisionOutcomeStatus
import com.futureme.shared.models.ExplainabilityFactor
import com.futureme.shared.models.FinancialExplainability
import com.futureme.shared.models.FinancialGpsResult
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.FutureOutcomeContribution
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.ImpactDimension
import com.futureme.shared.models.ImpactSentiment
import com.futureme.shared.models.InvestmentAccount
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.LifeEventPlan
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.MoneyLeak
import com.futureme.shared.models.MoneyLeakType
import com.futureme.shared.models.MonthlyFinancialReview
import com.futureme.shared.models.NextBestAction
import com.futureme.shared.models.OpportunityRecommendation
import com.futureme.shared.models.OpportunitySource
import com.futureme.shared.models.ScenarioImpactCell
import com.futureme.shared.models.ScenarioImpactHeatmap
import com.futureme.shared.models.ScenarioResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class OpportunityRankingEngine {
    fun rank(
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
        moneyLeaks: List<MoneyLeak>,
        investmentAccounts: List<InvestmentAccount>,
        goals: List<GoalProbabilityResult>,
        readiness: List<LifeReadinessResult>,
        financialGps: FinancialGpsResult,
        lifeEvents: List<LifeEventPlan>,
        scenarioResults: List<ScenarioResult>,
    ): List<OpportunityRecommendation> {
        val candidates = buildList {
            moneyLeaks.forEach { leak ->
                val scenarioId = when (leak.type) {
                    MoneyLeakType.HIGH_INTEREST_DEBT -> "pay-off-cards"
                    MoneyLeakType.REFINANCE_OPPORTUNITY -> "refinance-now"
                    MoneyLeakType.MISSED_EMPLOYER_MATCH -> "invest-more"
                    else -> null
                }
                val scenarioBenefit = scenarioId
                    ?.let { id -> scenarioResults.firstOrNull { it.scenario.id == id } }
                    ?.netWorthDelta5Years
                    ?.coerceAtLeast(0.0)
                    ?: 0.0
                add(
                    Candidate(
                        id = "opportunity-${leak.id}",
                        title = actionTitle(leak),
                        description = leak.fixRecommendation,
                        source = when (leak.type) {
                            MoneyLeakType.HIGH_INTEREST_DEBT -> OpportunitySource.DEBT
                            MoneyLeakType.MISSED_EMPLOYER_MATCH -> OpportunitySource.INVESTMENT
                            else -> OpportunitySource.MONEY_LEAK
                        },
                        effortScore = effort(leak.difficulty),
                        confidenceScore = confidence(leak.type),
                        annualBenefit = leak.estimatedAnnualLoss,
                        fiveYearBenefit = max(leak.estimatedFiveYearLoss, scenarioBenefit),
                        monthlyCommitment = when (leak.type) {
                            MoneyLeakType.HIGH_INTEREST_DEBT -> 300.0
                            MoneyLeakType.MISSED_EMPLOYER_MATCH ->
                                employerMatchGap(profile, investmentAccounts)
                            else -> leak.estimatedMonthlyLoss
                        },
                        relatedScenarioId = scenarioId,
                    ),
                )
            }

            goals.sortedBy { it.probabilityPercentage }.take(2).forEach { goal ->
                val annual = goal.requiredMonthlyImprovement * 12.0
                add(
                    Candidate(
                        id = "opportunity-${goal.id}",
                        title = "Close the gap for ${goal.title.lowercase()}",
                        description = goal.recommendedActions.firstOrNull()
                            ?: "Fund the next milestone before adding another commitment.",
                        source = OpportunitySource.GOAL,
                        effortScore = 62,
                        confidenceScore = 78,
                        annualBenefit = annual,
                        fiveYearBenefit = annual * 2.5,
                        monthlyCommitment = goal.requiredMonthlyImprovement,
                        relatedScenarioId = scenarioForGoal(goal.id),
                    ),
                )
            }

            readiness.sortedBy { it.readinessScore }.take(1).forEach { result ->
                val scoreGap = (80 - result.readinessScore).coerceAtLeast(0)
                add(
                    Candidate(
                        id = "opportunity-${result.id}",
                        title = "Strengthen ${result.title}",
                        description = result.recommendedActions.firstOrNull()
                            ?: "Address the leading readiness blocker.",
                        source = OpportunitySource.READINESS,
                        effortScore = 58,
                        confidenceScore = when (result.confidenceLevel.name) {
                            "HIGH" -> 88
                            "MEDIUM" -> 74
                            else -> 60
                        },
                        annualBenefit = scoreGap * 450.0,
                        fiveYearBenefit = scoreGap * 1_100.0,
                        monthlyCommitment = scoreGap * 40.0,
                    ),
                )
            }

            add(
                Candidate(
                    id = "opportunity-financial-gps",
                    title = "Follow the Financial GPS action plan",
                    description = financialGps.monthlyActionPlan.firstOrNull()
                        ?: "Direct the monthly surplus to the highest-value action.",
                    source = OpportunitySource.FINANCIAL_GPS,
                    effortScore = 48,
                    confidenceScore = when (financialGps.confidenceLevel.name) {
                        "HIGH" -> 88
                        "MEDIUM" -> 74
                        else -> 60
                    },
                    annualBenefit = financialGps.difference / 5.0,
                    fiveYearBenefit = financialGps.difference,
                    monthlyCommitment = dashboard.monthlyCashFlow.coerceAtLeast(0.0) * 0.25,
                    relatedScenarioId = "pay-off-cards",
                ),
            )

            lifeEvents.maxByOrNull { it.riskImpact }?.let { event ->
                val midpoint = (event.oneTimeCostLow + event.oneTimeCostHigh) / 2.0
                add(
                    Candidate(
                        id = "opportunity-${event.id}",
                        title = "Prepare for ${event.title.lowercase()}",
                        description = event.recommendedPreparationSteps.firstOrNull()
                            ?: "Fund the event reserve before committing.",
                        source = OpportunitySource.LIFE_EVENT,
                        effortScore = 65,
                        confidenceScore = 72,
                        annualBenefit = midpoint / 3.0,
                        fiveYearBenefit = midpoint,
                        monthlyCommitment = event.estimatedMonthlyImpact.coerceAtLeast(0.0),
                        relatedScenarioId = event.suggestedScenarioIds.firstOrNull(),
                    ),
                )
            }
        }

        val maxBenefit = candidates.maxOfOrNull { it.fiveYearBenefit }?.coerceAtLeast(1.0) ?: 1.0
        return candidates
            .map { candidate ->
                val impact = (candidate.fiveYearBenefit / maxBenefit * 100.0)
                    .roundToInt()
                    .coerceIn(10, 100)
                ScoredCandidate(
                    candidate = candidate,
                    impactScore = impact,
                    priorityScore = impact * 0.60 +
                        candidate.confidenceScore * 0.25 +
                        (100 - candidate.effortScore) * 0.15,
                )
            }
            .sortedByDescending { it.priorityScore }
            .mapIndexed { index, scored ->
                val candidate = scored.candidate
                OpportunityRecommendation(
                    id = candidate.id,
                    title = candidate.title,
                    description = candidate.description,
                    source = candidate.source,
                    impactScore = scored.impactScore,
                    effortScore = candidate.effortScore,
                    confidenceScore = candidate.confidenceScore,
                    annualBenefitEstimate = candidate.annualBenefit,
                    fiveYearBenefitEstimate = candidate.fiveYearBenefit,
                    priorityRanking = index + 1,
                    monthlyCommitment = candidate.monthlyCommitment,
                    relatedScenarioId = candidate.relatedScenarioId,
                )
            }
    }

    fun nextBestAction(recommendations: List<OpportunityRecommendation>): NextBestAction {
        val top = requireNotNull(recommendations.firstOrNull()) {
            "At least one recommendation is required."
        }
        val monthlyPhrase = if (top.monthlyCommitment > 0.0) {
            "${top.monthlyCommitment.asDollars()}/month"
        } else {
            "one focused action"
        }
        return NextBestAction(
            recommendationId = top.id,
            title = top.title,
            description = top.description,
            callout = "Committing $monthlyPhrase may improve your five-year outlook by " +
                "${top.fiveYearBenefitEstimate.asDollars()}.",
            monthlyCommitment = top.monthlyCommitment,
            fiveYearImpact = top.fiveYearBenefitEstimate,
            impactScore = top.impactScore,
            confidenceScore = top.confidenceScore,
        )
    }

    private fun actionTitle(leak: MoneyLeak): String = when (leak.type) {
        MoneyLeakType.HIGH_INTEREST_DEBT -> "Pay off high-interest debt"
        MoneyLeakType.MISSED_EMPLOYER_MATCH -> "Capture the full employer match"
        MoneyLeakType.REFINANCE_OPPORTUNITY -> "Evaluate a mortgage refinance"
        MoneyLeakType.HIGH_SUBSCRIPTION_SPEND -> "Reduce subscription spending"
        MoneyLeakType.EXCESS_CHECKING_CASH -> "Move excess checking cash"
        MoneyLeakType.INSURANCE_OVERPAYMENT -> "Re-shop insurance coverage"
    }

    private fun effort(difficulty: ActionDifficulty): Int = when (difficulty) {
        ActionDifficulty.EASY -> 20
        ActionDifficulty.MODERATE -> 50
        ActionDifficulty.INVOLVED -> 78
    }

    private fun confidence(type: MoneyLeakType): Int = when (type) {
        MoneyLeakType.HIGH_INTEREST_DEBT,
        MoneyLeakType.MISSED_EMPLOYER_MATCH,
        MoneyLeakType.HIGH_SUBSCRIPTION_SPEND,
        -> 94
        MoneyLeakType.EXCESS_CHECKING_CASH -> 88
        MoneyLeakType.INSURANCE_OVERPAYMENT -> 76
        MoneyLeakType.REFINANCE_OPPORTUNITY -> 70
    }

    private fun employerMatchGap(
        profile: FinancialProfile,
        accounts: List<InvestmentAccount>,
    ): Double {
        val largestGap = accounts.maxOfOrNull {
            (it.employerMatchPercent - it.employeeContributionPercent).coerceAtLeast(0.0)
        } ?: 0.0
        return profile.annualGrossIncome / 12.0 * largestGap
    }

    private fun scenarioForGoal(goalId: String): String? = when (goalId) {
        "goal-home" -> "wait-to-buy"
        "goal-debt" -> "pay-off-cards"
        "goal-child" -> "have-a-child"
        "goal-move" -> "move-to-texas"
        "goal-retire" -> "invest-more"
        else -> null
    }

    private data class Candidate(
        val id: String,
        val title: String,
        val description: String,
        val source: OpportunitySource,
        val effortScore: Int,
        val confidenceScore: Int,
        val annualBenefit: Double,
        val fiveYearBenefit: Double,
        val monthlyCommitment: Double,
        val relatedScenarioId: String? = null,
    )

    private data class ScoredCandidate(
        val candidate: Candidate,
        val impactScore: Int,
        val priorityScore: Double,
    )
}

class FinancialExplainabilityEngine {
    fun explain(
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
        financialGps: FinancialGpsResult,
    ): FinancialExplainability {
        val factors = buildList {
            val surplusImpact = when {
                dashboard.monthlyCashFlow >= 2_000.0 -> 3
                dashboard.monthlyCashFlow >= 0.0 -> 1
                else -> -5
            }
            add(
                factor(
                    "monthly-surplus",
                    "Monthly surplus",
                    "${dashboard.monthlyCashFlow.asDollars()} remains available after core obligations.",
                    surplusImpact,
                ),
            )

            val reserveImpact = when {
                dashboard.emergencyFundMonths >= 12.0 -> 3
                dashboard.emergencyFundMonths >= 6.0 -> 1
                else -> -4
            }
            add(
                factor(
                    "emergency-fund",
                    "Emergency fund",
                    "${oneDecimal(dashboard.emergencyFundMonths)} months of essential expenses are covered.",
                    reserveImpact,
                ),
            )

            val debtImpact = when {
                profile.creditCardDebt <= 0.0 -> 4
                profile.creditCardDebt > profile.monthlyNetIncome -> -4
                else -> -2
            }
            add(
                factor(
                    "debt-utilization",
                    "High-interest debt",
                    "${profile.creditCardDebt.asDollars()} still carries a ${oneDecimal(profile.creditCardApr * 100.0)}% APR.",
                    debtImpact,
                ),
            )

            val retirementRate = profile.monthlyRetirementContribution / profile.monthlyNetIncome
            val retirementImpact = if (retirementRate >= 0.10) 2 else -2
            add(
                factor(
                    "retirement-saving",
                    "Retirement saving",
                    "${profile.monthlyRetirementContribution.asDollars()} is invested each month.",
                    retirementImpact,
                ),
            )

            add(
                factor(
                    "gps-opportunity",
                    "Action-plan upside",
                    "The current GPS plan identifies ${financialGps.difference.asDollars()} of five-year upside.",
                    1,
                ),
            )
        }
        val netChange = factors.sumOf { it.pointImpact }
        val previous = (dashboard.healthScore.value - netChange).coerceIn(0, 100)
        return FinancialExplainability(
            previousScore = previous,
            currentScore = dashboard.healthScore.value,
            netChange = dashboard.healthScore.value - previous,
            factors = factors,
            summary = if (netChange >= 0) {
                "Cash flow and reserves improved the score, while high-interest debt remains the main drag."
            } else {
                "Debt and liquidity pressure outweighed this month's positive contributions."
            },
        )
    }

    private fun factor(
        id: String,
        title: String,
        description: String,
        impact: Int,
    ): ExplainabilityFactor = ExplainabilityFactor(
        id = id,
        title = title,
        description = description,
        pointImpact = impact,
        sentiment = when {
            impact > 0 -> ImpactSentiment.POSITIVE
            impact < 0 -> ImpactSentiment.NEGATIVE
            else -> ImpactSentiment.NEUTRAL
        },
    )
}

class ScenarioImpactHeatmapEngine {
    fun buildAll(
        dashboard: DashboardSnapshot,
        results: List<ScenarioResult>,
        simulations: List<LifeDecisionSimulation>,
    ): List<ScenarioImpactHeatmap> = results.map { result ->
        val simulation = simulations.first { it.scenarioId == result.scenario.id }
        val scenario = result.scenario
        val debtScore = (
            (-scenario.debtDelta / 250.0) -
                (scenario.mortgageDelta / 5_000.0)
        ).roundToInt().coerceIn(-100, 100)
        val cells = listOf(
            cell(
                ImpactDimension.CASH_FLOW,
                (result.monthlyCashFlowImpact / 20.0).roundToInt(),
                result.monthlyCashFlowImpact.asSignedDollars() + "/mo",
            ),
            cell(
                ImpactDimension.DEBT,
                debtScore,
                when {
                    scenario.debtDelta < 0.0 -> "${abs(scenario.debtDelta).asDollars()} reduced"
                    scenario.mortgageDelta > 0.0 -> "${scenario.mortgageDelta.asDollars()} added"
                    else -> "No material change"
                },
            ),
            cell(
                ImpactDimension.EMERGENCY_FUND,
                ((result.emergencyFundMonths - dashboard.emergencyFundMonths) * 10.0).roundToInt(),
                "${oneDecimal(result.emergencyFundMonths)} months",
            ),
            cell(
                ImpactDimension.RETIREMENT,
                (
                    scenario.monthlyInvestmentDelta / 20.0 +
                        result.netWorthDelta5Years / 5_000.0
                ).roundToInt(),
                result.netWorthDelta5Years.asSignedDollars() + " at 5 years",
            ),
            cell(
                ImpactDimension.READINESS,
                simulation.readinessImpact * 5,
                simulation.readinessImpact.asSignedPoints(),
            ),
            cell(
                ImpactDimension.RISK,
                -simulation.riskChange * 4,
                simulation.riskChange.asSignedPoints(),
            ),
        )
        ScenarioImpactHeatmap(
            scenarioId = scenario.id,
            title = scenario.title,
            cells = cells,
        )
    }

    private fun cell(
        dimension: ImpactDimension,
        score: Int,
        label: String,
    ): ScenarioImpactCell {
        val normalized = score.coerceIn(-100, 100)
        return ScenarioImpactCell(
            dimension = dimension,
            sentiment = when {
                normalized >= 4 -> ImpactSentiment.POSITIVE
                normalized <= -4 -> ImpactSentiment.NEGATIVE
                else -> ImpactSentiment.NEUTRAL
            },
            score = normalized,
            label = label,
        )
    }
}

class MonthlyFinancialReviewEngine {
    fun generateHistory(
        dashboard: DashboardSnapshot,
        opportunities: List<OpportunityRecommendation>,
        readiness: List<LifeReadinessResult>,
        goals: List<GoalProbabilityResult>,
    ): List<MonthlyFinancialReview> {
        val top = opportunities.first()
        val weakest = readiness.minBy { it.readinessScore }
        return listOf(
            MonthlyFinancialReview(
                id = "review-2026-06",
                month = "2026-06",
                label = "June 2026",
                generatedDate = "2026-06-11",
                wins = listOf(
                    "Monthly cash flow remains positive at ${dashboard.monthlyCashFlow.asDollars()}.",
                    "Emergency reserves cover ${oneDecimal(dashboard.emergencyFundMonths)} months.",
                ),
                risks = listOf(
                    dashboard.alerts.firstOrNull() ?: "High-interest debt remains the main risk.",
                    "${weakest.title} is the lowest readiness category at ${weakest.readinessScore}%.",
                ),
                opportunities = opportunities.take(3).map {
                    "${it.title}: ${it.fiveYearBenefitEstimate.asDollars()} modeled five-year benefit."
                },
                recommendedActions = opportunities.take(3).map { it.title },
                readinessChanges = readiness.take(3).map {
                    "${it.title}: ${it.trend.name.lowercase()} (${it.trendDelta.asSignedPoints()})."
                },
                goalProgress = goals.take(3).map {
                    "${it.title}: ${it.probabilityPercentage}% probability."
                },
                aiSummary = "${top.title} is the highest-impact move this month. " +
                    "It improves the modeled outlook while addressing the household's clearest constraint.",
            ),
            MonthlyFinancialReview(
                id = "review-2026-05",
                month = "2026-05",
                label = "May 2026",
                generatedDate = "2026-05-31",
                wins = listOf(
                    "Automated retirement contributions stayed on schedule.",
                    "The household avoided adding new revolving debt.",
                ),
                risks = listOf("Subscription and insurance costs remained above the planning benchmark."),
                opportunities = listOf("Capture the full employer match before increasing taxable investing."),
                recommendedActions = listOf(
                    "Increase the 401(k) contribution to the full match.",
                    "Review recurring subscriptions.",
                ),
                readinessChanges = listOf("Retirement readiness improved by 1 point."),
                goalProgress = listOf("Debt payoff probability improved to 78%."),
                aiSummary = "May protected momentum. The next gain comes from redirecting existing cash, not adding complexity.",
            ),
            MonthlyFinancialReview(
                id = "review-2026-04",
                month = "2026-04",
                label = "April 2026",
                generatedDate = "2026-04-30",
                wins = listOf("The emergency reserve moved above the 12-month planning threshold."),
                risks = listOf("Credit-card APR remained above 20%."),
                opportunities = listOf("Accelerating debt payoff would free $850 per month."),
                recommendedActions = listOf("Add $300 to the monthly card payment."),
                readinessChanges = listOf("Home purchase readiness improved by 2 points."),
                goalProgress = listOf("Emergency-fund goal reached its target range."),
                aiSummary = "April strengthened resilience. High-interest debt is now the most valuable constraint to remove.",
            ),
        )
    }
}

class DecisionJournalEngine {
    fun seedEntries(): List<DecisionJournalEntry> = listOf(
        DecisionJournalEntry(
            id = "decision-401k",
            type = "Investment increase",
            title = "Increased 401(k) contribution",
            decisionDate = "2026-03-01",
            expectedMonthlyImpact = -250.0,
            actualMonthlyImpact = -250.0,
            expectedFiveYearImpact = 17_400.0,
            actualFiveYearImpact = 18_100.0,
            status = DecisionOutcomeStatus.AHEAD,
            notes = "Contribution increase was automated and the employer match was captured.",
            relatedScenarioId = "invest-more",
        ),
        DecisionJournalEntry(
            id = "decision-subscriptions",
            type = "Spending reduction",
            title = "Reduced recurring subscriptions",
            decisionDate = "2026-04-12",
            expectedMonthlyImpact = 90.0,
            actualMonthlyImpact = 74.0,
            expectedFiveYearImpact = 5_400.0,
            actualFiveYearImpact = 4_440.0,
            status = DecisionOutcomeStatus.BEHIND,
            notes = "Most duplicate services were removed; two annual plans remain.",
        ),
        DecisionJournalEntry(
            id = "decision-emergency",
            type = "Emergency fund",
            title = "Added to emergency savings",
            decisionDate = "2026-05-15",
            expectedMonthlyImpact = -500.0,
            actualMonthlyImpact = -500.0,
            expectedFiveYearImpact = 3_200.0,
            actualFiveYearImpact = 3_350.0,
            status = DecisionOutcomeStatus.ON_TRACK,
            notes = "The transfer now runs automatically after each payday.",
        ),
    )

    fun fromScenario(
        result: ScenarioResult,
        sequence: Int,
    ): DecisionJournalEntry = DecisionJournalEntry(
        id = "decision-scenario-$sequence",
        type = result.scenario.type.name.lowercase().replace('_', ' '),
        title = result.scenario.title,
        decisionDate = "2026-06-11",
        expectedMonthlyImpact = result.monthlyCashFlowImpact,
        expectedFiveYearImpact = result.netWorthDelta5Years,
        status = DecisionOutcomeStatus.PLANNED,
        notes = "Saved from the Life Decision Simulator for outcome tracking.",
        relatedScenarioId = result.scenario.id,
    )
}

class FutureOutcomeEngine {
    fun calculate(
        journal: List<DecisionJournalEntry>,
        opportunities: List<OpportunityRecommendation>,
    ): List<FutureOutcomeContribution> {
        val entries = journal.map { entry ->
            Triple(
                entry.id,
                entry.title,
                entry.actualFiveYearImpact ?: entry.expectedFiveYearImpact,
            )
        } + opportunities.take(2).map {
            Triple(it.id, it.title, it.fiveYearBenefitEstimate)
        }
        val positive = entries.filter { it.third > 0.0 }.sortedByDescending { it.third }.take(5)
        val total = positive.sumOf { it.third }.coerceAtLeast(1.0)
        return positive.map { (id, title, contribution) ->
            FutureOutcomeContribution(
                id = "contribution-$id",
                title = title,
                description = "Estimated contribution to future net worth based on the modeled or tracked result.",
                fiveYearContribution = contribution,
                sharePercentage = (contribution / total * 100.0).roundToInt(),
                source = sourceFor(title),
            )
        }
    }

    private fun sourceFor(title: String): OpportunitySource = when {
        "debt" in title.lowercase() -> OpportunitySource.DEBT
        "401" in title || "investment" in title.lowercase() -> OpportunitySource.INVESTMENT
        else -> OpportunitySource.MONEY_LEAK
    }
}

fun bankingVisionDemo(): BankingVisionDemo = BankingVisionDemo(
    title = "FutureMe Banking Vision",
    subtitle = "From transaction history to continuous financial decision intelligence.",
    audiences = listOf(
        "Portfolio reviews",
        "Leadership presentations",
        "Innovation showcases",
        "Patent discussions",
    ),
    steps = listOf(
        BankingVisionStep(1, "Life readiness", "See readiness for major life decisions.", "Readiness dashboard"),
        BankingVisionStep(2, "Financial GPS", "Compare the current path with an improved path.", "Five-year trajectory"),
        BankingVisionStep(3, "Money leak detection", "Find avoidable losses across the household.", "Detected leaks"),
        BankingVisionStep(4, "AI coaching", "Turn the household context into a focused strategy.", "AI coach"),
        BankingVisionStep(5, "Opportunity ranking", "Rank every recommendation by impact, effort, and confidence.", "Ranked actions"),
        BankingVisionStep(6, "Next best action", "Answer what the household should do this month.", "Highest-impact action"),
        BankingVisionStep(7, "Monthly review", "Explain progress, risks, and what changes next.", "Monthly review"),
    ),
)

private fun Double.asDollars(): String {
    val whole = abs(roundToInt())
    val grouped = whole.toString().reversed().chunked(3).joinToString(",").reversed()
    return (if (this < 0.0) "-$" else "$") + grouped
}

private fun Double.asSignedDollars(): String =
    (if (this >= 0.0) "+" else "-") + abs(this).asDollars()

private fun Int.asSignedPoints(): String = when {
    this > 0 -> "+$this points"
    this < 0 -> "$this points"
    else -> "No change"
}

private fun oneDecimal(value: Double): String =
    ((value * 10.0).roundToInt() / 10.0).toString()
