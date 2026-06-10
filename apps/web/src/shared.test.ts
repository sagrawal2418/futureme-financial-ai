import { describe, expect, it } from "vitest";

import {
  askFutureMe,
  bootstrapProduct,
  compareScenarios,
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
      "RELOCATE",
      "HAVE_CHILD",
      "INCREASE_INVESTMENTS",
    ]));
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
});
