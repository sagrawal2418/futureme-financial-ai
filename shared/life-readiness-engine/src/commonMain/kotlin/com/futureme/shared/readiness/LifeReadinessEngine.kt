package com.futureme.shared.readiness

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.models.ConfidenceLevel
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.LifeTimelinePoint
import com.futureme.shared.models.ReadinessCategory
import com.futureme.shared.models.ReadinessImprovementPlan
import com.futureme.shared.models.ReadinessLevel
import com.futureme.shared.models.ReadinessTrend
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.ScenarioType
import com.futureme.shared.models.TimelineHorizon
import com.futureme.shared.models.TimelineReadinessScore
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class LifeReadinessEngine {
    fun evaluateAll(profile: FinancialProfile): List<LifeReadinessResult> =
        ReadinessCategory.entries.map { evaluate(profile, it) }

    fun evaluate(
        profile: FinancialProfile,
        category: ReadinessCategory,
    ): LifeReadinessResult {
        val score = score(profile, category)
        val projectedScore = score(projectProfile(profile, 6), category)
        val trendDelta = projectedScore - score
        val monthsToReady = estimatedMonths(category, score, READY_THRESHOLD)
        return LifeReadinessResult(
            id = "readiness-${category.name.lowercase().replace('_', '-')}",
            category = category,
            title = title(category),
            readinessScore = score,
            readinessLevel = level(score),
            strengths = strengths(profile, category),
            weaknesses = weaknesses(profile, category),
            blockers = blockers(profile, category),
            confidenceLevel = confidence(category),
            recommendedActions = actions(category),
            projectedReadyDate = projectedDate(monthsToReady),
            estimatedMonthsToReady = monthsToReady,
            trend = when {
                trendDelta >= 2 -> ReadinessTrend.IMPROVING
                trendDelta <= -2 -> ReadinessTrend.DECLINING
                else -> ReadinessTrend.STABLE
            },
            trendDelta = trendDelta,
        )
    }

    fun improvementPlan(
        profile: FinancialProfile,
        category: ReadinessCategory,
        targetScore: Int = 85,
    ): ReadinessImprovementPlan {
        val readiness = evaluate(profile, category)
        val normalizedTarget = targetScore.coerceIn(readiness.readinessScore, 100)
        val months = estimatedMonths(category, readiness.readinessScore, normalizedTarget)
        return ReadinessImprovementPlan(
            id = "plan-${category.name.lowercase().replace('_', '-')}",
            category = category,
            title = "${title(category)} Improvement Plan",
            currentScore = readiness.readinessScore,
            targetScore = normalizedTarget,
            scoreGap = normalizedTarget - readiness.readinessScore,
            recommendations = readiness.recommendedActions,
            monthlyCommitment = monthlyCommitment(category),
            estimatedTimelineMonths = months,
            projectedTargetDate = projectedDate(months),
        )
    }

    fun improvementPlans(profile: FinancialProfile): List<ReadinessImprovementPlan> =
        ReadinessCategory.entries.map { improvementPlan(profile, it) }

    fun estimatedMonths(
        category: ReadinessCategory,
        currentScore: Int,
        targetScore: Int,
    ): Int {
        if (currentScore >= targetScore) return 0
        return ceil((targetScore - currentScore) / monthlyScoreGain(category)).toInt()
    }

    private fun score(
        profile: FinancialProfile,
        category: ReadinessCategory,
    ): Int {
        val surplus = FinancialMath.monthlyCashFlow(profile)
        val essentialOutflow = FinancialMath.essentialMonthlyOutflow(profile)
        val runway = FinancialMath.emergencyFundRunway(
            profile.liquidSavings,
            essentialOutflow,
        )
        val cashFlow = percent(surplus, 4_000.0)
        val reserves = percent(runway, 12.0)
        val longReserves = percent(runway, 18.0)
        val debtResilience = (
            100.0 - percent(profile.creditCardDebt, profile.monthlyNetIncome * 2.0)
        ).coerceIn(0.0, 100.0)
        val incomeResilience = if (profile.spouseMonthlyNetIncome > 0.0) 85.0 else 45.0
        val retirementProgress = (
            percent(profile.investmentBalance, profile.annualGrossIncome * 1.5) +
                percent(profile.monthlyRetirementContribution, 3_000.0)
        ) / 2.0
        val netWorthProgress = percent(
            FinancialMath.currentNetWorth(profile),
            profile.annualGrossIncome * 4.0,
        )

        val weighted = when (category) {
            ReadinessCategory.HOME_PURCHASE ->
                percent(profile.liquidSavings, 140_000.0) * 0.30 +
                    cashFlow * 0.25 +
                    reserves * 0.20 +
                    debtResilience * 0.20 +
                    incomeResilience * 0.05
            ReadinessCategory.CHILD ->
                percent(surplus, 3_500.0) * 0.30 +
                    reserves * 0.25 +
                    percent(profile.liquidSavings, 50_000.0) * 0.20 +
                    incomeResilience * 0.15 +
                    debtResilience * 0.10
            ReadinessCategory.RELOCATION ->
                cashFlow * 0.25 +
                    reserves * 0.25 +
                    percent(profile.liquidSavings, 30_000.0) * 0.20 +
                    incomeResilience * 0.20 +
                    debtResilience * 0.10
            ReadinessCategory.RETIREMENT ->
                retirementProgress * 0.45 +
                    cashFlow * 0.15 +
                    reserves * 0.10 +
                    debtResilience * 0.15 +
                    netWorthProgress * 0.15
            ReadinessCategory.BUSINESS_STARTUP ->
                longReserves * 0.30 +
                    percent(profile.liquidSavings, 150_000.0) * 0.25 +
                    cashFlow * 0.20 +
                    incomeResilience * 0.10 +
                    debtResilience * 0.15
            ReadinessCategory.PARENT_SUPPORT ->
                percent(surplus, 3_000.0) * 0.30 +
                    reserves * 0.25 +
                    percent(profile.liquidSavings, 120_000.0) * 0.20 +
                    incomeResilience * 0.10 +
                    debtResilience * 0.15
            ReadinessCategory.EDUCATION_FUNDING ->
                percent(surplus, 2_500.0) * 0.30 +
                    percent(profile.investmentBalance, profile.annualGrossIncome * 1.5) * 0.20 +
                    reserves * 0.20 +
                    debtResilience * 0.15 +
                    incomeResilience * 0.15
        }
        return weighted.roundToInt().coerceIn(0, 100)
    }

    private fun strengths(
        profile: FinancialProfile,
        category: ReadinessCategory,
    ): List<String> = buildList {
        val surplus = FinancialMath.monthlyCashFlow(profile)
        val runway = FinancialMath.emergencyFundRunway(
            profile.liquidSavings,
            FinancialMath.essentialMonthlyOutflow(profile),
        )
        if (surplus >= 2_000.0) add("Positive monthly cash flow creates room to prepare.")
        if (runway >= 6.0) add("Emergency reserves exceed the six-month planning floor.")
        if (profile.spouseMonthlyNetIncome > 0.0) {
            add("Two income sources improve household resilience.")
        }
        if (
            category == ReadinessCategory.RETIREMENT ||
            category == ReadinessCategory.EDUCATION_FUNDING
        ) {
            add("Existing investments provide a meaningful compounding base.")
        }
        if (category == ReadinessCategory.RELOCATION && profile.liquidSavings >= 30_000.0) {
            add("Available cash can cover modeled moving and transition costs.")
        }
    }

    private fun weaknesses(
        profile: FinancialProfile,
        category: ReadinessCategory,
    ): List<String> = buildList {
        if (profile.creditCardDebt > 0.0) {
            add("High-interest debt reduces flexibility and readiness.")
        }
        when (category) {
            ReadinessCategory.HOME_PURCHASE ->
                add("A larger down payment would preserve more liquidity after closing.")
            ReadinessCategory.CHILD ->
                add("Childcare would consume a meaningful share of current surplus.")
            ReadinessCategory.RELOCATION ->
                add("Compensation and benefit assumptions are not yet confirmed.")
            ReadinessCategory.RETIREMENT ->
                add("The current retirement savings rate is below the early-retirement target.")
            ReadinessCategory.BUSINESS_STARTUP ->
                add("The household does not yet hold an 18-month business runway.")
            ReadinessCategory.PARENT_SUPPORT ->
                add("No dedicated parent-care reserve is modeled.")
            ReadinessCategory.EDUCATION_FUNDING ->
                add("No dedicated education account is included in the current profile.")
        }
    }

    private fun blockers(
        profile: FinancialProfile,
        category: ReadinessCategory,
    ): List<String> = buildList {
        val surplus = FinancialMath.monthlyCashFlow(profile)
        val runway = FinancialMath.emergencyFundRunway(
            profile.liquidSavings,
            FinancialMath.essentialMonthlyOutflow(profile),
        )
        if (profile.creditCardDebt > profile.monthlyNetIncome) {
            add("Pay down the credit-card balance before taking on another major commitment.")
        }
        when (category) {
            ReadinessCategory.HOME_PURCHASE -> {
                if (profile.liquidSavings < 140_000.0) {
                    add("The target down payment and closing reserve are not fully funded.")
                }
            }
            ReadinessCategory.CHILD -> {
                if (surplus < 3_500.0) add("Current surplus does not fully absorb modeled childcare.")
            }
            ReadinessCategory.RELOCATION ->
                add("A signed compensation and benefits package is required before moving.")
            ReadinessCategory.RETIREMENT -> {
                if (profile.monthlyRetirementContribution < 3_000.0) {
                    add("Monthly retirement investing is below the early-retirement pace.")
                }
            }
            ReadinessCategory.BUSINESS_STARTUP -> {
                if (runway < 18.0) add("Build an 18-month household and business runway.")
            }
            ReadinessCategory.PARENT_SUPPORT -> {
                if (surplus < 3_000.0) add("Recurring parent support would exceed the planning buffer.")
            }
            ReadinessCategory.EDUCATION_FUNDING ->
                add("Choose a funding target and open a dedicated education account.")
        }
    }

    private fun actions(category: ReadinessCategory): List<String> = when (category) {
        ReadinessCategory.HOME_PURCHASE -> listOf(
            "Pay off high-interest debt.",
            "Save an additional down payment and closing reserve.",
            "Keep at least six months of essential expenses liquid.",
            "Reduce discretionary spending by $250 per month.",
        )
        ReadinessCategory.CHILD -> listOf(
            "Build a $15,000 family transition fund.",
            "Model childcare and dependent-care benefits.",
            "Confirm parental leave and healthcare coverage.",
        )
        ReadinessCategory.RELOCATION -> listOf(
            "Confirm compensation, taxes, and health benefits.",
            "Reserve $15,000 for moving and setup costs.",
            "Compare housing and childcare costs before committing.",
        )
        ReadinessCategory.RETIREMENT -> listOf(
            "Capture the full employer match.",
            "Increase retirement investing after card payoff.",
            "Model healthcare and bridge-income costs.",
        )
        ReadinessCategory.BUSINESS_STARTUP -> listOf(
            "Build an 18-month household runway.",
            "Separate startup capital from emergency savings.",
            "Test the plan with one income before leaving employment.",
        )
        ReadinessCategory.PARENT_SUPPORT -> listOf(
            "Create a dedicated parent-care reserve.",
            "Estimate recurring care, travel, and housing support.",
            "Review insurance and public-benefit eligibility.",
        )
        ReadinessCategory.EDUCATION_FUNDING -> listOf(
            "Set a target share of future education costs.",
            "Open and automate a dedicated education account.",
            "Increase contributions after high-interest debt is cleared.",
        )
    }

    private fun projectProfile(
        profile: FinancialProfile,
        months: Int,
    ): FinancialProfile {
        val surplus = max(0.0, FinancialMath.monthlyCashFlow(profile))
        return profile.copy(
            liquidSavings = profile.liquidSavings + surplus * 0.35 * months,
            creditCardDebt = max(0.0, profile.creditCardDebt - surplus * 0.30 * months),
            investmentBalance = FinancialMath.compoundMonthly(
                openingBalance = profile.investmentBalance,
                annualReturn = 0.06,
                monthlyContribution = profile.monthlyRetirementContribution + surplus * 0.15,
                months = months,
            ),
        )
    }

    private fun level(score: Int): ReadinessLevel = when (score) {
        in 0..39 -> ReadinessLevel.NOT_READY
        in 40..59 -> ReadinessLevel.NEEDS_PREPARATION
        in 60..79 -> ReadinessLevel.ALMOST_READY
        else -> ReadinessLevel.READY
    }

    private fun confidence(category: ReadinessCategory): ConfidenceLevel = when (category) {
        ReadinessCategory.HOME_PURCHASE,
        ReadinessCategory.CHILD,
        ReadinessCategory.RELOCATION,
        -> ConfidenceLevel.HIGH
        ReadinessCategory.RETIREMENT,
        ReadinessCategory.PARENT_SUPPORT,
        ReadinessCategory.EDUCATION_FUNDING,
        -> ConfidenceLevel.MEDIUM
        ReadinessCategory.BUSINESS_STARTUP -> ConfidenceLevel.LOW
    }

    private fun title(category: ReadinessCategory): String = when (category) {
        ReadinessCategory.HOME_PURCHASE -> "Home Purchase Readiness"
        ReadinessCategory.CHILD -> "Child Readiness"
        ReadinessCategory.RELOCATION -> "Relocation Readiness"
        ReadinessCategory.RETIREMENT -> "Retirement Readiness"
        ReadinessCategory.BUSINESS_STARTUP -> "Business Startup Readiness"
        ReadinessCategory.PARENT_SUPPORT -> "Parent Support Readiness"
        ReadinessCategory.EDUCATION_FUNDING -> "Education Funding Readiness"
    }

    private fun monthlyCommitment(category: ReadinessCategory): Double = when (category) {
        ReadinessCategory.HOME_PURCHASE -> 1_050.0
        ReadinessCategory.CHILD -> 900.0
        ReadinessCategory.RELOCATION -> 350.0
        ReadinessCategory.RETIREMENT -> 1_400.0
        ReadinessCategory.BUSINESS_STARTUP -> 1_250.0
        ReadinessCategory.PARENT_SUPPORT -> 600.0
        ReadinessCategory.EDUCATION_FUNDING -> 750.0
    }

    private fun monthlyScoreGain(category: ReadinessCategory): Double = when (category) {
        ReadinessCategory.HOME_PURCHASE -> 1.7
        ReadinessCategory.CHILD -> 1.1
        ReadinessCategory.RELOCATION -> 1.4
        ReadinessCategory.RETIREMENT -> 0.45
        ReadinessCategory.BUSINESS_STARTUP -> 0.9
        ReadinessCategory.PARENT_SUPPORT -> 1.0
        ReadinessCategory.EDUCATION_FUNDING -> 0.7
    }

    private fun projectedDate(months: Int): String {
        val monthIndex = BASE_MONTH - 1 + months
        val year = BASE_YEAR + monthIndex / 12
        val month = monthIndex % 12 + 1
        return "$year-${month.toString().padStart(2, '0')}-01"
    }

    private fun percent(value: Double, target: Double): Double =
        if (target <= 0.0) 0.0 else (value / target * 100.0).coerceIn(0.0, 100.0)

    companion object {
        private const val READY_THRESHOLD = 80
        private const val BASE_YEAR = 2026
        private const val BASE_MONTH = 6
    }
}

