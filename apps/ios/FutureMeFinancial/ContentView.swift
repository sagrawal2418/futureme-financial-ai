import Charts
import Shared
import SwiftUI

struct ContentView: View {
    @ObservedObject var viewModel: FutureMeViewModel

    var body: some View {
        switch viewModel.state {
        case .loading:
            LoadingView()
        case let .empty(message):
            StateView(
                title: "No scenarios yet",
                message: message,
                action: "Reload demo workspace",
                onAction: viewModel.load
            )
        case let .error(message):
            StateView(
                title: "We could not load FutureMe",
                message: message,
                action: "Try again",
                onAction: viewModel.load
            )
        case let .content(content):
            DashboardTabs(
                content: content,
                onSelect: viewModel.select,
                onCompare: viewModel.compare,
                onAsk: viewModel.ask
            )
        }
    }
}

private struct DashboardTabs: View {
    let content: FutureMeDashboardContent
    let onSelect: (ScenarioCardModel) -> Void
    let onCompare: (ScenarioCardModel, ScenarioCardModel) -> Void
    let onAsk: (String) -> Void
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        Header(name: content.displayName, householdName: content.householdName)
                        HealthCard(dashboard: content.dashboard)
                            .padding(.horizontal, 20)
                        WeeklyCheckupCard(insights: content.insights)
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                        FinancialGpsCard(gps: content.financialGps) {
                            selectedTab = 4
                            onAsk("How can I improve my 5-year outlook?")
                        }
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                        MetricsGrid(dashboard: content.dashboard)
                            .padding(.horizontal, 20)
                            .padding(.top, 12)
                        AlertsCard(alerts: content.dashboard.alerts)
                            .padding(.horizontal, 20)
                            .padding(.top, 12)
                        Button {
                            selectedTab = 4
                        } label: {
                            Label("Ask FutureMe about this plan", systemImage: "sparkles")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(AppTheme.forest)
                        .padding(.horizontal, 20)
                        .padding(.top, 14)
                        .accessibilityHint("Opens the financial assistant")
                        MoneyLeakPreview(leaks: content.moneyLeaks) {
                            selectedTab = 1
                        }
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                        GoalReadinessCard(goals: content.goals)
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                        ProjectionCard(result: content.result)
                            .padding(.horizontal, 20)
                            .padding(.top, 14)
                        SectionTitle(eyebrow: "DECISION LAB", title: "Explore a different future")
                            .padding(.horizontal, 20)
                            .padding(.top, 30)
                        ScenarioCarousel(
                            scenarios: content.scenarios,
                            selected: content.selected,
                            onSelect: onSelect
                        )
                            .padding(.top, 13)
                        InsightCard(result: content.result)
                            .padding(.horizontal, 20)
                            .padding(.top, 16)
                        RiskExplanationCard(result: content.result)
                            .padding(.horizontal, 20)
                            .padding(.top, 12)
                        SectionTitle(eyebrow: "SIDE BY SIDE", title: "Compare two paths")
                            .padding(.horizontal, 20)
                            .padding(.top, 30)
                        ComparisonCard(comparison: content.comparison)
                            .padding(.horizontal, 20)
                            .padding(.top, 13)
                        Label(
                            "Educational simulation only, not financial advice.",
                            systemImage: "shield.checkered"
                        )
                        .font(.caption2)
                        .foregroundStyle(AppTheme.muted)
                        .padding(.vertical, 28)
                    }
                }
                .background(AppTheme.canvas)
                .toolbar(.hidden, for: .navigationBar)
            }
            .tabItem { Label("Overview", systemImage: "chart.bar.fill") }
            .tag(0)

            PlanningTab(
                content: content,
                onPlanScenario: { scenarioId in
                    guard let scenario = content.scenarios.first(where: { $0.id == scenarioId }) else {
                        return
                    }
                    onSelect(scenario)
                    selectedTab = 0
                },
                onAsk: { question in
                    selectedTab = 4
                    onAsk(question)
                }
            )
                .tabItem { Label("Plan", systemImage: "map.fill") }
                .tag(1)

            ScenarioListTab(
                scenarios: content.scenarios,
                selected: content.selected,
                onSelect: onSelect,
                openOverview: { selectedTab = 0 }
            )
                .tabItem { Label("Scenarios", systemImage: "sparkles") }
                .tag(2)

            ComparisonTab(
                comparison: content.comparison,
                scenarios: content.scenarios,
                disclaimer: content.disclaimer,
                onCompare: onCompare
            )
                .tabItem { Label("Compare", systemImage: "rectangle.split.2x1") }
                .tag(3)

            AssistantTab(
                messages: content.messages,
                suggestions: content.suggestions,
                onAsk: onAsk
            )
                .tabItem { Label("Assistant", systemImage: "bubble.left.and.sparkles") }
                .tag(4)
        }
        .tint(AppTheme.positive)
    }
}

private struct WeeklyCheckupCard: View {
    let insights: [Insight]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Eyebrow("THIS WEEK'S FINANCIAL CHECKUP")
            Text("Three signals worth your attention")
                .font(.title3.bold())
                .foregroundStyle(AppTheme.ink)
            ForEach(Array(insights.prefix(3)), id: \.id) { insight in
                VStack(alignment: .leading, spacing: 3) {
                    Text(insight.title)
                        .font(.subheadline.bold())
                        .foregroundStyle(AppTheme.ink)
                    Text(insight.summary)
                        .font(.caption)
                        .foregroundStyle(AppTheme.muted)
                    if insight.estimatedDollarImpact > 0 {
                        Text(money(insight.estimatedDollarImpact) + " modeled impact")
                            .font(.caption2.bold())
                            .foregroundStyle(AppTheme.positive)
                    }
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(AppTheme.surfaceVariant)
                .clipShape(RoundedRectangle(cornerRadius: 13))
            }
        }
        .padding(20)
        .futureMeCard()
        .accessibilityElement(children: .combine)
        .accessibilityLabel("This week's financial checkup with \(min(3, insights.count)) insights")
    }
}

