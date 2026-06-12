package com.futureme.shared.domain

import com.futureme.shared.models.AiEvaluationCategoryScore
import com.futureme.shared.models.AiEvaluationDashboard
import com.futureme.shared.models.CustomerPersona
import com.futureme.shared.models.ExecutiveDemoStory
import com.futureme.shared.models.FeatureRecommendation
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.PersonaReadinessScore
import com.futureme.shared.models.ProductNavigationTab
import com.futureme.shared.models.ProductPriority
import com.futureme.shared.models.ProductStrategy

class ProductStrategyService {
    fun strategy(): ProductStrategy = ProductStrategy(
        positioningStatement = "FutureMe turns a household's financial data into a clear answer " +
            "about what to do next and how ready they are for a major life decision.",
        productPromise = "Know what matters now, why it matters, and what action moves your future.",
        navigation = listOf(
            ProductNavigationTab(
                id = "home",
                label = "Home",
                customerQuestion = "What should I focus on today?",
                contents = listOf(
                    "Next Best Action",
                    "Top readiness change",
                    "Biggest risk",
                    "Biggest opportunity",
                ),
                rationale = "Starts with a decision, not a dashboard inventory.",
            ),
            ProductNavigationTab(
                id = "missions",
                label = "Missions",
                customerQuestion = "What am I trying to achieve?",
                contents = listOf(
                    "Mission readiness",
                    "Blockers",
                    "Action plans",
                    "Roadmaps and scenarios",
                ),
                rationale = "Groups goals, readiness, scenarios, and timelines around the outcome.",
            ),
            ProductNavigationTab(
                id = "insights",
                label = "Insights",
                customerQuestion = "What changed?",
                contents = listOf(
                    "Money leaks",
                    "Risks",
                    "Opportunities",
                    "Monthly review",
                ),
                rationale = "Combines overlapping alerts and recommendations into one change feed.",
            ),
            ProductNavigationTab(
                id = "coach",
                label = "Coach",
                customerQuestion = "Help me understand.",
                contents = listOf(
                    "Claude Coach",
                    "Suggested questions",
                    "Mission explanations",
                    "AI quality benchmark",
                ),
                rationale = "Makes explanation a distinct, grounded layer over deterministic results.",
            ),
            ProductNavigationTab(
                id = "profile",
                label = "Profile",
                customerQuestion = "Who am I?",
                contents = listOf(
                    "Financial profile",
                    "Family profile",
                    "Connected accounts",
                    "Settings and demo mode",
                ),
                rationale = "Keeps identity, data sources, privacy, and configuration out of planning flows.",
            ),
        ),
        featureRecommendations = listOf(
            feature("Mission Control", ProductPriority.MUST_HAVE, "The core organizing experience."),
            feature("Next Best Action", ProductPriority.MUST_HAVE, "Creates immediate monthly value."),
            feature("Mission Action Plans", ProductPriority.MUST_HAVE, "Turns readiness into execution."),
            feature("Mission Coach", ProductPriority.MUST_HAVE, "Explains deterministic results in plain language."),
            feature("Readiness and blockers", ProductPriority.MUST_HAVE, "Answers whether a life decision is supportable."),
            feature("Financial explainability", ProductPriority.MUST_HAVE, "Builds trust by explaining change."),
            feature("Money leaks and risks", ProductPriority.MUST_HAVE, "Provides proactive, measurable value."),
            feature("Profile and account context", ProductPriority.MUST_HAVE, "Establishes the facts behind guidance."),
            feature("Mission scenarios", ProductPriority.NICE_TO_HAVE, "Useful inside a mission, not as a top-level lab."),
            feature("Mission history", ProductPriority.NICE_TO_HAVE, "Proves progress after repeated use."),
            feature("Monthly review", ProductPriority.NICE_TO_HAVE, "Creates a natural engagement cadence."),
            feature("Decision journal", ProductPriority.NICE_TO_HAVE, "Supports outcome learning and advisor review."),
            feature("Financial timeline", ProductPriority.NICE_TO_HAVE, "Helpful when presented inside a mission."),
            feature("AI evaluation dashboard", ProductPriority.NICE_TO_HAVE, "Makes model quality visible to product teams."),
            feature("Live account aggregation", ProductPriority.FUTURE_RELEASE, "Requires bank-grade consent and controls."),
            feature("Advisor workspace", ProductPriority.FUTURE_RELEASE, "Valuable after the consumer experience is proven."),
            feature("Personalized product fulfillment", ProductPriority.FUTURE_RELEASE, "Requires suitability and compliance design."),
            feature("Standalone readiness dashboard", ProductPriority.POTENTIALLY_REMOVE, "Duplicates Missions."),
            feature("Standalone scenario dashboard", ProductPriority.POTENTIALLY_REMOVE, "Scenarios belong inside Missions."),
            feature("Standalone banking intelligence dashboard", ProductPriority.POTENTIALLY_REMOVE, "Its content belongs on Home and Insights."),
            feature("Separate goal probability screens", ProductPriority.POTENTIALLY_REMOVE, "Probability should be a mission factor."),
        ),
    )