class LifeDecisionSimulator(
    private val readinessEngine: LifeReadinessEngine,
) {
    fun simulate(
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
        result: ScenarioResult,
    ): LifeDecisionSimulation {
        val category = category(result.scenario.type)
        val readiness = readinessEngine.evaluate(profile, category)
        val impact = (
            (result.monthlyCashFlowImpact / 150.0).coerceIn(-24.0, 16.0) +
                ((result.emergencyFundMonths - dashboard.emergencyFundMonths) * 2.0)
                    .coerceIn(-20.0, 12.0) -
                ((result.riskScore.value - dashboard.riskScore.value) * 0.30)
                    .coerceIn(-12.0, 18.0) +
                (result.netWorthDelta5Years / 25_000.0).coerceIn(-8.0, 8.0)
        ).roundToInt()
        val after = (readiness.readinessScore + impact).coerceIn(0, 100)
        val timelineBefore = readiness.estimatedMonthsToReady
        val timelineAfter = readinessEngine.estimatedMonths(category, after, 80)
        return LifeDecisionSimulation(
            scenarioId = result.scenario.id,
            title = result.scenario.title,
            category = category,
            readinessScoreBefore = readiness.readinessScore,
            readinessScoreAfter = after,
            readinessImpact = after - readiness.readinessScore,
            monthlyCashFlowImpact = result.monthlyCashFlowImpact,
            fiveYearNetWorthImpact = result.netWorthDelta5Years,
            riskScoreBefore = dashboard.riskScore.value,
            riskScoreAfter = result.riskScore.value,
            riskChange = result.riskScore.value - dashboard.riskScore.value,
            timelineChangeMonths = timelineAfter - timelineBefore,
            summary = summary(result, after),
            recommendedActions = (
                readiness.recommendedActions + result.tradeoffs
            ).distinct().take(4),
        )
    }

    private fun category(type: ScenarioType): ReadinessCategory = when (type) {
        ScenarioType.BUY_HOME,
        ScenarioType.REFINANCE_MORTGAGE,
        -> ReadinessCategory.HOME_PURCHASE
        ScenarioType.HAVE_CHILD,
        ScenarioType.SPOUSE_STOPS_WORKING,
        -> ReadinessCategory.CHILD
        ScenarioType.RELOCATE -> ReadinessCategory.RELOCATION
        ScenarioType.START_BUSINESS -> ReadinessCategory.BUSINESS_STARTUP
        ScenarioType.INCREASE_INVESTMENTS -> ReadinessCategory.RETIREMENT
        ScenarioType.PAY_OFF_DEBT -> ReadinessCategory.HOME_PURCHASE
        ScenarioType.JOB_LOSS -> ReadinessCategory.BUSINESS_STARTUP
    }

    private fun summary(
        result: ScenarioResult,
        scoreAfter: Int,
    ): String = when {
        scoreAfter < 40 ->
            "${result.scenario.title} would leave the household not ready without a smaller commitment or longer runway."
        scoreAfter < 60 ->
            "${result.scenario.title} needs preparation before the household can absorb the modeled cash-flow and risk changes."
        scoreAfter < 80 ->
            "${result.scenario.title} is within reach, but the plan should close the remaining readiness gaps first."
        else ->
            "${result.scenario.title} remains supportable under the shared readiness and risk model."
    }
}