private struct FinancialGpsCard: View {
    let gps: FinancialGpsResult
    let onExplain: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 13) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Eyebrow("FINANCIAL GPS")
                    Text("A better route is available")
                        .font(.title3.bold())
                        .foregroundStyle(AppTheme.ink)
                }
                Spacer()
                Text("HIGH CONFIDENCE")
                    .font(.system(size: 8, weight: .bold))
                    .foregroundStyle(AppTheme.positive)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 5)
                    .background(AppTheme.softMint)
                    .clipShape(Capsule())
            }
            HStack(spacing: 9) {
                GpsMetric(label: "CURRENT TRAJECTORY", value: moneyCompact(gps.currentFiveYearNetWorth))
                GpsMetric(label: "IMPROVED TRAJECTORY", value: moneyCompact(gps.improvedFiveYearNetWorth))
            }
            Text("Potential five-year lift: " + money(gps.difference))
                .font(.headline)
                .foregroundStyle(AppTheme.positive)
            ForEach(gps.monthlyActionPlan, id: \.self) { action in
                Label(action, systemImage: "arrow.turn.down.right")
                    .font(.caption)
                    .foregroundStyle(AppTheme.muted)
            }
            Button("Explain my improved route", action: onExplain)
                .buttonStyle(.bordered)
                .tint(AppTheme.positive)
                .frame(maxWidth: .infinity)
                .accessibilityHint("Asks FutureMe to explain the Financial GPS plan")
        }
        .padding(20)
        .futureMeCard(fill: AppTheme.softMint, showsBorder: false)
    }
}

private struct GpsMetric: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 8, weight: .bold))
                .foregroundStyle(AppTheme.muted)
            Text(value)
                .font(.title3.bold())
                .foregroundStyle(AppTheme.ink)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(13)
        .background(AppTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 13))
    }
}

private struct MoneyLeakPreview: View {
    let leaks: [MoneyLeak]
    let onReview: () -> Void

    private var annualImpact: Double {
        leaks.reduce(0) { $0 + $1.estimatedAnnualLoss }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Eyebrow("MONEY LEAKS")
            Text(money(annualImpact) + " in annual opportunity")
                .font(.title3.bold())
                .foregroundStyle(AppTheme.ink)
            ForEach(Array(leaks.prefix(2)), id: \.id) { leak in
                HStack {
                    Text(leak.title)
                        .font(.caption)
                        .foregroundStyle(AppTheme.ink)
                    Spacer()
                    Text(money(leak.estimatedMonthlyLoss) + "/mo")
                        .font(.caption.bold())
                        .foregroundStyle(AppTheme.warning)
                }
            }
            Button("Review all money leaks", action: onReview)
                .buttonStyle(.bordered)
                .tint(AppTheme.positive)
        }
        .padding(20)
        .futureMeCard()
    }
}

private struct GoalReadinessCard: View {
    let goals: [GoalProbabilityResult]

    var body: some View {
        VStack(alignment: .leading, spacing: 11) {
            Eyebrow("GOAL READINESS")
            Text("How close your next chapter is")
                .font(.title3.bold())
                .foregroundStyle(AppTheme.ink)
            ForEach(Array(goals.prefix(3)), id: \.id) { goal in
                HStack {
                    Text(goal.title)
                        .font(.caption)
                        .foregroundStyle(AppTheme.ink)
                    Spacer()
                    Text("\(goal.probabilityPercentage)%")
                        .font(.caption.bold())
                        .foregroundStyle(AppTheme.positive)
                }
                ProgressView(value: Double(goal.probabilityPercentage), total: 100)
                    .tint(AppTheme.positive)
                    .accessibilityLabel("\(goal.title) readiness")
                    .accessibilityValue("\(goal.probabilityPercentage) percent")
            }
        }
        .padding(20)
        .futureMeCard()
    }
}

private struct Header: View {
    let name: String
    let householdName: String

    var body: some View {
        HStack(spacing: 12) {
            Text("FM")
                .font(.caption.bold())
                .foregroundStyle(AppTheme.mint)
                .frame(width: 42, height: 42)
                .background(AppTheme.forest)
                .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            VStack(alignment: .leading, spacing: 3) {
                Eyebrow("FINANCIAL DIGITAL TWIN")
                Text("Good evening, \(name)")
                    .font(.title2.bold())
                    .foregroundStyle(AppTheme.ink)
                Text(householdName)
                    .font(.caption2)
                    .foregroundStyle(AppTheme.muted)
            }
            Spacer()
            Text("JD")
                .font(.caption2.bold())
                .foregroundStyle(AppTheme.forest)
                .frame(width: 38, height: 38)
                .overlay(Circle().stroke(AppTheme.line))
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 18)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("FutureMe Financial. Good evening, \(name). \(householdName).")
    }
}

private struct HealthCard: View {
    let dashboard: DashboardSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 22) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Eyebrow("FINANCIAL HEALTH", light: true)
                    Text(dashboard.healthScore.label)
                        .font(.title3.bold())
                        .foregroundStyle(.white)
                    Text(dashboard.healthScore.summary)
                        .font(.caption2)
                        .foregroundStyle(Color(red: 172 / 255, green: 217 / 255, blue: 200 / 255))
                }
                Spacer()
                Text("Updated today")
                    .font(.caption2.bold())
                    .foregroundStyle(Color(red: 172 / 255, green: 217 / 255, blue: 200 / 255))
                    .padding(.horizontal, 9)
                    .padding(.vertical, 6)
                    .background(Color(red: 33 / 255, green: 74 / 255, blue: 62 / 255))
                    .clipShape(Capsule())
            }
            HStack(spacing: 23) {
                ZStack {
                    Circle()
                        .stroke(Color(red: 49 / 255, green: 80 / 255, blue: 71 / 255), lineWidth: 11)
                    Circle()
                        .trim(from: 0, to: Double(dashboard.healthScore.value) / 100)
                        .stroke(AppTheme.mint, style: StrokeStyle(lineWidth: 11, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                    VStack(spacing: 0) {
                        Text("\(dashboard.healthScore.value)")
                            .font(.system(size: 31, weight: .bold, design: .rounded))
                            .foregroundStyle(.white)
                        Text("out of 100")
                            .font(.system(size: 8))
                            .foregroundStyle(Color(red: 145 / 255, green: 169 / 255, blue: 159 / 255))
                    }
                }
                .frame(width: 112, height: 112)
                .accessibilityElement(children: .ignore)
                .accessibilityLabel("Financial health score")
                .accessibilityValue("\(dashboard.healthScore.value) out of 100")

                VStack(alignment: .leading, spacing: 13) {
                    Text("Your plan is resilient, with room to make one meaningful move.")
                        .font(.caption)
                        .foregroundStyle(Color(red: 185 / 255, green: 206 / 255, blue: 197 / 255))
                    HStack(spacing: 20) {
                        MiniStat(label: "MONTHLY CUSHION", value: money(dashboard.monthlyCashFlow))
                        MiniStat(label: "CASH RUNWAY", value: oneDecimal(dashboard.emergencyFundMonths) + " mo")
                    }
                }
            }
        }
        .padding(22)
        .futureMeCard(fill: AppTheme.forest, showsBorder: false)
    }
}