    fun aiEvaluationDashboard(): AiEvaluationDashboard {
        val categories = listOf(
            category("MISSION_READINESS", "Mission readiness", 8, 0, 0, 0, 0, 0, 0),
            category("HOME_PURCHASE", "Home purchase", 7, 0, 0, 0, 0, 0, 0),
            category("CHILD_READINESS", "Child readiness", 7, 0, 0, 0, 0, 0, 0),
            category("DEBT_REDUCTION", "Debt reduction", 7, 0, 0, 0, 0, 0, 0),
            category("RETIREMENT", "Retirement", 7, 0, 0, 0, 0, 0, 0),
            category("RELOCATION", "Relocation", 7, 0, 0, 0, 0, 0, 0),
            category("RISK_ANALYSIS", "Risk analysis", 7, 0, 0, 0, 0, 0, 0),
        )
        return AiEvaluationDashboard(
            totalPrompts = categories.sumOf { it.promptCount },
            lastRunDate = "Pending",
            overallQualityScore = categories.map { it.responseQualityScore }.average().toInt(),
            reasoningQualityScore = categories.map { it.reasoningQualityScore }.average().toInt(),
            consistencyScore = categories.map { it.consistencyScore }.average().toInt(),
            hallucinationRiskScore = categories.map { it.hallucinationRiskScore }.average().toInt(),
            explanationUsefulnessScore =
                categories.map { it.explanationUsefulnessScore }.average().toInt(),
            categories = categories,
            statusLabel = "50-prompt quality benchmark ready to run",
            methodologyNote = "Scores remain pending until the live benchmark runs. The separate " +
                "provider integration suite passed 22 of 22 checks on June 12, 2026.",
        )
    }

