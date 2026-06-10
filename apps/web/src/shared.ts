import * as sharedModule from "futureme-shared";

export type ScenarioType =
  | "BUY_HOME"
  | "REFINANCE_MORTGAGE"
  | "PAY_OFF_DEBT"
  | "JOB_LOSS"
  | "RELOCATE"
  | "HAVE_CHILD"
  | "INCREASE_INVESTMENTS";

export interface UserIdentity {
  ownerId: string;
  displayName: string;
  householdName: string;
}

export interface FinancialProfile {
  profileId: string;
  ownerId: string;
  stateCode: string;
  annualGrossIncome: number;
  monthlyNetIncome: number;
  monthlyLivingExpenses: number;
  liquidSavings: number;
  creditCardDebt: number;
  creditCardApr: number;
  housingPayment: number;
  mortgageBalance: number;
  propertyValue: number;
  investmentBalance: number;
  monthlyRetirementContribution: number;
  monthlyDebtPayments: number;
  dependents: number;
}

export interface Scenario {
  id: string;
  type: ScenarioType;
  title: string;
  subtitle: string;
  tags: string[];
  rationale: string;
}

export interface ProjectionPoint {
  year: number;
  baselineNetWorth: number;
  scenarioNetWorth: number;
}

export interface RiskFactor {
  id: string;
  title: string;
  explanation: string;
  points: number;
}

export interface RiskScore {
  value: number;
  level: "LOW" | "MODERATE" | "ELEVATED" | "HIGH";
  summary: string;
  factors: RiskFactor[];
}

export interface FinancialHealthScore {
  value: number;
  label: string;
  summary: string;
}

export interface ScenarioResult {
  scenario: Scenario;
  monthlyCashFlowImpact: number;
  projectedMonthlySurplus: number;
  baselineNetWorth: number;
  projectedNetWorth1Year: number;
  projectedNetWorth3Years: number;
  projectedNetWorth5Years: number;
  netWorthDelta5Years: number;
  healthScore: FinancialHealthScore;
  riskScore: RiskScore;
  emergencyFundMonths: number;
  debtPayoffMonths: number | null;
  projections: ProjectionPoint[];
  tradeoffs: string[];
  recommendation: string;
}

export interface DashboardSnapshot {
  healthScore: FinancialHealthScore;
  riskScore: RiskScore;
  monthlyCashFlow: number;
  currentNetWorth: number;
  projectedNetWorth5Years: number;
  emergencyFundMonths: number;
  debtPayoffMonths: number | null;
  alerts: string[];
}

export interface ScenarioComparison {
  left: ScenarioResult;
  right: ScenarioResult;
  preferredScenarioId: string;
  summary: string;
}

export interface SuggestedQuestion {
  id: string;
  title: string;
  prompt: string;
  category: string;
}

export interface AssistantResponse {
  answer: string;
  relatedScenarioId: string | null;
  suggestedActions: string[];
}

export interface ProductBootstrap {
  identity: UserIdentity;
  profile: FinancialProfile;
  dashboard: DashboardSnapshot;
  scenarios: Scenario[];
  recentScenarioResults: ScenarioResult[];
  suggestedQuestions: SuggestedQuestion[];
  designTokens: {
    spacing: Record<string, number>;
    radius: Record<string, number>;
    typography: Record<string, string>;
    colors: {
      brand: string;
      accent: string;
      positive: string;
      warning: string;
      canvasLight: string;
      canvasDark: string;
      surfaceLight: string;
      surfaceDark: string;
    };
    card: Record<string, string>;
  };
  disclaimer: string;
}

const api = sharedModule.FutureMeWebApi.getInstance();
const parse = <T,>(json: string): T => JSON.parse(json) as T;

export const bootstrapProduct = (): ProductBootstrap =>
  parse<ProductBootstrap>(api.bootstrapJson());

export const simulateScenario = (scenarioId: string): ScenarioResult =>
  parse<ScenarioResult>(api.simulateJson(scenarioId));

export const compareScenarios = (
  leftScenarioId: string,
  rightScenarioId: string,
): ScenarioComparison =>
  parse<ScenarioComparison>(
    api.compareJson(leftScenarioId, rightScenarioId),
  );

export const askFutureMe = (
  question: string,
  latestScenarioId?: string,
): AssistantResponse =>
  parse<AssistantResponse>(
    api.askJson(question, latestScenarioId ?? null),
  );