private struct ProjectionCard: View {
    let result: ScenarioResult

    private var chartPoints: [ProjectionPoint] {
        result.projections
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Eyebrow("FIVE-YEAR OUTLOOK")
                    Text(moneyCompact(result.projectedNetWorth5Years))
                        .font(.title2.bold())
                        .foregroundStyle(AppTheme.ink)
                }
                Spacer()
                Text(signedMoney(result.netWorthDelta5Years) + " vs plan")
                    .font(.caption2.bold())
                    .foregroundStyle(result.netWorthDelta5Years >= 0 ? AppTheme.positive : AppTheme.warning)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 6)
                    .background(result.netWorthDelta5Years >= 0 ? AppTheme.softMint : Color.orange.opacity(0.12))
                    .clipShape(Capsule())
            }

            Chart {
                ForEach(chartPoints, id: \.year) { point in
                    LineMark(
                        x: .value("Year", point.year),
                        y: .value("Current plan", point.baselineNetWorth)
                    )
                    .foregroundStyle(Color.gray.opacity(0.55))
                    .lineStyle(StrokeStyle(lineWidth: 2, dash: [5, 5]))

                    LineMark(
                        x: .value("Year", point.year),
                        y: .value("Selected scenario", point.scenarioNetWorth)
                    )
                    .foregroundStyle(AppTheme.positive)
                    .lineStyle(StrokeStyle(lineWidth: 2.5, lineCap: .round))
                }
            }
            .chartXAxis {
                AxisMarks(values: chartPoints.map(\.year)) { value in
                    AxisValueLabel {
                        if let year = value.as(Int.self) {
                            Text(year == 0 ? "Now" : "\(year)Y")
                        }
                    }
                    .font(.system(size: 8))
                    AxisGridLine(stroke: StrokeStyle(lineWidth: 0.5, dash: [3, 4]))
                        .foregroundStyle(AppTheme.line)
                }
            }
            .chartYAxis(.hidden)
            .frame(height: 155)
            .accessibilityLabel("Five-year net worth projection")
            .accessibilityValue(
                "Selected scenario \(money(result.projectedNetWorth5Years)), "
                    + "\(signedMoney(result.netWorthDelta5Years)) versus current plan."
            )

            HStack {
                Spacer()
                Legend(label: "Selected scenario", color: AppTheme.positive)
                Legend(label: "Current plan", color: .gray.opacity(0.6))
            }
        }
        .padding(20)
        .futureMeCard()
    }
}

private struct MetricsGrid: View {
    let dashboard: DashboardSnapshot

    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10),
    ]

    var body: some View {
        LazyVGrid(columns: columns, spacing: 10) {
            MetricCard(code: "NW", label: "Current net worth", value: money(dashboard.currentNetWorth))
            MetricCard(code: "EF", label: "Emergency fund", value: oneDecimal(dashboard.emergencyFundMonths) + " months")
            MetricCard(code: "DF", label: "Debt-free in", value: "\(dashboard.debtPayoffMonths?.intValue ?? 0) months")
            MetricCard(code: "5Y", label: "Five-year outlook", value: moneyCompact(dashboard.projectedNetWorth5Years))
        }
    }
}

private struct MetricCard: View {
    let code: String
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(code)
                .font(.caption2.bold())
                .foregroundStyle(AppTheme.positive)
                .frame(width: 33, height: 33)
                .background(AppTheme.softMint)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            Text(label)
                .font(.caption2)
                .foregroundStyle(AppTheme.muted)
                .padding(.top, 6)
            Text(value)
                .font(.subheadline.bold())
                .foregroundStyle(AppTheme.ink)
        }
        .frame(maxWidth: .infinity, minHeight: 105, alignment: .leading)
        .padding(15)
        .futureMeCard()
    }
}