    fun personas(): List<CustomerPersona> = listOf(
        persona(
            id = "young-family",
            title = "Young Family",
            summary = "Dual-income parents balancing a second child, a larger home, and expensive debt.",
            stateCode = "NJ",
            income = 188_000.0,
            netIncome = 11_400.0,
            spouseNetIncome = 4_800.0,
            expenses = 7_050.0,
            savings = 54_000.0,
            cardDebt = 14_500.0,
            mortgage = 318_000.0,
            property = 515_000.0,
            investments = 142_000.0,
            dependents = 1,
            goals = listOf("Prepare for a second child", "Move to a larger home"),
            challenges = listOf("Childcare pressure", "High-interest debt", "Four months of reserves"),
            recommendations = listOf(
                "Eliminate revolving debt before increasing housing cost",
                "Build a dedicated family transition reserve",
                "Stress-test the plan on one income",
            ),
            readiness = listOf("Child" to 66, "Home purchase" to 58, "Emergency fund" to 64),
            missionPlan = listOf(
                "Pay off the highest-rate card",
                "Save $750 monthly into the transition fund",
                "Confirm childcare and leave costs",
            ),
        ),
        persona(
            id = "high-income-professional",
            title = "High Income Professional",
            summary = "A strong earner with concentrated equity, lifestyle expansion, and limited planning time.",
            stateCode = "CA",
            income = 420_000.0,
            netIncome = 22_500.0,
            expenses = 12_900.0,
            savings = 118_000.0,
            cardDebt = 7_500.0,
            mortgage = 780_000.0,
            property = 1_250_000.0,
            investments = 690_000.0,
            dependents = 0,
            goals = listOf("Reach work-optional status", "Diversify concentrated equity"),
            challenges = listOf("Employer-stock concentration", "High fixed costs", "Irregular bonus income"),
            recommendations = listOf(
                "Automate diversification after vesting",
                "Base recurring spending on salary rather than bonus",
                "Model an early-retirement contribution target",
            ),
            readiness = listOf("Retirement" to 74, "Business startup" to 70, "Relocation" to 82),
            missionPlan = listOf(
                "Set a concentration limit",
                "Redirect the next vest into diversified assets",
                "Create a work-optional target date",
            ),
        ),
        persona(
            id = "pre-retirement-couple",
            title = "Pre-Retirement Couple",
            summary = "A couple five years from retirement deciding when work can safely become optional.",
            stateCode = "FL",
            income = 214_000.0,
            netIncome = 12_800.0,
            spouseNetIncome = 5_300.0,
            expenses = 8_400.0,
            savings = 162_000.0,
            cardDebt = 0.0,
            mortgage = 126_000.0,
            property = 640_000.0,
            investments = 1_380_000.0,
            dependents = 0,
            goals = listOf("Retire in five years", "Support an aging parent"),
            challenges = listOf("Healthcare bridge", "Sequence-of-returns risk", "Parent support uncertainty"),
            recommendations = listOf(
                "Build a pre-Medicare healthcare reserve",
                "Separate near-term spending from growth assets",
                "Define a sustainable parent-support budget",
            ),
            readiness = listOf("Retirement" to 79, "Parent support" to 68, "Emergency fund" to 88),
            missionPlan = listOf(
                "Price the healthcare bridge",
                "Fund two years of planned withdrawals",
                "Test retirement under a market decline",
            ),
        ),
        persona(
            id = "new-home-buyer",
            title = "New Home Buyer",
            summary = "A first-time buyer trying to balance down payment speed with financial resilience.",
            stateCode = "TX",
            income = 136_000.0,
            netIncome = 8_150.0,
            expenses = 4_650.0,
            savings = 72_000.0,
            cardDebt = 3_800.0,
            mortgage = 0.0,
            property = 0.0,
            investments = 82_000.0,
            dependents = 0,
            goals = listOf("Buy a first home within 18 months"),
            challenges = listOf("Thin emergency reserve after closing", "Uncertain ownership costs"),
            recommendations = listOf(
                "Keep closing reserves separate from the down payment",
                "Clear card debt before underwriting",
                "Model taxes, insurance, maintenance, and HOA costs",
            ),
            readiness = listOf("Home purchase" to 63, "Emergency fund" to 57, "Relocation" to 75),
            missionPlan = listOf(
                "Pay off $3,800 of card debt",
                "Build six months of post-close reserves",
                "Set an all-in monthly housing ceiling",
            ),
        ),
        persona(
            id = "single-parent",
            title = "Single Parent",
            summary = "A single-income household prioritizing resilience, education, and predictable cash flow.",
            stateCode = "IL",
            income = 112_000.0,
            netIncome = 7_050.0,
            expenses = 5_850.0,
            savings = 28_000.0,
            cardDebt = 9_200.0,
            mortgage = 226_000.0,
            property = 370_000.0,
            investments = 96_000.0,
            dependents = 2,
            goals = listOf("Build a six-month reserve", "Fund education without sacrificing retirement"),
            challenges = listOf("Single-income risk", "Childcare costs", "Limited monthly surplus"),
            recommendations = listOf(
                "Prioritize income protection and emergency reserves",
                "Use a staged debt payoff plan",
                "Protect retirement contributions before increasing education funding",
            ),
            readiness = listOf("Emergency fund" to 49, "Education funding" to 55, "Retirement" to 61),
            missionPlan = listOf(
                "Reach the next month of emergency runway",
                "Pay the highest-rate debt first",
                "Automate a modest education contribution",
            ),
        ),
    )

