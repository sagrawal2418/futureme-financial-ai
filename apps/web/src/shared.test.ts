import { describe, expect, it } from "vitest";

import {
  askFutureMe,
  bootstrapProduct,
  compareScenarios,
  recordAnalyticsEvent,
  saveDecision,
  simulateScenario,
} from "./shared";

describe("shared Kotlin/JS product bridge", () => {
  it("loads every required scenario family", () => {
    const product = bootstrapProduct();
    const types = new Set(product.scenarios.map((scenario) => scenario.type));

    expect(types).toEqual(new Set([
      "BUY_HOME",
      "REFINANCE_MORTGAGE",
      "PAY_OFF_DEBT",
      "JOB_LOSS",
      "SPOUSE_STOPS_WORKING",
      "RELOCATE",
      "HAVE_CHILD",
      "START_BUSINESS",
      "INCREASE_INVESTMENTS",
    ]));
  });

  it("loads the complete life readiness platform contract", () => {
    const product = bootstrapProduct();

    expect(product.readiness).toHaveLength(7);
    expect(product.readinessPlans).toHaveLength(7);
    expect(product.decisionSimulations).toHaveLength(product.scenarios.length);
    expect(product.lifeTimeline.map((point) => point.monthsFromNow)).toEqual([
      0, 6, 12, 36, 60,
    ]);
    expect(product.executiveDemo.steps).toHaveLength(5);
    expect(product.opportunities.length).toBeGreaterThanOrEqual(8);
    expect(product.nextBestAction.recommendationId).toBe(product.opportunities[0].id);
    expect(product.scenarioImpactHeatmaps).toHaveLength(product.scenarios.length);
    expect(product.bankingVisionDemo.steps).toHaveLength(7);
    expect(product.missions).toHaveLength(8);
    expect(product.missionControl.activeMissions).toHaveLength(8);
    expect(product.missionControl.nextBestAction.estimatedReadinessIncrease).toBeGreaterThan(0);
    expect(product.missionExecution.plans).toHaveLength(8);
    expect(product.missionExecution.plans.every((plan) => plan.actionPlan.actions.length === 4)).toBe(true);
    expect(product.missionExecution.notifications.length).toBeGreaterThan(0);
    expect(product.missionExecution.plans[0].roadmap.stages.map((stage) => stage.label)).toEqual([
      "30 Days", "90 Days", "1 Year",
    ]);
    expect(product.missionAnalytics.trends).toHaveLength(8);
  });

  it("returns projections and explainable risk from the shared engine", () => {
    const result = simulateScenario("buy-home");

    expect(result.projections).toHaveLength(6);
    expect(result.riskScore.factors.length).toBeGreaterThan(0);
    expect(result.recommendation).not.toHaveLength(0);
  });

  it("compares scenarios and answers a scenario-aware question", () => {
    const comparison = compareScenarios("move-to-texas", "stay-in-new-jersey");
    const response = askFutureMe("Can I move to Texas and stay on track?", "move-to-texas");

    expect([
      comparison.left.scenario.id,
      comparison.right.scenario.id,
    ]).toContain(comparison.preferredScenarioId);
    expect(response.answer).toContain("Move to Austin");
  });

  it("stores local banking events and decisions through the shared product", () => {
    const event = recordAnalyticsEvent("recommendation_accepted", "top-action");
    const decision = saveDecision("pay-off-cards");

    expect(event.type).toBe("RECOMMENDATION_ACCEPTED");
    expect(decision.relatedScenarioId).toBe("pay-off-cards");
    expect(decision.status).toBe("PLANNED");
  });
});