private struct AlertsCard: View {
    let alerts: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 9) {
            Eyebrow("PRIORITY SIGNALS")
            Text("What deserves attention")
                .font(.subheadline.bold())
                .foregroundStyle(AppTheme.ink)
            if alerts.isEmpty {
                Text("No urgent alerts. Your plan is within the configured thresholds.")
                    .font(.caption)
                    .foregroundStyle(AppTheme.muted)
            } else {
                ForEach(alerts, id: \.self) { alert in
                    Label(alert, systemImage: "exclamationmark.circle")
                        .font(.caption)
                        .foregroundStyle(AppTheme.muted)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .futureMeCard()
        .accessibilityElement(children: .combine)
    }
}

private struct ScenarioCarousel: View {
    let scenarios: [ScenarioCardModel]
    let selected: ScenarioCardModel
    let onSelect: (ScenarioCardModel) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            LazyHStack(spacing: 11) {
                ForEach(scenarios) { scenario in
                    Button {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            onSelect(scenario)
                        }
                    } label: {
                        VStack(alignment: .leading, spacing: 7) {
                            Text(scenario.code)
                                .font(.caption2.bold())
                                .foregroundStyle(AppTheme.positive)
                                .frame(width: 38, height: 38)
                                .background(AppTheme.softMint)
                                .clipShape(RoundedRectangle(cornerRadius: 11))
                            Text(scenario.title)
                                .font(.subheadline.bold())
                                .foregroundStyle(AppTheme.ink)
                                .padding(.top, 4)
                            Text(scenario.subtitle)
                                .font(.caption2)
                                .foregroundStyle(AppTheme.muted)
                                .lineLimit(2)
                            Spacer()
                            Text("SIMULATE  >")
                                .font(.caption2.bold())
                                .foregroundStyle(AppTheme.positive)
                        }
                        .frame(width: 180, height: 130, alignment: .leading)
                        .padding(16)
                        .futureMeCard()
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(
                                    selected.id == scenario.id ? AppTheme.positive : .clear,
                                    lineWidth: 2
                                )
                        )
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(
                        "\(selected.id == scenario.id ? "Selected scenario" : "Simulate") \(scenario.title)"
                    )
                    .accessibilityHint(scenario.subtitle)
                    .accessibilityAddTraits(selected.id == scenario.id ? .isSelected : [])
                }
            }
            .padding(.horizontal, 20)
        }
    }
}

private struct InsightCard: View {
    let result: ScenarioResult

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("FUTUREME INSIGHT", systemImage: "sparkles")
                .font(.caption2.bold())
                .foregroundStyle(AppTheme.positive)
            Text(result.scenario.title)
                .font(.title3.bold())
                .foregroundStyle(AppTheme.ink)
            Text(result.recommendation)
                .font(.caption)
                .foregroundStyle(Color(red: 83 / 255, green: 99 / 255, blue: 91 / 255))
            ForEach(Array(result.tradeoffs.prefix(3)), id: \.self) { tradeoff in
                HStack(alignment: .top, spacing: 8) {
                    Circle()
                        .fill(AppTheme.positive)
                        .frame(width: 5, height: 5)
                        .padding(.top, 5)
                    Text(tradeoff)
                        .font(.caption2)
                        .foregroundStyle(AppTheme.muted)
                }
            }
            HStack(spacing: 8) {
                ImpactTile(label: "MONTHLY IMPACT", value: signedMoney(result.monthlyCashFlowImpact), positive: result.monthlyCashFlowImpact >= 0)
                ImpactTile(label: "5-YEAR CHANGE", value: signedMoney(result.netWorthDelta5Years), positive: result.netWorthDelta5Years >= 0)
            }
            HStack(spacing: 8) {
                ImpactTile(label: "RISK SCORE", value: "\(result.riskScore.value)/100", positive: result.riskScore.value < 45)
                ImpactTile(label: "CASH RUNWAY", value: oneDecimal(result.emergencyFundMonths) + " mo", positive: result.emergencyFundMonths >= 6)
            }
        }
        .padding(20)
        .futureMeCard(fill: AppTheme.surfaceVariant, showsBorder: false)
    }
}

private struct ImpactTile: View {
    let label: String
    let value: String
    let positive: Bool

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.system(size: 8, weight: .medium))
                .foregroundStyle(AppTheme.muted)
            Text(value)
                .font(.subheadline.bold())
                .foregroundStyle(positive ? AppTheme.positive : AppTheme.warning)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 13)
        .background(AppTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 13))
        .overlay(RoundedRectangle(cornerRadius: 13).stroke(AppTheme.line))
    }
}

private struct RiskExplanationCard: View {
    let result: ScenarioResult

    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            HStack(spacing: 14) {
                VStack(spacing: 0) {
                    Text("\(result.riskScore.value)")
                        .font(.title2.bold())
                        .foregroundStyle(result.riskScore.value >= 50 ? AppTheme.warning : AppTheme.positive)
                    Text("/100")
                        .font(.system(size: 8))
                        .foregroundStyle(AppTheme.muted)
                }
                .frame(width: 64, height: 64)
                .background(result.riskScore.value >= 50 ? AppTheme.warning.opacity(0.14) : AppTheme.softMint)
                .clipShape(RoundedRectangle(cornerRadius: 17))

                VStack(alignment: .leading, spacing: 4) {
                    Eyebrow("TRANSPARENT RISK MODEL")
                    Text("\(result.riskScore.level.name.capitalized) risk")
                        .font(.title3.bold())
                        .foregroundStyle(AppTheme.ink)
                    Text(result.riskScore.summary)
                        .font(.caption)
                        .foregroundStyle(AppTheme.muted)
                }
            }

            VStack(spacing: 1) {
                ForEach(result.riskScore.factors, id: \.id) { factor in
                    HStack(alignment: .center, spacing: 10) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(factor.title)
                                .font(.caption.bold())
                                .foregroundStyle(AppTheme.ink)
                            Text(factor.explanation)
                                .font(.caption2)
                                .foregroundStyle(AppTheme.muted)
                        }
                        Spacer()
                        Text("\(factor.points > 0 ? "+" : "")\(factor.points)")
                            .font(.caption.bold())
                            .foregroundStyle(factor.points < 0 ? AppTheme.positive : AppTheme.warning)
                    }
                    .padding(12)
                    .background(AppTheme.surfaceVariant)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 13))
        }
        .padding(20)
        .futureMeCard()
        .accessibilityElement(children: .combine)
        .accessibilityLabel(
            "\(result.riskScore.level.name.capitalized) risk, \(result.riskScore.value) out of 100. \(result.riskScore.summary)"
        )
    }
}

private struct ComparisonCard: View {
    let comparison: ScenarioComparison

