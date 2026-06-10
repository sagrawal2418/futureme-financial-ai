package com.futureme.financialai.usecase

import com.futureme.financialai.domain.FinancialCalculator
import com.futureme.financialai.model.FinancialWorkspace
import com.futureme.financialai.model.Scenario
import com.futureme.financialai.model.ScenarioComparison
import com.futureme.financialai.model.ScenarioResult
import com.futureme.financialai.repository.FinancialRepository

class LoadFinancialWorkspaceUseCase(
    private val repository: FinancialRepository,
    private val calculator: FinancialCalculator,
) {
    operator fun invoke(): FinancialWorkspace {
        val profile = repository.getProfile()
        val scenarios = repository.getScenarios()
        require(scenarios.size >= 2) {
            "At least two demo scenarios are required for comparison mode."
        }
        val selected = scenarios.first()
        return FinancialWorkspace(
            profile = profile,
            dashboard = calculator.dashboard(profile),
            scenarios = scenarios,
            selectedScenario = selected,
            selectedResult = calculator.simulate(profile, selected),
            comparison = calculator.compare(profile, scenarios[0], scenarios[1]),
        )
    }
}

class SimulateScenarioUseCase(
    private val calculator: FinancialCalculator,
) {
    operator fun invoke(
        profile: com.futureme.financialai.model.FinancialProfile,
        scenario: Scenario,
    ): ScenarioResult = calculator.simulate(profile, scenario)
}

class CompareScenariosUseCase(
    private val calculator: FinancialCalculator,
) {
    operator fun invoke(
        profile: com.futureme.financialai.model.FinancialProfile,
        left: Scenario,
        right: Scenario,
    ): ScenarioComparison = calculator.compare(profile, left, right)
}
