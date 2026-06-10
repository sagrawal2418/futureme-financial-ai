package com.futureme.financialai.data

import com.futureme.financialai.domain.FinancialCalculator
import com.futureme.financialai.repository.FinancialRepository
import com.futureme.financialai.usecase.CompareScenariosUseCase
import com.futureme.financialai.usecase.LoadFinancialWorkspaceUseCase
import com.futureme.financialai.usecase.SimulateScenarioUseCase

class AppContainer {
    private val repository: FinancialRepository = MockFinancialRepository()
    private val explanationProvider = MockFinancialExplanationProvider()
    private val calculator = FinancialCalculator(
        baseline = repository.getBaselineScenario(),
        explanationProvider = explanationProvider,
    )

    val loadFinancialWorkspace = LoadFinancialWorkspaceUseCase(repository, calculator)
    val simulateScenario = SimulateScenarioUseCase(calculator)
    val compareScenarios = CompareScenariosUseCase(calculator)
}