    var body: some View {
        VStack(spacing: 16) {
            ComparisonProjectionChart(comparison: comparison)
                .frame(height: 170)
            ComparisonOption(
                label: "OPTION A",
                result: comparison.left,
                preferred: comparison.preferredScenarioId == comparison.left.scenario.id
            )
            Divider().overlay(AppTheme.line)
            ComparisonOption(
                label: "OPTION B",
                result: comparison.right,
                preferred: comparison.preferredScenarioId == comparison.right.scenario.id
            )
            Label(comparison.summary, systemImage: "sparkles")
                .font(.caption2)
                .foregroundStyle(Color(red: 83 / 255, green: 99 / 255, blue: 91 / 255))
                .padding(13)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(AppTheme.softMint)
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .padding(18)
        .futureMeCard()
    }
}

private struct ComparisonProjectionChart: View {
    let comparison: ScenarioComparison

    var body: some View {
        Chart {
            ForEach(comparison.left.projections, id: \.year) { point in
                LineMark(
                    x: .value("Year", point.year),
                    y: .value(comparison.left.scenario.title, point.scenarioNetWorth)
                )
                .foregroundStyle(by: .value("Scenario", comparison.left.scenario.title))
                .lineStyle(StrokeStyle(lineWidth: 2.5, lineCap: .round))
            }
            ForEach(comparison.right.projections, id: \.year) { point in
                LineMark(
                    x: .value("Year", point.year),
                    y: .value(comparison.right.scenario.title, point.scenarioNetWorth)
                )
                .foregroundStyle(by: .value("Scenario", comparison.right.scenario.title))
                .lineStyle(StrokeStyle(lineWidth: 2.5, lineCap: .round))
            }
        }
        .chartForegroundStyleScale([
            comparison.left.scenario.title: AppTheme.positive,
            comparison.right.scenario.title: Color.purple.opacity(0.8),
        ])
        .chartYAxis(.hidden)
        .chartXAxis {
            AxisMarks(values: comparison.left.projections.map(\.year)) { value in
                AxisValueLabel {
                    if let year = value.as(Int.self) {
                        Text(year == 0 ? "Now" : "\(year)Y")
                    }
                }
                .font(.system(size: 8))
                AxisGridLine(stroke: StrokeStyle(lineWidth: 0.5, dash: [3, 4]))
                    .foregroundStyle(AppTheme.line)
            }
        }
        .accessibilityLabel(
            "Five-year net worth comparison for \(comparison.left.scenario.title) and \(comparison.right.scenario.title)"
        )
    }
}

private struct ComparisonOption: View {
    let label: String
    let result: ScenarioResult
    let preferred: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Eyebrow(label)
                Text(result.scenario.title)
                    .font(.subheadline.bold())
                    .foregroundStyle(AppTheme.ink)
                Text(moneyCompact(result.projectedNetWorth5Years) + " projected net worth")
                    .font(.caption2)
                    .foregroundStyle(AppTheme.muted)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 5) {
                if preferred {
                    Label("RECOMMENDED", systemImage: "sparkles")
                        .font(.system(size: 8, weight: .bold))
                        .foregroundStyle(AppTheme.positive)
                }
                Text(signedMoney(result.netWorthDelta5Years) + " / 5Y")
                    .font(.caption.bold())
                    .foregroundStyle(result.netWorthDelta5Years >= 0 ? AppTheme.positive : AppTheme.warning)
                Text("Risk \(result.riskScore.value)/100")
                    .font(.caption2)
                    .foregroundStyle(AppTheme.muted)
            }
        }
    }
}

private struct SectionTitle: View {
    let eyebrow: String
    let title: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Eyebrow(eyebrow)
            Text(title)
                .font(.title2.bold())
                .foregroundStyle(AppTheme.ink)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct Eyebrow: View {
    let text: String
    let light: Bool

    init(_ text: String, light: Bool = false) {
        self.text = text
        self.light = light
    }

    var body: some View {
        Text(text)
            .font(.system(size: 9, weight: .bold))
            .tracking(1.2)
            .foregroundStyle(light ? Color(red: 146 / 255, green: 182 / 255, blue: 169 / 255) : AppTheme.muted)
    }
}

private struct MiniStat: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(label)
                .font(.system(size: 7))
                .tracking(0.6)
                .foregroundStyle(Color(red: 128 / 255, green: 156 / 255, blue: 145 / 255))
            Text(value)
                .font(.caption.bold())
                .foregroundStyle(.white)
        }
    }
}

private struct Legend: View {
    let label: String
    let color: Color

    var body: some View {
        HStack(spacing: 5) {
            Rectangle().fill(color).frame(width: 14, height: 2)
            Text(label).font(.system(size: 8)).foregroundStyle(AppTheme.muted)
        }
    }
}

private struct LoadingView: View {
    var body: some View {
        VStack(spacing: 14) {
            ProgressView()
                .tint(AppTheme.positive)
                .scaleEffect(1.2)
            Text("Building your financial twin")
                .font(.title3.bold())
                .foregroundStyle(AppTheme.ink)
            Text("Loading profile, scenarios, and projections.")
                .font(.caption)
                .foregroundStyle(AppTheme.muted)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.canvas)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Loading financial dashboard")
    }
}

private struct StateView: View {
    let title: String
    let message: String
    let action: String
    let onAction: () -> Void

    var body: some View {
        VStack(spacing: 13) {
            Image(systemName: "exclamationmark.circle")
                .font(.largeTitle)
                .foregroundStyle(AppTheme.warning)
            Text(title)
                .font(.title2.bold())
                .foregroundStyle(AppTheme.ink)
            Text(message)
                .font(.caption)
                .multilineTextAlignment(.center)
                .foregroundStyle(AppTheme.muted)
            Button(action, action: onAction)
                .buttonStyle(.borderedProminent)
                .tint(AppTheme.forest)
                .padding(.top, 5)
                .accessibilityHint("Attempts to reload the financial workspace")
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.canvas)
    }
}