class LifeTimelineEngine(
    private val readinessEngine: LifeReadinessEngine,
) {
    fun build(
        profile: FinancialProfile,
        baselineResult: ScenarioResult,
    ): List<LifeTimelinePoint> = listOf(
        point(profile, baselineResult, TimelineHorizon.TODAY, "Today", 0),
        point(profile, baselineResult, TimelineHorizon.SIX_MONTHS, "6 months", 6),
        point(profile, baselineResult, TimelineHorizon.ONE_YEAR, "1 year", 12),
        point(profile, baselineResult, TimelineHorizon.THREE_YEARS, "3 years", 36),
        point(profile, baselineResult, TimelineHorizon.FIVE_YEARS, "5 years", 60),
    )

    private fun point(
        profile: FinancialProfile,
        baselineResult: ScenarioResult,
        horizon: TimelineHorizon,
        label: String,
        months: Int,
    ): LifeTimelinePoint {
        val debtBalance = max(
            0.0,
            profile.creditCardDebt - profile.monthlyDebtPayments * months * 0.78,
        )
        val investmentBalance = FinancialMath.compoundMonthly(
            openingBalance = profile.investmentBalance,
            annualReturn = 0.06,
            monthlyContribution = profile.monthlyRetirementContribution,
            months = months,
        )
        val surplus = max(0.0, FinancialMath.monthlyCashFlow(profile))
        val projectedProfile = profile.copy(
            liquidSavings = profile.liquidSavings + surplus * 0.25 * months,
            creditCardDebt = debtBalance,
            investmentBalance = investmentBalance,
        )
        return LifeTimelinePoint(
            horizon = horizon,
            label = label,
            monthsFromNow = months,
            netWorth = netWorthAt(baselineResult, months),
            debtBalance = debtBalance,
            investmentBalance = investmentBalance,
            readinessScores = readinessEngine.evaluateAll(projectedProfile).map {
                TimelineReadinessScore(it.category, it.readinessScore)
            },
            completedGoals = completedGoals(projectedProfile, months),
        )
    }

    private fun netWorthAt(
        result: ScenarioResult,
        months: Int,
    ): Double {
        if (months == 6) {
            return (result.projections[0].scenarioNetWorth + result.projections[1].scenarioNetWorth) / 2.0
        }
        val year = months / 12
        return result.projections.first { it.year == year }.scenarioNetWorth
    }

    private fun completedGoals(
        profile: FinancialProfile,
        months: Int,
    ): List<String> = buildList {
        if (profile.creditCardDebt <= 0.0) add("High-interest debt paid off")
        if (
            FinancialMath.emergencyFundRunway(
                profile.liquidSavings,
                FinancialMath.essentialMonthlyOutflow(profile),
            ) >= 12.0
        ) {
            add("12-month emergency reserve")
        }
        if (months >= 36) add("Education funding automation established")
        if (months >= 60) add("Retirement contribution step-up completed")
    }
}
