import * as sharedModule from "futureme-shared";

export type ScenarioType =
  | "BUY_HOME"
  | "REFINANCE_MORTGAGE"
  | "PAY_OFF_DEBT"
  | "JOB_LOSS"
  | "SPOUSE_STOPS_WORKING"
  | "RELOCATE"
  | "HAVE_CHILD"
  | "START_BUSINESS"
  | "INCREASE_INVESTMENTS";

export type ReadinessCategory =
  | "HOME_PURCHASE"
  | "CHILD"
  | "RELOCATION"
  | "RETIREMENT"
  | "BUSINESS_STARTUP"
  | "PARENT_SUPPORT"
  | "EDUCATION_FUNDING";

export type ReadinessLevel =
  | "NOT_READY"
  | "NEEDS_PREPARATION"
  | "ALMOST_READY"
  | "READY";

export type ReadinessTrend = "IMPROVING" | "STABLE" | "DECLINING";

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

export type InsightSeverity = "INFO" | "OPPORTUNITY" | "WARNING" | "CRITICAL";

export interface Insight {
  id: string;
  title: string;
  summary: string;
  severity: InsightSeverity;
  category: string;
  estimatedDollarImpact: number;
  recommendedAction: string;
  relatedScenarioType: ScenarioType | null;
  createdDate: string;
}

export interface MoneyLeak {
  id: string;
  type: string;
  title: string;
  summary: string;
  estimatedMonthlyLoss: number;
  estimatedAnnualLoss: number;
  estimatedFiveYearLoss: number;
  fixRecommendation: string;
  difficulty: "EASY" | "MODERATE" | "INVOLVED";
}

export interface FinancialGpsResult {
  currentFiveYearNetWorth: number;
  improvedFiveYearNetWorth: number;
  difference: number;
  monthlyActionPlan: string[];
  confidenceLevel: "HIGH" | "MEDIUM" | "LOW";
  explanation: string;
  currentTrajectory: ProjectionPoint[];
  improvedTrajectory: ProjectionPoint[];
}

export interface GoalProbabilityResult {
  id: string;
  type: string;
  title: string;
  probabilityPercentage: number;
  blockers: string[];
  recommendedActions: string[];
  requiredMonthlyImprovement: number;
  projectedReadyDate: string;
  explanation: string;
}

export interface LifeEventPlan {
  id: string;
  type: string;
  title: string;
  subtitle: string;
  estimatedMonthlyImpact: number;
  oneTimeCostLow: number;
  oneTimeCostHigh: number;
  riskImpact: number;
  recommendedPreparationSteps: string[];
  relatedInsights: string[];
  suggestedScenarioIds: string[];
}

export interface LifeReadinessResult {
  id: string;
  category: ReadinessCategory;
  title: string;
  readinessScore: number;
  readinessLevel: ReadinessLevel;
  strengths: string[];
  weaknesses: string[];
  blockers: string[];
  confidenceLevel: "HIGH" | "MEDIUM" | "LOW";
  recommendedActions: string[];
  projectedReadyDate: string;
  estimatedMonthsToReady: number;
  trend: ReadinessTrend;
  trendDelta: number;
}

export interface ReadinessImprovementPlan {
  id: string;
  category: ReadinessCategory;
  title: string;
  currentScore: number;
  targetScore: number;
  scoreGap: number;
  recommendations: string[];
  monthlyCommitment: number;
  estimatedTimelineMonths: number;
  projectedTargetDate: string;
}

export interface LifeDecisionSimulation {
  scenarioId: string;
  title: string;
  category: ReadinessCategory;
  readinessScoreBefore: number;
  readinessScoreAfter: number;
  readinessImpact: number;
  monthlyCashFlowImpact: number;
  fiveYearNetWorthImpact: number;
  riskScoreBefore: number;
  riskScoreAfter: number;
  riskChange: number;
  timelineChangeMonths: number;
  summary: string;
  recommendedActions: string[];
}

export interface TimelineReadinessScore {
  category: ReadinessCategory;
  score: number;
}

export interface LifeTimelinePoint {
  horizon: "TODAY" | "SIX_MONTHS" | "ONE_YEAR" | "THREE_YEARS" | "FIVE_YEARS";
  label: string;
  monthsFromNow: number;
  netWorth: number;
  debtBalance: number;
  investmentBalance: number;
  readinessScores: TimelineReadinessScore[];
  completedGoals: string[];
}

export interface ExecutiveDemoStep {
  order: number;
  title: string;
  category: ReadinessCategory | null;
  description: string;
  coachPrompt: string;
}

export interface ExecutiveDemoExperience {
  personaTitle: string;
  personaSummary: string;
  personaFacts: string[];
  steps: ExecutiveDemoStep[];
}

export interface Transaction {
  id: string;
  postedDate: string;
  merchant: string;
  category: string;
  amount: number;
  isRecurring: boolean;
}

export interface ProductBootstrap {
  identity: UserIdentity;
  profile: FinancialProfile;
  dashboard: DashboardSnapshot;
  scenarios: Scenario[];
  recentScenarioResults: ScenarioResult[];
  transactions: Transaction[];
  debtAccounts: unknown[];
  investmentAccounts: unknown[];
  cashAccounts: unknown[];
  mortgageAccounts: unknown[];
  insights: Insight[];
  financialGps: FinancialGpsResult;
  goals: GoalProbabilityResult[];
  lifeEvents: LifeEventPlan[];
  moneyLeaks: MoneyLeak[];
  readiness: LifeReadinessResult[];
  readinessPlans: ReadinessImprovementPlan[];
  decisionSimulations: LifeDecisionSimulation[];
  lifeTimeline: LifeTimelinePoint[];
  executiveDemo: ExecutiveDemoExperience;
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