private struct ScenarioListTab: View {
    let scenarios: [ScenarioCardModel]
    let selected: ScenarioCardModel
    let onSelect: (ScenarioCardModel) -> Void
    let openOverview: () -> Void

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    SectionTitle(eyebrow: "WHAT-IF SCENARIOS", title: "Model a major decision")
                        .padding(.bottom, 4)
                    ForEach(scenarios) { scenario in
                        Button {
                            onSelect(scenario)
                            openOverview()
                        } label: {
                            HStack(spacing: 14) {
                                Text(scenario.code)
                                    .font(.caption2.bold())
                                    .foregroundStyle(AppTheme.positive)
                                    .frame(width: 44, height: 44)
                                    .background(AppTheme.softMint)
                                    .clipShape(RoundedRectangle(cornerRadius: 12))
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(scenario.title)
                                        .font(.headline)
                                        .foregroundStyle(AppTheme.ink)
                                    Text(scenario.subtitle)
                                        .font(.caption)
                                        .foregroundStyle(AppTheme.muted)
                                        .multilineTextAlignment(.leading)
                                }
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .foregroundStyle(AppTheme.muted)
                            }
                            .padding(16)
                            .futureMeCard()
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Simulate \(scenario.title)")
                        .accessibilityHint(scenario.subtitle)
                        .accessibilityAddTraits(selected.id == scenario.id ? .isSelected : [])
                    }
                }
                .padding(20)
            }
            .background(AppTheme.canvas)
            .navigationTitle("Scenarios")
        }
    }
}

private struct PlanningTab: View {
    let content: FutureMeDashboardContent
    let onPlanScenario: (String) -> Void
    let onAsk: (String) -> Void

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    SectionTitle(
                        eyebrow: "PROACTIVE INSIGHTS",
                        title: "Every signal worth reviewing"
                    )
                    ForEach(content.insights, id: \.id) { insight in
                        InsightDetailCard(insight: insight)
                    }

                    FinancialGpsDetail(gps: content.financialGps) {
                        onAsk("How can I improve my 5-year outlook?")
                    }
                    .padding(.top, 18)

                    SectionTitle(
                        eyebrow: "GOAL READINESS",
                        title: "What stands between you and each goal"
                    )
                    .padding(.top, 18)
                    ForEach(content.goals, id: \.id) { goal in
                        NavigationLink {
                            GoalDetail(goal: goal)
                        } label: {
                            GoalSummaryCard(goal: goal)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(
                            "\(goal.title), \(goal.probabilityPercentage) percent ready"
                        )
                    }

                    SectionTitle(
                        eyebrow: "LIFE EVENT PLANNER",
                        title: "Plan the moments that change everything"
                    )
                    .padding(.top, 18)
                    ForEach(content.lifeEvents, id: \.id) { event in
                        NavigationLink {
                            LifeEventDetail(
                                event: event,
                                onPlanScenario: onPlanScenario
                            )
                        } label: {
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Text(event.type.name.replacingOccurrences(of: "_", with: " "))
                                        .font(.system(size: 8, weight: .bold))
                                        .foregroundStyle(AppTheme.positive)
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundStyle(AppTheme.muted)
                                }
                                Text(event.title)
                                    .font(.headline)
                                    .foregroundStyle(AppTheme.ink)
                                Text(event.subtitle)
                                    .font(.caption)
                                    .foregroundStyle(AppTheme.muted)
                                    .multilineTextAlignment(.leading)
                                Text(
                                    "Monthly \(signedMoney(-event.estimatedMonthlyImpact)) · "
                                        + "Upfront \(money(event.oneTimeCostLow))–\(money(event.oneTimeCostHigh))"
                                )
                                .font(.caption2.bold())
                                .foregroundStyle(AppTheme.warning)
                            }
                            .padding(16)
                            .futureMeCard()
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Plan \(event.title)")
                    }

                    SectionTitle(
                        eyebrow: "MONEY LEAK DETECTOR",
                        title: "Keep more of what you earn"
                    )
                    .padding(.top, 18)

                    ForEach(content.moneyLeaks, id: \.id) { leak in
                        VStack(alignment: .leading, spacing: 7) {
                            HStack {
                                Text(leak.title)
                                    .font(.subheadline.bold())
                                    .foregroundStyle(AppTheme.ink)
                                Spacer()
                                Text(money(leak.estimatedMonthlyLoss) + "/mo")
                                    .font(.caption.bold())
                                    .foregroundStyle(AppTheme.warning)
                            }
                            Text(leak.summary)
                                .font(.caption)
                                .foregroundStyle(AppTheme.muted)
                            Text("Five-year impact " + money(leak.estimatedFiveYearLoss))
                                .font(.caption2)
                                .foregroundStyle(AppTheme.muted)
                            Text(leak.fixRecommendation)
                                .font(.caption2.bold())
                                .foregroundStyle(AppTheme.positive)
                        }
                        .padding(16)
                        .futureMeCard()
                        .accessibilityElement(children: .combine)
                    }
                }
                .padding(20)
            }
            .background(AppTheme.canvas)
            .navigationTitle("Plan")
        }
    }
}

private struct InsightDetailCard: View {
    let insight: Insight

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            HStack {
                Eyebrow(insight.category.name.replacingOccurrences(of: "_", with: " "))
                Spacer()
                if insight.estimatedDollarImpact > 0 {
                    Text(money(insight.estimatedDollarImpact))
                        .font(.caption.bold())
                        .foregroundStyle(AppTheme.positive)
                }
            }
            Text(insight.title)
                .font(.headline)
                .foregroundStyle(AppTheme.ink)
            Text(insight.summary)
                .font(.caption)
                .foregroundStyle(AppTheme.muted)
            Text(insight.recommendedAction)
                .font(.caption2.bold())
                .foregroundStyle(AppTheme.positive)
        }
        .padding(16)
        .futureMeCard()
        .accessibilityElement(children: .combine)
    }
}

