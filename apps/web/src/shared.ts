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

export type MissionType =
  | "BUY_HOME"
  | "HAVE_CHILD"
  | "RELOCATE"
  | "RETIRE_EARLY"
  | "BECOME_DEBT_FREE"
  | "BUILD_EMERGENCY_FUND"
  | "SUPPORT_PARENTS"
  | "START_BUSINESS";

export type MissionStatus =
  | "NOT_STARTED"
  | "IN_PROGRESS"
  | "AT_RISK"
  | "ON_TRACK"
  | "COMPLETED";

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

export type OpportunitySource =
  | "MONEY_LEAK"
  | "DEBT"
  | "INVESTMENT"
  | "GOAL"
  | "READINESS"
  | "FINANCIAL_GPS"
  | "LIFE_EVENT";

export interface OpportunityRecommendation {
  id: string;
  title: string;
  description: string;
  source: OpportunitySource;
  impactScore: number;
  effortScore: number;
  confidenceScore: number;
  annualBenefitEstimate: number;
  fiveYearBenefitEstimate: number;
  priorityRanking: number;
  monthlyCommitment: number;
  relatedScenarioId: string | null;
}

export interface NextBestAction {
  recommendationId: string;
  title: string;
  description: string;
  callout: string;
  monthlyCommitment: number;
  fiveYearImpact: number;
  impactScore: number;
  confidenceScore: number;
}

export type ImpactSentiment = "POSITIVE" | "NEUTRAL" | "NEGATIVE";
export type ImpactDimension =
  | "CASH_FLOW"
  | "DEBT"
  | "EMERGENCY_FUND"
  | "RETIREMENT"
  | "READINESS"
  | "RISK";

export interface ExplainabilityFactor {
  id: string;
  title: string;
  description: string;
  pointImpact: number;
  sentiment: ImpactSentiment;
}

export interface FinancialExplainability {
  previousScore: number;
  currentScore: number;
  netChange: number;
  factors: ExplainabilityFactor[];
  summary: string;
}

export interface ScenarioImpactHeatmap {
  scenarioId: string;
  title: string;
  cells: {
    dimension: ImpactDimension;
    sentiment: ImpactSentiment;
    score: number;
    label: string;
  }[];
}

export interface MonthlyFinancialReview {
  id: string;
  month: string;
  label: string;
  generatedDate: string;
  wins: string[];
  risks: string[];
  opportunities: string[];
  recommendedActions: string[];
  readinessChanges: string[];
  goalProgress: string[];
  aiSummary: string;
}

export interface DecisionJournalEntry {
  id: string;
  type: string;
  title: string;
  decisionDate: string;
  expectedMonthlyImpact: number;
  actualMonthlyImpact: number | null;
  expectedFiveYearImpact: number;
  actualFiveYearImpact: number | null;
  status: "PLANNED" | "TRACKING" | "AHEAD" | "ON_TRACK" | "BEHIND";
  notes: string;
  relatedScenarioId: string | null;
}

export interface FutureOutcomeContribution {
  id: string;
  title: string;
  description: string;
  fiveYearContribution: number;
  sharePercentage: number;
  source: OpportunitySource;
}

export interface BankingVisionDemo {
  title: string;
  subtitle: string;
  audiences: string[];
  steps: {
    order: number;
    title: string;
    description: string;
    focusTarget: string;
  }[];
}

export interface AnalyticsEvent {
  id: string;
  type:
    | "SCENARIO_CREATED"
    | "GOAL_ADDED"
    | "INSIGHT_VIEWED"
    | "RECOMMENDATION_ACCEPTED"
    | "READINESS_VIEWED"
    | "AI_QUESTION_ASKED"
    | "MONTHLY_REVIEW_OPENED"
    | "MISSION_CREATED"
    | "MISSION_COMPLETED"
    | "MISSION_READINESS_IMPROVED"
    | "MISSION_TIMELINE_IMPROVED"
    | "MISSION_ACTION_COMPLETED"
    | "MISSION_GOAL_ACHIEVED";
  occurredAt: string;
  properties: { name: string; value: string }[];
}

export interface MissionNextAction {
  id: string;
  title: string;
  description: string;
  estimatedReadinessIncrease: number;
  estimatedTimelineReductionMonths: number;
  annualBenefitEstimate: number;
  fiveYearBenefitEstimate: number;
  impactScore: number;
  confidenceScore: number;
  relatedScenarioId: string | null;
}

export interface MissionTimelinePoint {
  horizon: "TODAY" | "THIRTY_DAYS" | "NINETY_DAYS" | "ONE_YEAR" | "THREE_YEARS";
  label: string;
  monthsFromNow: number;
  readinessScore: number;
  progressPercentage: number;
  completedActions: number;
  milestone: string;
  projectedCompletionDate: string;
}

export interface Mission {
  missionId: string;
  missionType: MissionType;
  title: string;
  description: string;
  targetDate: string;
  readinessScore: number;
  progressPercentage: number;
  riskLevel: "LOW" | "MODERATE" | "ELEVATED" | "HIGH";
  estimatedCost: number;
  projectedBenefit: number;
  blockers: string[];
  recommendations: string[];
  nextAction: MissionNextAction;
  timeline: MissionTimelinePoint[];
  createdDate: string;
  updatedDate: string;
  status: MissionStatus;
  readinessFactors: {
    category: "FINANCIAL" | "CASH_FLOW" | "RISK" | "EMERGENCY_FUND" | "DEBT" | "GOAL";
    title: string;
    score: number;
    explanation: string;
  }[];
  strengths: string[];
  weaknesses: string[];
  confidenceLevel: "HIGH" | "MEDIUM" | "LOW";
  goalProbabilityPercentage: number;
}