    fun executiveDemoStory(): ExecutiveDemoStory = ExecutiveDemoStory(
        title = "Meet Sarah: two decisions, one coordinated plan",
        audience = listOf(
            "Bank executives",
            "Innovation teams",
            "Product leaders",
            "Engineering leaders",
        ),
        personaId = "young-family",
        opening = "Sarah wants a larger home and a second child, but her banking app only shows balances.",
        steps = listOf(
            "Home shows the one action with the greatest modeled impact this month.",
            "Missions explains readiness for the home and child decisions.",
            "FutureMe identifies debt and reserve blockers shared by both missions.",
            "The roadmap sequences debt payoff, savings, and childcare planning.",
            "Insights explains what changed and why the timeline moved.",
            "Claude Coach answers Sarah's questions using only calculated mission facts.",
            "FutureMe tracks progress until Sarah is ready to act.",
        ),
        closing = "FutureMe turns passive banking data into a continuously managed life-decision plan.",
    )

    private fun feature(
        name: String,
        priority: ProductPriority,
        rationale: String,
    ) = FeatureRecommendation(name, priority, rationale)

    private fun category(
        category: String,
        label: String,
        count: Int,
        response: Int,
        reasoning: Int,
        consistency: Int,
        hallucination: Int,
        usefulness: Int,
        passRate: Int,
    ) = AiEvaluationCategoryScore(
        category = category,
        label = label,
        promptCount = count,
        responseQualityScore = response,
        reasoningQualityScore = reasoning,
        consistencyScore = consistency,
        hallucinationRiskScore = hallucination,
        explanationUsefulnessScore = usefulness,
        passRate = passRate,
    )

    private fun persona(
        id: String,
        title: String,
        summary: String,
        stateCode: String,
        income: Double,
        netIncome: Double,
        spouseNetIncome: Double = 0.0,
        expenses: Double,
        savings: Double,
        cardDebt: Double,
        mortgage: Double,
        property: Double,
        investments: Double,
        dependents: Int,
        goals: List<String>,
        challenges: List<String>,
        recommendations: List<String>,
        readiness: List<Pair<String, Int>>,
        missionPlan: List<String>,
    ) = CustomerPersona(
        id = id,
        title = title,
        summary = summary,
        profile = FinancialProfile(
            profileId = "persona-$id",
            ownerId = "persona-owner-$id",
            stateCode = stateCode,
            annualGrossIncome = income,
            monthlyNetIncome = netIncome,
            monthlyLivingExpenses = expenses,
            liquidSavings = savings,
            creditCardDebt = cardDebt,
            creditCardApr = if (cardDebt > 0.0) 0.2099 else 0.0,
            housingPayment = if (mortgage > 0.0) expenses * 0.38 else expenses * 0.31,
            mortgageBalance = mortgage,
            propertyValue = property,
            investmentBalance = investments,
            monthlyRetirementContribution = netIncome * 0.11,
            monthlyDebtPayments = if (cardDebt > 0.0) 450.0 else 0.0,
            dependents = dependents,
            primaryMonthlyNetIncome = netIncome - spouseNetIncome,
            spouseMonthlyNetIncome = spouseNetIncome,
            monthlyChildcare = if (dependents > 0) 1_450.0 else 0.0,
            monthlyInsurance = 620.0,
            monthlySubscriptions = 145.0,
            monthlyUtilities = 430.0,
        ),
        goals = goals,
        challenges = challenges,
        expectedRecommendations = recommendations,
        expectedReadinessScores = readiness.map {
            PersonaReadinessScore(category = it.first, score = it.second)
        },
        expectedMissionPlan = missionPlan,
    )
}