private struct FinancialGpsDetail: View {
    let gps: FinancialGpsResult
    let onExplain: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 13) {
            SectionTitle(
                eyebrow: "FINANCIAL GPS",
                title: "Current route versus improved route"
            )
            HStack(spacing: 9) {
                GpsMetric(label: "CURRENT TRAJECTORY", value: moneyCompact(gps.currentFiveYearNetWorth))
                GpsMetric(label: "IMPROVED TRAJECTORY", value: moneyCompact(gps.improvedFiveYearNetWorth))
            }
            Chart {
                ForEach(gps.currentTrajectory, id: \.year) { point in
                    LineMark(
                        x: .value("Year", point.year),
                        y: .value("Current route", point.scenarioNetWorth)
                    )
                    .foregroundStyle(by: .value("Route", "Current route"))
                    .lineStyle(StrokeStyle(lineWidth: 2, lineCap: .round))
                }
                ForEach(gps.improvedTrajectory, id: \.year) { point in
                    LineMark(
                        x: .value("Year", point.year),
                        y: .value("Improved route", point.scenarioNetWorth)
                    )
                    .foregroundStyle(by: .value("Route", "Improved route"))
                    .lineStyle(StrokeStyle(lineWidth: 2.5, lineCap: .round))
                }
            }
            .chartForegroundStyleScale([
                "Current route": Color.gray.opacity(0.7),
                "Improved route": AppTheme.positive,
            ])
            .chartYAxis(.hidden)
            .frame(height: 175)
            .accessibilityLabel("Five-year Financial GPS current and improved trajectories")

            Text("Potential five-year lift: " + money(gps.difference))
                .font(.headline)
                .foregroundStyle(AppTheme.positive)
            Text(gps.explanation)
                .font(.caption)
                .foregroundStyle(AppTheme.muted)
            ForEach(gps.monthlyActionPlan, id: \.self) { action in
                Label(action, systemImage: "arrow.turn.down.right")
                    .font(.caption)
                    .foregroundStyle(AppTheme.ink)
            }
            Button("Explain my improved route", action: onExplain)
                .buttonStyle(.borderedProminent)
                .tint(AppTheme.forest)
                .frame(maxWidth: .infinity)
        }
        .padding(18)
        .futureMeCard(fill: AppTheme.softMint, showsBorder: false)
    }
}

private struct GoalSummaryCard: View {
    let goal: GoalProbabilityResult

    var body: some View {
        VStack(alignment: .leading, spacing: 9) {
            HStack {
                Text(goal.title)
                    .font(.headline)
                    .foregroundStyle(AppTheme.ink)
                Spacer()
                Text("\(goal.probabilityPercentage)%")
                    .font(.title3.bold())
                    .foregroundStyle(AppTheme.positive)
            }
            ProgressView(value: Double(goal.probabilityPercentage), total: 100)
                .tint(AppTheme.positive)
            Text(goal.explanation)
                .font(.caption)
                .foregroundStyle(AppTheme.muted)
            HStack {
                Text("Modeled ready \(goal.projectedReadyDate)")
                    .font(.caption2)
                    .foregroundStyle(AppTheme.muted)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundStyle(AppTheme.muted)
            }
        }
        .padding(16)
        .futureMeCard()
    }
}

private struct GoalDetail: View {
    let goal: GoalProbabilityResult

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Eyebrow(goal.type.name.replacingOccurrences(of: "_", with: " "))
                Text(goal.title)
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTheme.ink)
                Text("\(goal.probabilityPercentage)% ready")
                    .font(.title3.bold())
                    .foregroundStyle(AppTheme.positive)
                ProgressView(value: Double(goal.probabilityPercentage), total: 100)
                    .tint(AppTheme.positive)
                Text(goal.explanation)
                    .foregroundStyle(AppTheme.muted)

                GoalListCard(title: "BLOCKERS", items: goal.blockers)
                GoalListCard(title: "RECOMMENDED ACTIONS", items: goal.recommendedActions)

                HStack(spacing: 10) {
                    GpsMetric(
                        label: "MONTHLY IMPROVEMENT",
                        value: money(goal.requiredMonthlyImprovement)
                    )
                    GpsMetric(
                        label: "MODELED READY",
                        value: goal.projectedReadyDate
                    )
                }
            }
            .padding(20)
        }
        .background(AppTheme.canvas)
        .navigationTitle("Goal plan")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct GoalListCard: View {
    let title: String
    let items: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 9) {
            Eyebrow(title)
            if items.isEmpty {
                Text("No blockers detected in the current model.")
                    .font(.callout)
                    .foregroundStyle(AppTheme.muted)
            } else {
                ForEach(items, id: \.self) { item in
                    Label(item, systemImage: "checkmark.circle")
                        .font(.callout)
                        .foregroundStyle(AppTheme.ink)
                }
            }
        }
        .padding(18)
        .futureMeCard()
    }
}