export interface MissionControlSnapshot {
  activeMissions: Mission[];
  highestReadinessMission: Mission;
  lowestReadinessMission: Mission;
  missionProgressPercentage: number;
  missionTimeline: MissionTimelinePoint[];
  nextBestAction: MissionNextAction;
  risks: {
    id: string;
    missionId: string;
    title: string;
    description: string;
    impactLabel: string;
  }[];
  opportunities: {
    id: string;
    missionId: string;
    title: string;
    description: string;
    impactLabel: string;
  }[];
}

export interface MissionAnalyticsSnapshot {
  missionsCreated: number;
  missionsCompleted: number;
  readinessImprovements: number;
  timelineImprovements: number;
  actionsCompleted: number;
  goalsAchieved: number;
  trends: {
    missionId: string;
    title: string;
    startingReadinessScore: number;
    currentReadinessScore: number;
    readinessChange: number;
    timelineReductionMonths: number;
    actionsCompleted: number;
  }[];
}

export type MissionActionStatus =
  | "LOCKED"
  | "AVAILABLE"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "MISSED";

export interface MissionAction {
  actionId: string;
  missionId: string;
  title: string;
  description: string;
  category: "DEBT" | "SAVINGS" | "CASH_FLOW" | "EMERGENCY_FUND" | "INVESTING" | "RISK" | "PLANNING";
  effort: "LOW" | "MEDIUM" | "HIGH";
  impact: "LOW" | "MEDIUM" | "HIGH";
  readinessGain: number;
  targetDate: string;
  completionStatus: MissionActionStatus;
  dependencyActionIds: string[];
  blockerMessage: string | null;
  metricLabel: string;
  currentMetricValue: number;
  targetMetricValue: number;
  metricProgressPercentage: number;
}

export interface MissionExecutionPlan {
  missionId: string;
  actionPlan: {
    missionId: string;
    actions: MissionAction[];
    nextAction: MissionAction | null;
    blockedActions: MissionAction[];
    unlockedActions: MissionAction[];
  };
  progress: {
    missionId: string;
    progressPercentage: number;
    completedActions: number;
    totalActions: number;
    actionProgressPercentage: number;
    metricProgressPercentage: number;
    readinessContributionPercentage: number;
    summary: string;
  };
  roadmap: {
    missionId: string;
    stages: {
      horizon: "THIRTY_DAYS" | "NINETY_DAYS" | "ONE_YEAR";
      label: string;
      currentStatus: "GREEN" | "YELLOW" | "RED";
      upcomingActions: MissionAction[];
      completedActions: MissionAction[];
      expectedReadinessGrowth: number;
      projectedCompletionDate: string;
    }[];
  };
  health: {
    missionId: string;
    status: "GREEN" | "YELLOW" | "RED";
    score: number;
    factors: {
      id: string;
      title: string;
      triggered: boolean;
      explanation: string;
      penaltyPoints: number;
    }[];
    summary: string;
  };
  notifications: MissionNotification[];
  history: {
    missionId: string;
    events: {
      eventId: string;
      missionId: string;
      type: "READINESS_CHANGED" | "ACTION_COMPLETED" | "TIMELINE_CHANGED" | "RISK_CHANGED" | "HEALTH_CHANGED";
      occurredAt: string;
      title: string;
      detail: string;
    }[];
    points: {
      date: string;
      readinessScore: number;
      progressPercentage: number;
      riskScore: number;
      healthStatus: "GREEN" | "YELLOW" | "RED";
    }[];
  };
  scenarioImpacts: {
    scenarioId: string;
    title: string;
    readinessImpact: number;
    timelineImpactMonths: number;
    riskImpact: number;
    summary: string;
  }[];
}

export interface MissionNotification {
  notificationId: string;
  missionId: string;
  type: "ACTION_UNLOCKED" | "MISSION_AT_RISK" | "READINESS_IMPROVED" | "MILESTONE_COMPLETED" | "TIMELINE_ACCELERATED" | "MISSION_COMPLETED";
  title: string;
  message: string;
  createdAt: string;
  isRead: boolean;
}

export interface MissionExecutionCenter {
  plans: MissionExecutionPlan[];
  notifications: MissionNotification[];
  atRiskMissionCount: number;
  actionsDueCount: number;
  recentlyCompletedCount: number;
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
  opportunities: OpportunityRecommendation[];
  nextBestAction: NextBestAction;
  financialExplainability: FinancialExplainability;
  scenarioImpactHeatmaps: ScenarioImpactHeatmap[];
  monthlyReviews: MonthlyFinancialReview[];
  decisionJournal: DecisionJournalEntry[];
  futureOutcomeContributions: FutureOutcomeContribution[];
  bankingVisionDemo: BankingVisionDemo;
  analyticsEvents: AnalyticsEvent[];
  missions: Mission[];
  missionControl: MissionControlSnapshot;
  missionExecution: MissionExecutionCenter;
  missionAnalytics: MissionAnalyticsSnapshot;
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

export const recordAnalyticsEvent = (
  typeCode: string,
  subjectId = "",
): AnalyticsEvent =>
  parse<AnalyticsEvent>(api.recordAnalyticsEventJson(typeCode, subjectId));

export const saveDecision = (scenarioId: string): DecisionJournalEntry =>
  parse<DecisionJournalEntry>(api.saveDecisionJson(scenarioId));

export const completeMissionAction = (actionId: string): ProductBootstrap =>
  parse<ProductBootstrap>(api.completeMissionActionJson(actionId));