private struct LifeEventDetail: View {
    let event: LifeEventPlan
    let onPlanScenario: (String) -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Eyebrow(event.type.name.replacingOccurrences(of: "_", with: " "))
                Text(event.title)
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTheme.ink)
                Text(event.subtitle)
                    .foregroundStyle(AppTheme.muted)

                HStack(spacing: 10) {
                    GpsMetric(
                        label: "MONTHLY IMPACT",
                        value: signedMoney(-event.estimatedMonthlyImpact)
                    )
                    GpsMetric(
                        label: "RISK IMPACT",
                        value: "+\(event.riskImpact)"
                    )
                }

                VStack(alignment: .leading, spacing: 9) {
                    Eyebrow("PREPARATION PLAN")
                    ForEach(event.recommendedPreparationSteps, id: \.self) { step in
                        Label(step, systemImage: "checkmark.circle")
                            .font(.callout)
                            .foregroundStyle(AppTheme.ink)
                    }
                }
                .padding(18)
                .futureMeCard()

                if !event.relatedInsights.isEmpty {
                    VStack(alignment: .leading, spacing: 9) {
                        Eyebrow("RELATED INSIGHTS")
                        ForEach(event.relatedInsights, id: \.self) { insight in
                            Label(insight, systemImage: "lightbulb")
                                .font(.callout)
                                .foregroundStyle(AppTheme.ink)
                        }
                    }
                    .padding(18)
                    .futureMeCard()
                }

                Button("Plan this event") {
                    if let scenarioId = event.suggestedScenarioIds.first {
                        onPlanScenario(scenarioId)
                    }
                }
                    .buttonStyle(.borderedProminent)
                    .tint(AppTheme.forest)
                    .frame(maxWidth: .infinity)
                    .disabled(event.suggestedScenarioIds.isEmpty)
                    .accessibilityHint("Uses the linked deterministic scenarios")

                Text("Suggested scenarios: " + event.suggestedScenarioIds.joined(separator: ", "))
                    .font(.caption)
                    .foregroundStyle(AppTheme.muted)
            }
            .padding(20)
        }
        .background(AppTheme.canvas)
        .navigationTitle("Event plan")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct ComparisonTab: View {
    let comparison: ScenarioComparison
    let scenarios: [ScenarioCardModel]
    let disclaimer: String
    let onCompare: (ScenarioCardModel, ScenarioCardModel) -> Void
    @State private var leftId: String
    @State private var rightId: String

    init(
        comparison: ScenarioComparison,
        scenarios: [ScenarioCardModel],
        disclaimer: String,
        onCompare: @escaping (ScenarioCardModel, ScenarioCardModel) -> Void
    ) {
        self.comparison = comparison
        self.scenarios = scenarios
        self.disclaimer = disclaimer
        self.onCompare = onCompare
        _leftId = State(initialValue: comparison.left.scenario.id)
        _rightId = State(initialValue: comparison.right.scenario.id)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    SectionTitle(eyebrow: "SIDE BY SIDE", title: "Compare two paths")
                    VStack(spacing: 10) {
                        Picker("Option A", selection: $leftId) {
                            ForEach(scenarios.filter { $0.id != rightId }) { scenario in
                                Text(scenario.title).tag(scenario.id)
                            }
                        }
                        .accessibilityLabel("Option A scenario")
                        .onChange(of: leftId) { newValue in
                            requestComparison(leftId: newValue, rightId: rightId)
                        }

                        Picker("Option B", selection: $rightId) {
                            ForEach(scenarios.filter { $0.id != leftId }) { scenario in
                                Text(scenario.title).tag(scenario.id)
                            }
                        }
                        .accessibilityLabel("Option B scenario")
                        .onChange(of: rightId) { newValue in
                            requestComparison(leftId: leftId, rightId: newValue)
                        }
                    }
                    .pickerStyle(.menu)
                    .padding(14)
                    .futureMeCard()
                    ComparisonCard(comparison: comparison)
                    Label(disclaimer, systemImage: "shield.checkered")
                        .font(.caption2)
                        .foregroundStyle(AppTheme.muted)
                }
                .padding(20)
            }
            .background(AppTheme.canvas)
            .navigationTitle("Comparison")
        }
    }

    private func requestComparison(leftId: String, rightId: String) {
        guard
            leftId != rightId,
            let left = scenarios.first(where: { $0.id == leftId }),
            let right = scenarios.first(where: { $0.id == rightId })
        else {
            return
        }
        onCompare(left, right)
    }
}

private struct AssistantTab: View {
    let messages: [AssistantMessage]
    let suggestions: [SuggestedQuestion]
    let onAsk: (String) -> Void
    @State private var question = ""

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(suggestions, id: \.id) { suggestion in
                                    Button(suggestion.title) {
                                        onAsk(suggestion.prompt)
                                    }
                                    .buttonStyle(.bordered)
                                    .tint(AppTheme.positive)
                                    .accessibilityLabel("Ask \(suggestion.title)")
                                }
                            }
                        }
                        .padding(.bottom, 6)

                        ForEach(messages) { message in
                            HStack {
                                if message.isUser { Spacer(minLength: 42) }
                                Text(message.text)
                                    .font(.callout)
                                    .foregroundStyle(message.isUser ? Color.white : AppTheme.ink)
                                    .padding(14)
                                    .background(message.isUser ? AppTheme.forest : AppTheme.surface)
                                    .clipShape(RoundedRectangle(cornerRadius: 17))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 17)
                                            .stroke(message.isUser ? Color.clear : AppTheme.line)
                                    )
                                if !message.isUser { Spacer(minLength: 42) }
                            }
                            .accessibilityElement(children: .combine)
                            .accessibilityLabel(
                                "\(message.isUser ? "Your question" : "FutureMe response"): \(message.text)"
                            )
                        }
                    }
                    .padding(20)
                }

                HStack(alignment: .bottom, spacing: 10) {
                    TextField("Ask a financial what-if question", text: $question, axis: .vertical)
                        .textFieldStyle(.roundedBorder)
                        .lineLimit(1...4)
                        .accessibilityLabel("Financial assistant question")
                    Button {
                        onAsk(question)
                        question = ""
                    } label: {
                        Image(systemName: "arrow.up")
                            .font(.headline.bold())
                            .frame(width: 38, height: 38)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(AppTheme.forest)
                    .disabled(question.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    .accessibilityLabel("Send question to FutureMe")
                }
                .padding(16)
                .background(AppTheme.surface)
            }
            .background(AppTheme.canvas)
            .navigationTitle("Ask FutureMe")
        }
    }
}

private func money(_ value: Double) -> String {
    value.formatted(.currency(code: "USD").precision(.fractionLength(0)))
}

private func moneyCompact(_ value: Double) -> String {
    let magnitude = abs(value)
    let divisor: Double
    let suffix: String

    switch magnitude {
    case 1_000_000_000...:
        divisor = 1_000_000_000
        suffix = "B"
    case 1_000_000...:
        divisor = 1_000_000
        suffix = "M"
    case 1_000...:
        divisor = 1_000
        suffix = "K"
    default:
        return money(value)
    }

    let formatter = NumberFormatter()
    formatter.numberStyle = .currency
    formatter.currencyCode = "USD"
    formatter.maximumFractionDigits = magnitude / divisor < 10 ? 1 : 0
    formatter.minimumFractionDigits = 0
    return (formatter.string(from: NSNumber(value: value / divisor)) ?? money(value)) + suffix
}

private func signedMoney(_ value: Double) -> String {
    (value >= 0 ? "+" : "-") + money(abs(value))
}

private func oneDecimal(_ value: Double) -> String {
    value.formatted(.number.precision(.fractionLength(1)))
}
