import {
  AlertCircle,
  ArrowRight,
  Baby,
  BarChart3,
  Bell,
  BriefcaseBusiness,
  CheckCircle2,
  ChevronDown,
  CircleDollarSign,
  Clock3,
  CreditCard,
  Home,
  Landmark,
  LockKeyhole,
  Menu,
  MessageCircle,
  Moon,
  MoveRight,
  PiggyBank,
  RefreshCw,
  RotateCcw,
  Settings,
  ShieldCheck,
  Send,
  Sparkles,
  Sun,
  TrendingUp,
  X,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";

import { ComparisonChart } from "./components/ComparisonChart";
import { GpsTrajectoryChart } from "./components/GpsTrajectoryChart";
import { ProjectionChart } from "./components/ProjectionChart";
import {
  askFutureMe,
  bootstrapProduct,
  completeMissionAction,
  compareScenarios,
  recordAnalyticsEvent,
  saveDecision,
  simulateScenario,
  type DecisionJournalEntry,
  type MissionCoachBriefing,
  type MissionType,
  type ProductBootstrap,
  type ReadinessCategory,
  type Scenario,
  type ScenarioResult,
  type ScenarioType,
} from "./shared";

interface ChatMessage {
  id: number;
  text: string;
  isUser: boolean;
}

type CoachExplanationKey =
  | "whyNotReady"
  | "whatImprovedRecently"
  | "whatIsHurtingProgress"
  | "whatShouldIFocusOn"
  | "howCanIAccelerateTimeline"
  | "whatHappensIfIDoNothing";

const coachExplanationLabels: { key: CoachExplanationKey; label: string }[] = [
  { key: "whyNotReady", label: "Why not ready?" },
  { key: "whatImprovedRecently", label: "What improved?" },
  { key: "whatIsHurtingProgress", label: "What is hurting?" },
  { key: "whatShouldIFocusOn", label: "What should I focus on?" },
  { key: "howCanIAccelerateTimeline", label: "How do I move faster?" },
  { key: "whatHappensIfIDoNothing", label: "What if I do nothing?" },
];

const money = (value: number, compact = false) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
    notation: compact ? "compact" : "standard",
  }).format(value);

const signedMoney = (value: number) =>
  `${value >= 0 ? "+" : "-"}${money(Math.abs(value))}`;

const readinessLevel = (value: string) =>
  value.replaceAll("_", " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());

const displayDate = (value: string) =>
  new Intl.DateTimeFormat("en-US", { month: "short", year: "numeric" }).format(
    new Date(`${value}T00:00:00`),
  );

const scenarioIcon = (type: ScenarioType) => {
  const props = { size: 20, strokeWidth: 1.8 };
  switch (type) {
    case "BUY_HOME": return <Home {...props} />;
    case "REFINANCE_MORTGAGE": return <RefreshCw {...props} />;
    case "PAY_OFF_DEBT": return <CreditCard {...props} />;
    case "RELOCATE": return <MoveRight {...props} />;
    case "HAVE_CHILD": return <Baby {...props} />;
    case "INCREASE_INVESTMENTS": return <TrendingUp {...props} />;
    case "JOB_LOSS": return <BriefcaseBusiness {...props} />;
    case "SPOUSE_STOPS_WORKING": return <BriefcaseBusiness {...props} />;
    case "START_BUSINESS": return <BriefcaseBusiness {...props} />;
  }
};

const missionIcon = (type: MissionType) => {
  const props = { size: 20, strokeWidth: 1.9 };
  switch (type) {
    case "BUY_HOME": return <Home {...props} />;
    case "HAVE_CHILD": return <Baby {...props} />;
    case "RELOCATE": return <MoveRight {...props} />;
    case "RETIRE_EARLY": return <TrendingUp {...props} />;
    case "BECOME_DEBT_FREE": return <CreditCard {...props} />;
    case "BUILD_EMERGENCY_FUND": return <ShieldCheck {...props} />;
    case "SUPPORT_PARENTS": return <Landmark {...props} />;
    case "START_BUSINESS": return <BriefcaseBusiness {...props} />;
  }
};

function App() {
  const [loadState, setLoadState] = useState<
    | { status: "loading" }
    | { status: "error"; message: string }
    | { status: "content"; data: ProductBootstrap }
  >({ status: "loading" });
  const [selectedMissionId, setSelectedMissionId] = useState("mission-home");
  const [selectedId, setSelectedId] = useState("move-to-texas");
  const [leftId, setLeftId] = useState("move-to-texas");
  const [rightId, setRightId] = useState("stay-in-new-jersey");
  const [mobileMenu, setMobileMenu] = useState(false);
  const [assistantOpen, setAssistantOpen] = useState(false);
  const [selectedLifeEventId, setSelectedLifeEventId] = useState("event-baby");
  const [selectedReadinessCategory, setSelectedReadinessCategory] =
    useState<ReadinessCategory>("HOME_PURCHASE");
  const [showAllInsights, setShowAllInsights] = useState(false);
  const [showAllScenarios, setShowAllScenarios] = useState(false);
  const [showFinancialDetails, setShowFinancialDetails] = useState(false);
  const [acceptedAction, setAcceptedAction] = useState(false);
  const [acceptedMissionAction, setAcceptedMissionAction] = useState(false);
  const [coachExplanation, setCoachExplanation] =
    useState<CoachExplanationKey>("whatShouldIFocusOn");
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [readNotificationIds, setReadNotificationIds] = useState<string[]>([]);
  const [reviewOpen, setReviewOpen] = useState(true);
  const [decisionJournal, setDecisionJournal] = useState<DecisionJournalEntry[]>([]);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 0,
      text: "Tell me which mission matters now. I will explain the blocker, timeline, and highest-impact action.",
      isUser: false,
    },
  ]);
  const [darkMode, setDarkMode] = useState(() => {
    const stored = window.localStorage.getItem("futureme-theme");
    return stored
      ? stored === "dark"
      : window.matchMedia("(prefers-color-scheme: dark)").matches;
  });

  const loadDashboard = useCallback(async () => {
    setLoadState({ status: "loading" });
    try {
      const data = bootstrapProduct();
      setDecisionJournal(data.decisionJournal);
      setLoadState({ status: "content", data });
    } catch {
      setLoadState({
        status: "error",
        message: "FutureMe could not load the shared financial workspace. Rebuild the Kotlin/JS package and try again.",
      });
    }
  }, []);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  useEffect(() => {
    document.documentElement.dataset.theme = darkMode ? "dark" : "light";
    window.localStorage.setItem("futureme-theme", darkMode ? "dark" : "light");
  }, [darkMode]);

  useEffect(() => {
    if (loadState.status !== "content") return;
    const colors = loadState.data.designTokens.colors;
    const root = document.documentElement.style;
    root.setProperty("--forest", colors.brand);
    root.setProperty("--mint", colors.accent);
    root.setProperty("--green", colors.positive);
  }, [loadState]);

  if (loadState.status === "loading") {
    return <LoadingState />;
  }
  if (loadState.status === "error") {
    return <ErrorState message={loadState.message} onRetry={loadDashboard} />;
  }

  const { data } = loadState;
  const selectedMission =
    data.missions.find((mission) => mission.missionId === selectedMissionId) ??
    data.missionControl.activeMissions[0];
  const selectedExecution =
    data.missionExecution.plans.find((plan) => plan.missionId === selectedMission.missionId) ??
    data.missionExecution.plans[0];
  const selectedBriefing =
    data.missionCoachBriefings.find((briefing) => briefing.missionId === selectedMission.missionId) ??
    data.missionCoachBriefings[0];
  const activeMissionAction = selectedExecution.actionPlan.nextAction;
  const missionNextAction = activeMissionAction ?? selectedExecution.actionPlan.actions[0];
  const missionHealth = (missionId: string) =>
    data.missionExecution.plans.find((plan) => plan.missionId === missionId)?.health.status ??
    "YELLOW";
  const selectable = data.scenarios;
  if (selectable.length < 2) {
    return <EmptyState onRetry={loadDashboard} />;
  }
  const selected = data.scenarios.find((item) => item.id === selectedId) ?? selectable[0];
  const result = simulateScenario(selected.id);
  const left = data.scenarios.find((item) => item.id === leftId) ?? selectable[0];
  const right = data.scenarios.find((item) => item.id === rightId) ?? selectable[1];
  const comparison = compareScenarios(left.id, right.id);
  const leftResult = comparison.left;
  const rightResult = comparison.right;
  const preferred = comparison.preferredScenarioId === left.id ? leftResult : rightResult;
  const decisionSimulation =
    data.decisionSimulations.find((item) => item.scenarioId === selected.id) ??
    data.decisionSimulations[0];
  const scenarioHeatmap =
    data.scenarioImpactHeatmaps.find((item) => item.scenarioId === selected.id) ??
    data.scenarioImpactHeatmaps[0];
  const currentReview = data.monthlyReviews[0];
  const selectedReadiness =
    data.readiness.find((item) => item.category === selectedReadinessCategory) ??
    data.readiness[0];
  const selectedPlan =
    data.readinessPlans.find((item) => item.category === selectedReadiness.category) ??
    data.readinessPlans[0];
  const dashboardReadinessCategories: ReadinessCategory[] = [
    "HOME_PURCHASE",
    "CHILD",
    "RETIREMENT",
    "RELOCATION",
    "PARENT_SUPPORT",
  ];

  const submitQuestion = (prompt: string) => {
    const normalized = prompt.trim();
    if (!normalized) return;
    const shouldScopeToMission = [
      "what should i do next",
      "biggest blocker",
      "why is my readiness low",
      "ready faster",
      "speed up this mission",
      "why did readiness change",
      "still on track",
    ].some((phrase) => normalized.toLowerCase().includes(phrase));
    const coachPrompt = shouldScopeToMission
      ? `${normalized} Focus on my ${selectedMission.title} mission.`
      : normalized;
    const response = askFutureMe(coachPrompt, selected.id);
    setMessages((current) => [
      ...current,
      { id: Date.now(), text: normalized, isUser: true },
      { id: Date.now() + 1, text: response.answer, isUser: false },
    ]);
    setQuestion("");
    setAssistantOpen(true);
  };

  const chooseScenario = (scenarioId: string) => {
    setSelectedId(scenarioId);
    recordAnalyticsEvent("scenario_created", scenarioId);
  };

  const acceptHighestAction = () => {
    recordAnalyticsEvent("recommendation_accepted", data.nextBestAction.recommendationId);
    setAcceptedAction(true);
  };

  const focusMissionAction = () => {
    if (!activeMissionAction) return;
    const refreshed = completeMissionAction(activeMissionAction.actionId);
    setDecisionJournal(refreshed.decisionJournal);
    setLoadState({ status: "content", data: refreshed });
    setAcceptedMissionAction(false);
  };

  const saveSelectedDecision = () => {
    const entry = saveDecision(selected.id);
    setDecisionJournal((current) => [entry, ...current.filter((item) => item.id !== entry.id)]);
  };

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileMenu ? "open" : ""}`}>
        <div className="brand">
          <div className="brand-mark"><span>F</span><span>M</span></div>
          <div><strong>FutureMe</strong><small>Financial</small></div>
        </div>
        <button className="mobile-close" onClick={() => setMobileMenu(false)} aria-label="Close menu">
          <X size={20} />
        </button>
        <nav>
          <a className="active" href="#mission-control"><BarChart3 size={19} />Mission Control</a>
          <a href="#active-missions"><ShieldCheck size={19} />Active missions</a>
          <a href="#mission-timeline"><TrendingUp size={19} />Mission timeline</a>
          <a href="#mission-analytics"><BarChart3 size={19} />Mission analytics</a>
          <button
            className="nav-button"
            onClick={() => {
              setShowFinancialDetails((current) => !current);
              setMobileMenu(false);
            }}
            aria-expanded={showFinancialDetails}
          >
            <CircleDollarSign size={19} />Supporting services
          </button>
          <button className="nav-button" onClick={() => setAssistantOpen(true)}><MessageCircle size={19} />Mission Coach</button>
        </nav>
        <div className="sidebar-spacer" />
        <div className="privacy-card">
          <ShieldCheck size={22} />
          <div><strong>Private by design</strong><p>Demo data stays local. No bank credentials.</p></div>
        </div>
        <nav className="secondary-nav">
          <a href="#settings"><Settings size={18} />Settings</a>
        </nav>
        <div className="user-chip">
          <div className="avatar">JD</div>
          <div><strong>{data.identity.displayName}</strong><small>{data.identity.householdName}</small></div>
          <ChevronDown size={16} />
        </div>
      </aside>

      <main id="main-content">
        <header className="topbar">
          <button className="menu-button" onClick={() => setMobileMenu(true)} aria-label="Open menu">
            <Menu size={22} />
          </button>
          <div>
            <p className="eyebrow">Mission Control</p>
            <h1>Your path to the next major life decision</h1>
          </div>
          <div className="header-actions">
            <button
              className="icon-button"
              aria-label="Mission notifications"
              aria-expanded={notificationsOpen}
              onClick={() => setNotificationsOpen((current) => !current)}
            >
              <Bell size={19} />
              {data.missionExecution.notifications.length - readNotificationIds.length > 0 && (
                <span className="notification-count">
                  {data.missionExecution.notifications.length - readNotificationIds.length}
                </span>
              )}
            </button>
            <button
              className="icon-button"
              aria-label={darkMode ? "Use light theme" : "Use dark theme"}
              onClick={() => setDarkMode((current) => !current)}
            >
              {darkMode ? <Sun size={19} /> : <Moon size={19} />}
            </button>
            <button className="primary-button" aria-label="Open FutureMe Mission Coach" onClick={() => setAssistantOpen(true)}>
              <Sparkles size={17} />Ask Mission Coach
            </button>
          </div>
        </header>

        {notificationsOpen && (
          <section className="mission-notification-center" aria-label="Mission notification center">
            <div className="mission-section-heading">
              <div>
                <p className="eyebrow">Mission notifications</p>
                <h2>What changed</h2>
              </div>
              <span>{data.missionExecution.notifications.length} updates</span>
            </div>
            <div>
              {data.missionExecution.notifications.slice(0, 8).map((notification) => {
                const isRead = readNotificationIds.includes(notification.notificationId);
                return (
                  <button
                    className={isRead ? "read" : ""}
                    key={notification.notificationId}
                    onClick={() => {
                      setSelectedMissionId(notification.missionId);
                      setReadNotificationIds((current) => (
                        current.includes(notification.notificationId)
                          ? current
                          : [...current, notification.notificationId]
                      ));
                    }}
                  >
                    <Bell size={16} />
                    <span><strong>{notification.title}</strong><small>{notification.message}</small></span>
                    <b>{isRead ? "Read" : "New"}</b>
                  </button>
                );
              })}
            </div>
          </section>
        )}

        <div className="offline-banner" role="status">
          <ShieldCheck size={16} />
          <span>Mission engines active. Demo data stays local.</span>
          <strong>Mission first</strong>
        </div>

        <section className="mission-control-hero" id="mission-control" aria-labelledby="mission-control-title">
          <div className="mission-hero-copy">
            <p className="eyebrow light">Mission Control</p>
            <h2 id="mission-control-title">What are you preparing for?</h2>
            <p>See readiness, blockers, your next action, and the path forward.</p>
          </div>
          <div className="mission-hero-score">
            <span>Overall progress</span>
            <strong>{data.missionControl.missionProgressPercentage}%</strong>
            <div><i style={{ width: `${data.missionControl.missionProgressPercentage}%` }} /></div>
          </div>
          <div className="mission-hero-extremes">
            <div>
              <span>Most ready</span>
              <strong>{data.missionControl.highestReadinessMission.title}</strong>
              <b>{data.missionControl.highestReadinessMission.readinessScore}%</b>
            </div>
            <div>
              <span>Needs focus</span>
              <strong>{data.missionControl.lowestReadinessMission.title}</strong>
              <b>{data.missionControl.lowestReadinessMission.readinessScore}%</b>
            </div>
          </div>
        </section>

        <section className="mission-section" id="active-missions" aria-labelledby="active-missions-title">
          <div className="mission-section-heading">
            <div>
              <p className="eyebrow">Active missions</p>
              <h2 id="active-missions-title">Choose the decision that matters now</h2>
            </div>
            <span>{data.missionControl.activeMissions.length} active</span>
          </div>
          <div className="mission-card-grid">
            {data.missionControl.activeMissions.map((mission) => (
              <button
                className={`mission-card ${mission.missionId === selectedMission.missionId ? "active" : ""}`}
                key={mission.missionId}
                onClick={() => {
                  setSelectedMissionId(mission.missionId);
                  setAcceptedMissionAction(false);
                  setCoachExplanation("whatShouldIFocusOn");
                  recordAnalyticsEvent("readiness_viewed", mission.missionId);
                }}
                aria-pressed={mission.missionId === selectedMission.missionId}
              >
                <span className="mission-card-icon">{missionIcon(mission.missionType)}</span>
                <span className="mission-card-copy">
                  <strong>{mission.title}</strong>
                  <small>
                    <i className={`mission-health-dot ${missionHealth(mission.missionId).toLowerCase()}`} />
                    {readinessLevel(missionHealth(mission.missionId))} health
                  </small>
                </span>
                <b>{mission.readinessScore}</b>
                <span className="mission-card-progress"><i style={{ width: `${mission.progressPercentage}%` }} /></span>
              </button>
            ))}
          </div>
        </section>

        <section className="mission-detail-grid" aria-label={`${selectedMission.title} mission detail`}>
          <article className="mission-readiness-panel">
            <div className="mission-detail-title">
              <div className="mission-score-ring">
                <strong>{selectedMission.readinessScore}</strong>
                <span>ready</span>
              </div>
              <div>
                <p className="eyebrow">
                  Mission readiness · {readinessLevel(selectedExecution.health.status)} health
                </p>
                <h2>{selectedMission.title}</h2>
                <p>{selectedExecution.health.summary}</p>
              </div>
            </div>
            <div className="mission-factor-grid">
              {selectedMission.readinessFactors.map((factor) => (
                <div key={factor.category}>
                  <span>{factor.title.replace(" readiness", "")}</span>
                  <strong>{factor.score}%</strong>
                  <i><b style={{ width: `${factor.score}%` }} /></i>
                </div>
              ))}
            </div>
            <div className="mission-blocker">
              <AlertCircle size={18} />
              <div><span>Biggest blocker</span><strong>{selectedMission.blockers[0]}</strong></div>
            </div>
          </article>

          <article className="mission-action-panel">
            <p className="eyebrow light">Next best action</p>
            <h2>{missionNextAction.title}</h2>
            <p>{missionNextAction.description}</p>
            <div className="mission-action-impact">
              <div><span>Readiness</span><strong>+{missionNextAction.readinessGain}</strong></div>
              <div><span>Timeline</span><strong>-{selectedMission.nextAction.estimatedTimelineReductionMonths} mo</strong></div>
              <div><span>Target</span><strong>{displayDate(missionNextAction.targetDate)}</strong></div>
            </div>
            <button
              onClick={focusMissionAction}
              disabled={acceptedMissionAction || !activeMissionAction}
            >
              {activeMissionAction ? "Mark action complete" : "Mission complete"} <ArrowRight size={18} />
            </button>
          </article>
        </section>

        <MissionCoachBriefingCard
          briefing={selectedBriefing}
          explanationKey={coachExplanation}
          onSelectExplanation={setCoachExplanation}
          onAsk={submitQuestion}
        />

        <section className="mission-action-plan-section" aria-labelledby="mission-actions-title">
          <div className="mission-section-heading">
            <div>
              <p className="eyebrow">Mission action engine</p>
              <h2 id="mission-actions-title">Your dynamic action plan</h2>
            </div>
            <span>
              {selectedExecution.progress.completedActions}/{selectedExecution.progress.totalActions} complete
            </span>
          </div>
          <div className="mission-action-list">
            {selectedExecution.actionPlan.actions.map((action, index) => (
              <article className={action.completionStatus.toLowerCase()} key={action.actionId}>
                <span className="mission-action-order">
                  {action.completionStatus === "COMPLETED" ? (
                    <CheckCircle2 size={18} />
                  ) : action.completionStatus === "LOCKED" ? (
                    <LockKeyhole size={17} />
                  ) : action.completionStatus === "IN_PROGRESS" ? (
                    <Clock3 size={17} />
                  ) : index + 1}
                </span>
                <div>
                  <div className="mission-action-row">
                    <strong>{action.title}</strong>
                    <b>{readinessLevel(action.completionStatus)}</b>
                  </div>
                  <p>{action.blockerMessage ?? action.description}</p>
                  <div className="mission-action-meta">
                    <span>+{action.readinessGain} readiness</span>
                    <span>{readinessLevel(action.effort)} effort</span>
                    <span>Target {displayDate(action.targetDate)}</span>
                  </div>
                  <i><b style={{ width: `${action.metricProgressPercentage}%` }} /></i>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="mission-timeline-section" id="mission-timeline" aria-labelledby="mission-timeline-title">
          <div className="mission-section-heading">
            <div><p className="eyebrow">Mission roadmaps</p><h2 id="mission-timeline-title">{selectedMission.title} path forward</h2></div>
            <span>Target {displayDate(selectedMission.targetDate)}</span>
          </div>
          <div className="mission-timeline-track">
            {selectedExecution.roadmap.stages.map((stage) => (
              <article key={stage.horizon}>
                <span>{stage.label}</span>
                <strong>+{stage.expectedReadinessGrowth}</strong>
                <p>
                  {stage.upcomingActions.map((action) => action.title).join(" · ") ||
                    "Maintain completed actions."}
                </p>
                <small>
                  {stage.completedActions.length} complete · projected {displayDate(stage.projectedCompletionDate)}
                </small>
              </article>
            ))}
          </div>
        </section>

        <section className="mission-execution-grid">
          <article className="mission-health-panel">
            <p className="eyebrow">Mission health</p>
            <div className={`mission-health-score ${selectedExecution.health.status.toLowerCase()}`}>
              <strong>{selectedExecution.health.status}</strong>
              <span>{selectedExecution.health.score}/100</span>
            </div>
            {selectedExecution.health.factors.map((factor) => (
              <div className={factor.triggered ? "triggered" : ""} key={factor.id}>
                <span>{factor.title}</span>
                <b>{factor.triggered ? factor.explanation : "Clear"}</b>
              </div>
            ))}
          </article>

          <article className="mission-history-panel">
            <p className="eyebrow">Mission history</p>
            <h3>Readiness over time</h3>
            <div className="mission-history-graph" aria-label="Mission readiness history graph">
              {selectedExecution.history.points.map((point) => (
                <div key={point.date}>
                  <b style={{ height: `${point.readinessScore}%` }} />
                  <strong>{point.readinessScore}</strong>
                  <span>{new Date(`${point.date}T00:00:00`).toLocaleDateString("en-US", { month: "short" })}</span>
                </div>
              ))}
            </div>
            <p>{selectedExecution.history.events[0]?.detail}</p>
          </article>

          <article className="mission-scenario-panel">
            <p className="eyebrow">Mission scenarios</p>
            <h3>Evaluate the decision</h3>
            {selectedExecution.scenarioImpacts.map((scenario) => (
              <button
                key={scenario.scenarioId}
                onClick={() => {
                  if (data.scenarios.some((item) => item.id === scenario.scenarioId)) {
                    chooseScenario(scenario.scenarioId);
                  }
                  setShowFinancialDetails(true);
                }}
              >
                <span><strong>{scenario.title}</strong><small>{scenario.summary}</small></span>
                <b>
                  {scenario.readinessImpact >= 0 ? "+" : ""}{scenario.readinessImpact} ready ·{" "}
                  {scenario.timelineImpactMonths >= 0 ? "+" : ""}{scenario.timelineImpactMonths} mo
                </b>
              </button>
            ))}
          </article>
        </section>

        <section className="mission-intelligence-grid" id="mission-analytics">
          <article>
            <p className="eyebrow">Mission risks</p>
            {data.missionControl.risks.map((risk) => (
              <button key={risk.id} onClick={() => setSelectedMissionId(risk.missionId)}>
                <AlertCircle size={17} />
                <span><strong>{risk.title}</strong><small>{risk.description}</small></span>
                <b>{risk.impactLabel}</b>
              </button>
            ))}
          </article>
          <article>
            <p className="eyebrow">Mission opportunities</p>
            {data.missionControl.opportunities.map((opportunity) => (
              <button key={opportunity.id} onClick={() => setSelectedMissionId(opportunity.missionId)}>
                <TrendingUp size={17} />
                <span><strong>{opportunity.title}</strong><small>{opportunity.description}</small></span>
                <b>{opportunity.impactLabel}</b>
              </button>
            ))}
          </article>
          <article className="mission-analytics-panel">
            <p className="eyebrow">Mission analytics</p>
            <div className="mission-analytics-stats">
              <div><strong>{data.missionAnalytics.readinessImprovements}</strong><span>readiness gains</span></div>
              <div><strong>{data.missionAnalytics.actionsCompleted}</strong><span>actions done</span></div>
              <div><strong>{data.missionAnalytics.timelineImprovements}</strong><span>months saved</span></div>
            </div>
            {data.missionAnalytics.trends.slice(0, 3).map((trend) => (
              <div className="mission-trend" key={trend.missionId}>
                <span>{trend.title}</span><b>+{trend.readinessChange} pts</b>
              </div>
            ))}
          </article>
        </section>

        <section className="supporting-services-toggle" aria-labelledby="supporting-services-title">
          <div>
            <p className="eyebrow">Supporting services</p>
            <h2 id="supporting-services-title">Open the financial intelligence behind each mission</h2>
          </div>
          <button
            className="secondary-button"
            onClick={() => setShowFinancialDetails((current) => !current)}
            aria-expanded={showFinancialDetails}
          >
            {showFinancialDetails ? "Hide supporting services" : "View supporting services"}
            <ChevronDown className={showFinancialDetails ? "rotated" : ""} size={18} />
          </button>
        </section>

        {showFinancialDetails && (
          <div className="supporting-services">
        <section className="next-action-card" id="banking-intelligence" aria-labelledby="next-action-title">
          <div>
            <p className="eyebrow light">My highest impact action</p>
            <h2 id="next-action-title">{data.nextBestAction.title}</h2>
            <p>{data.nextBestAction.callout}</p>
          </div>
          <div className="next-action-stats">
            <span><b>{data.nextBestAction.impactScore}</b> impact</span>
            <span><b>{data.nextBestAction.confidenceScore}%</b> confidence</span>
            <span><b>{money(data.nextBestAction.fiveYearImpact, true)}</b> at 5 years</span>
          </div>
          <button className="next-action-button" onClick={acceptHighestAction} disabled={acceptedAction}>
            {acceptedAction ? <><ShieldCheck size={18} />Added to my plan</> : <>Make this my focus <ArrowRight size={18} /></>}
          </button>
        </section>

        <section className="banking-grid" aria-label="Banking intelligence">
          <article className="opportunity-panel">
            <div className="section-title compact">
              <div><p className="eyebrow">Ranked opportunities</p><h2>What matters next</h2></div>
            </div>
            {data.opportunities.slice(0, 4).map((opportunity) => (
              <div className="opportunity-row" key={opportunity.id}>
                <span>{opportunity.priorityRanking}</span>
                <div>
                  <strong>{opportunity.title}</strong>
                  <small>{money(opportunity.fiveYearBenefitEstimate)} potential over 5 years</small>
                </div>
                <b>{opportunity.impactScore}</b>
              </div>
            ))}
          </article>

          <article className="explainability-panel">
            <p className="eyebrow">Why my score changed</p>
            <div className="score-change">
              <span>{data.financialExplainability.previousScore}</span>
              <ArrowRight size={20} />
              <strong>{data.financialExplainability.currentScore}</strong>
              <b className={data.financialExplainability.netChange >= 0 ? "positive" : "negative"}>
                {data.financialExplainability.netChange >= 0 ? "+" : ""}{data.financialExplainability.netChange}
              </b>
            </div>
            <p>{data.financialExplainability.summary}</p>
            <div className="factor-list">
              {data.financialExplainability.factors.slice(0, 4).map((factor) => (
                <div key={factor.id}>
                  <span>{factor.title}</span>
                  <b className={factor.sentiment.toLowerCase()}>
                    {factor.pointImpact > 0 ? "+" : ""}{factor.pointImpact}
                  </b>
                </div>
              ))}
            </div>
          </article>
        </section>

        <section className="readiness-section" id="readiness" aria-labelledby="readiness-title">
          <div className="readiness-heading">
            <div>
              <p className="eyebrow">Life readiness dashboard</p>
              <h2 id="readiness-title">Are you ready for what comes next?</h2>
              <p>See your score, biggest blocker, and next best move.</p>
            </div>
            <button
              className="primary-button"
              onClick={() => submitQuestion("What is my weakest readiness category?")}
            >
              <Sparkles size={16} />Find my priority
            </button>
          </div>
          <div className="readiness-grid">
            {data.readiness
              .filter((item) => dashboardReadinessCategories.includes(item.category))
              .map((item) => (
                <button
                  className={`readiness-card ${item.category === selectedReadiness.category ? "active" : ""}`}
                  key={item.id}
                  onClick={() => {
                    setSelectedReadinessCategory(item.category);
                    recordAnalyticsEvent("readiness_viewed", item.category);
                  }}
                  aria-pressed={item.category === selectedReadiness.category}
                >
                  <div className="readiness-card-top">
                    <span>{item.confidenceLevel} confidence</span>
                    <b className={item.trend.toLowerCase()}>
                      {item.trend === "IMPROVING" ? "+" : ""}{item.trendDelta} pts / 6 mo
                    </b>
                  </div>
                  <div className="readiness-score-row">
                    <strong>{item.readinessScore}</strong>
                    <div>
                      <h3>{item.title}</h3>
                      <span>{readinessLevel(item.readinessLevel)}</span>
                    </div>
                  </div>
                  <div
                    className="readiness-progress"
                    role="progressbar"
                    aria-label={`${item.title} score`}
                    aria-valuenow={item.readinessScore}
                    aria-valuemin={0}
                    aria-valuemax={100}
                  >
                    <i style={{ width: `${item.readinessScore}%` }} />
                  </div>
                  <small>
                    {item.estimatedMonthsToReady === 0
                      ? "Ready now"
                      : `Estimated ready ${displayDate(item.projectedReadyDate)}`}
                  </small>
                </button>
              ))}
          </div>
        </section>

        <section className="improvement-section" id="improvement-plan" aria-labelledby="plan-title">
          <div className="plan-summary">
            <p className="eyebrow">Readiness improvement plan</p>
            <h2 id="plan-title">{selectedReadiness.title}</h2>
            <div className="plan-focus">
              <span>Main gap</span>
              <strong>
                {selectedReadiness.weaknesses[0] ??
                  selectedReadiness.blockers[0] ??
                  "No active gap."}
              </strong>
            </div>
            <div className="plan-score-path">
              <div><span>Current</span><strong>{selectedPlan.currentScore}%</strong></div>
              <MoveRight aria-hidden="true" />
              <div><span>Target</span><strong>{selectedPlan.targetScore}%</strong></div>
            </div>
            <div className="plan-timeline">
              <strong>{selectedPlan.estimatedTimelineMonths} months</strong>
              <span>Modeled target {displayDate(selectedPlan.projectedTargetDate)}</span>
            </div>
          </div>
          <div className="plan-actions">
            <div className="plan-tabs" aria-label="Choose readiness plan">
              {data.readiness.map((item) => (
                <button
                  key={item.id}
                  className={item.category === selectedReadiness.category ? "active" : ""}
                  onClick={() => setSelectedReadinessCategory(item.category)}
                >
                  {item.title.replace(" Readiness", "")}
                </button>
              ))}
            </div>
            <p className="eyebrow">Recommended sequence</p>
            {selectedPlan.recommendations.slice(0, 3).map((action, index) => (
              <div className="plan-action" key={action}>
                <span>{index + 1}</span>
                <strong>{action}</strong>
              </div>
            ))}
            <div className="monthly-commitment">
              <span>Modeled monthly commitment</span>
              <strong>{money(selectedPlan.monthlyCommitment)}</strong>
            </div>
          </div>
        </section>

        <section className="timeline-section" id="timeline" aria-labelledby="timeline-title">
          <div className="section-title">
            <div>
              <p className="eyebrow">Life timeline</p>
              <h2 id="timeline-title">See readiness change before the decision arrives</h2>
            </div>
          </div>
          <div className="timeline-track">
            {data.lifeTimeline.map((point) => {
              const averageReadiness = Math.round(
                point.readinessScores.reduce((sum, item) => sum + item.score, 0) /
                  point.readinessScores.length,
              );
              return (
                <article className="timeline-point" key={point.horizon}>
                  <div className="timeline-marker"><i /></div>
                  <p className="eyebrow">{point.label}</p>
                  <h3>{money(point.netWorth, true)} net worth</h3>
                  <dl>
                    <div><dt>Avg readiness</dt><dd>{averageReadiness}%</dd></div>
                    <div><dt>Debt</dt><dd>{money(point.debtBalance, true)}</dd></div>
                    <div><dt>Investments</dt><dd>{money(point.investmentBalance, true)}</dd></div>
                  </dl>
                  {point.completedGoals.length > 0 && (
                    <small>{point.completedGoals.length} goals complete</small>
                  )}
                </article>
              );
            })}
          </div>
        </section>

        <section className="details-toggle" aria-labelledby="details-title">
          <div>
            <p className="eyebrow">Supporting financial details</p>
            <h2 id="details-title">Want the numbers behind the score?</h2>
          </div>
          <button
            className="secondary-button"
            onClick={() => setShowFinancialDetails((current) => !current)}
            aria-expanded={showFinancialDetails}
          >
            {showFinancialDetails ? "Hide details" : "View financial details"}
            <ChevronDown className={showFinancialDetails ? "rotated" : ""} size={18} />
          </button>
        </section>

        {showFinancialDetails && (
          <div className="financial-details" id="financial-details">
        <section className="hero-grid" id="financial-overview">
          <article className="health-card">
            <div className="card-heading">
              <div><p className="eyebrow light">Financial health</p><h2>{data.dashboard.healthScore.label}</h2></div>
              <span className="live-pill"><i />Updated today</span>
            </div>
            <div className="health-content">
              <div
                className="score-ring"
                role="img"
                aria-label={`Financial health score ${data.dashboard.healthScore.value} out of 100`}
                style={{ "--score": `${data.dashboard.healthScore.value * 3.6}deg` } as React.CSSProperties}
              >
                <div><strong>{data.dashboard.healthScore.value}</strong><span>out of 100</span></div>
              </div>
              <div className="health-copy">
                <p>{data.dashboard.healthScore.summary}</p>
                <div className="mini-stat-row">
                  <div><span>Monthly cushion</span><strong>{money(data.dashboard.monthlyCashFlow)}</strong></div>
                  <div><span>Cash runway</span><strong>{data.dashboard.emergencyFundMonths.toFixed(1)} mo</strong></div>
                </div>
              </div>
            </div>
          </article>

          <article className="projection-card">
            <div className="card-heading">
              <div><p className="eyebrow">Five-year outlook</p><h2>{money(result.projectedNetWorth5Years, true)}</h2></div>
              <span className={`delta-pill ${result.netWorthDelta5Years >= 0 ? "positive" : "negative"}`}>
                {signedMoney(result.netWorthDelta5Years)} vs plan
              </span>
            </div>
            <ProjectionChart points={result.projections} />
          </article>
        </section>

        <section className="metrics-grid" aria-label="Financial summary">
          <MetricCard icon={<CircleDollarSign />} label="Current net worth" value={money(data.dashboard.currentNetWorth)} hint="+8.4% over 12 months" tone="mint" />
          <MetricCard icon={<PiggyBank />} label="Emergency fund" value={`${data.dashboard.emergencyFundMonths.toFixed(1)} months`} hint={`${money(data.profile.liquidSavings)} liquid`} tone="blue" />
          <MetricCard icon={<Landmark />} label="Debt-free in" value={`${data.dashboard.debtPayoffMonths ?? "—"} months`} hint={`${money(data.profile.creditCardDebt)} remaining`} tone="coral" />
          <MetricCard icon={<TrendingUp />} label="Investing monthly" value={money(data.profile.monthlyRetirementContribution)} hint={`${money(data.profile.investmentBalance)} invested`} tone="violet" />
        </section>

        <section className="alerts-panel" aria-labelledby="alerts-title">
          <div>
            <p className="eyebrow">Priority signals</p>
            <h2 id="alerts-title">What deserves attention</h2>
          </div>
          <div className="alert-list">
            {data.dashboard.alerts.length > 0 ? data.dashboard.alerts.map((alert) => (
              <div key={alert}><AlertCircle size={15} aria-hidden="true" /><span>{alert}</span></div>
            )) : (
              <p>No urgent alerts. Your plan is tracking within the configured thresholds.</p>
            )}
          </div>
        </section>

        <section className="v2-section" aria-labelledby="checkup-title">
          <div className="section-title">
            <div>
              <p className="eyebrow">This week's financial checkup</p>
              <h2 id="checkup-title">
                {showAllInsights ? "Every proactive insight" : "Your top three proactive insights"}
              </h2>
            </div>
            <button
              className="text-button"
              onClick={() => setShowAllInsights((current) => !current)}
              aria-expanded={showAllInsights}
            >
              {showAllInsights ? "Show top three" : "View all insights"} <ArrowRight size={16} />
            </button>
          </div>
          <div className="insights-grid">
            {(showAllInsights ? data.insights : data.insights.slice(0, 3)).map((insight) => (
              <article className={`proactive-card ${insight.severity.toLowerCase()}`} key={insight.id}>
                <div className="insight-label">
                  <span>{insight.category.replaceAll("_", " ")}</span>
                  {insight.estimatedDollarImpact > 0 && <strong>{money(insight.estimatedDollarImpact)}</strong>}
                </div>
                <h3>{insight.title}</h3>
                <p>{insight.summary}</p>
                <b>{insight.recommendedAction}</b>
              </article>
            ))}
          </div>
        </section>

        <section className="v2-section" id="gps" aria-labelledby="gps-title">
          <article className="gps-panel">
            <div className="gps-copy">
              <p className="eyebrow light">Financial GPS</p>
              <h2 id="gps-title">A better route is available</h2>
              <p>{data.financialGps.explanation}</p>
              <div className="gps-values">
                <div><span>Current trajectory</span><strong>{money(data.financialGps.currentFiveYearNetWorth, true)}</strong></div>
                <MoveRight aria-hidden="true" />
                <div><span>Improved trajectory</span><strong>{money(data.financialGps.improvedFiveYearNetWorth, true)}</strong></div>
              </div>
              <div className="gps-lift">+{money(data.financialGps.difference)} five-year lift</div>
              <GpsTrajectoryChart
                current={data.financialGps.currentTrajectory}
                improved={data.financialGps.improvedTrajectory}
              />
            </div>
            <div className="gps-actions">
              <span className="confidence-pill">{data.financialGps.confidenceLevel} confidence</span>
              {data.financialGps.monthlyActionPlan.map((action) => (
                <div key={action}><ShieldCheck size={16} /><span>{action}</span></div>
              ))}
              <button
                className="primary-button"
                onClick={() => submitQuestion("How can I improve my 5-year outlook?")}
              >
                <Sparkles size={16} />Explain my improved route
              </button>
            </div>
          </article>
        </section>

        <section className="v2-section" id="money-leaks" aria-labelledby="leaks-title">
          <div className="section-title">
            <div><p className="eyebrow">Money leak detector</p><h2 id="leaks-title">Keep more of what you earn</h2></div>
            <strong className="annual-impact">
              {money(data.moneyLeaks.reduce((total, leak) => total + leak.estimatedAnnualLoss, 0))} annual opportunity
            </strong>
          </div>
          <div className="leak-grid">
            {data.moneyLeaks.map((leak) => (
              <article className="leak-card" key={leak.id}>
                <div><span>{leak.difficulty}</span><strong>{money(leak.estimatedMonthlyLoss)}/mo</strong></div>
                <h3>{leak.title}</h3>
                <p>{leak.summary}</p>
                <small>Five-year impact {money(leak.estimatedFiveYearLoss)}</small>
                <b>{leak.fixRecommendation}</b>
              </article>
            ))}
          </div>
        </section>

        <section className="v2-section" id="goals" aria-labelledby="goals-title">
          <div className="section-title">
            <div><p className="eyebrow">Goal readiness</p><h2 id="goals-title">How close your next chapter is</h2></div>
          </div>
          <div className="goal-grid">
            {data.goals.map((goal) => (
              <article className="goal-card" key={goal.id}>
                <div className="goal-score"><strong>{goal.probabilityPercentage}%</strong><span>ready</span></div>
                <h3>{goal.title}</h3>
                <div
                  className="goal-progress"
                  role="progressbar"
                  aria-label={`${goal.title} readiness`}
                  aria-valuenow={goal.probabilityPercentage}
                  aria-valuemin={0}
                  aria-valuemax={100}
                >
                  <i style={{ width: `${goal.probabilityPercentage}%` }} />
                </div>
                <p>{goal.explanation}</p>
                <details className="goal-details">
                  <summary>View blockers and action plan</summary>
                  <strong>Blockers</strong>
                  {goal.blockers.length > 0 ? (
                    <ul>{goal.blockers.map((blocker) => <li key={blocker}>{blocker}</li>)}</ul>
                  ) : (
                    <p>No blockers detected in the current model.</p>
                  )}
                  <strong>Recommended actions</strong>
                  <ul>{goal.recommendedActions.map((action) => <li key={action}>{action}</li>)}</ul>
                  <b>Required monthly improvement {money(goal.requiredMonthlyImprovement)}</b>
                </details>
                <small>Modeled ready {goal.projectedReadyDate}</small>
              </article>
            ))}
          </div>
        </section>

        <section className="v2-section" id="life-events" aria-labelledby="events-title">
          <div className="section-title">
            <div><p className="eyebrow">Life event planner</p><h2 id="events-title">Plan the moments that change everything</h2></div>
          </div>
          <div className="life-event-layout">
            <div className="event-list" role="list">
              {data.lifeEvents.map((event) => (
                <button
                  key={event.id}
                  className={event.id === selectedLifeEventId ? "active" : ""}
                  onClick={() => setSelectedLifeEventId(event.id)}
                  aria-pressed={event.id === selectedLifeEventId}
                >
                  <Baby size={18} />
                  <span><strong>{event.title}</strong><small>{event.subtitle}</small></span>
                  <ArrowRight size={16} />
                </button>
              ))}
            </div>
            {data.lifeEvents.filter((event) => event.id === selectedLifeEventId).map((event) => (
              <article className="event-detail" key={event.id}>
                <p className="eyebrow">{event.type.replaceAll("_", " ")}</p>
                <h2>{event.title}</h2>
                <p>{event.subtitle}</p>
                <div className="event-metrics">
                  <div><span>Monthly impact</span><strong>{money(event.estimatedMonthlyImpact)}</strong></div>
                  <div><span>One-time range</span><strong>{money(event.oneTimeCostLow)}–{money(event.oneTimeCostHigh)}</strong></div>
                  <div><span>Risk impact</span><strong>+{event.riskImpact}</strong></div>
                </div>
                <h3>Preparation plan</h3>
                {event.recommendedPreparationSteps.map((step) => (
                  <div className="event-step" key={step}><ShieldCheck size={16} /><span>{step}</span></div>
                ))}
                <button
                  className="primary-button"
                  onClick={() => {
                    const scenarioId = event.suggestedScenarioIds[0];
                    if (scenarioId) chooseScenario(scenarioId);
                    document.querySelector("#scenarios")?.scrollIntoView({ behavior: "smooth" });
                  }}
                >
                  Plan this event <ArrowRight size={16} />
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="quick-actions" aria-label="Quick actions">
          <button onClick={() => document.querySelector("#scenarios")?.scrollIntoView({ behavior: "smooth" })}>
            <Sparkles size={18} /><span><strong>Simulate decision</strong><small>Model a major choice</small></span>
          </button>
          <button onClick={() => setAssistantOpen(true)}>
            <MessageCircle size={18} /><span><strong>Ask FutureMe</strong><small>Get a grounded explanation</small></span>
          </button>
          <button onClick={() => submitQuestion("How can I improve my 5-year outlook?")}>
            <TrendingUp size={18} /><span><strong>Improve my outlook</strong><small>Follow the Financial GPS</small></span>
          </button>
        </section>
          </div>
        )}

        <section className="section-block" id="scenarios">
          <div className="section-title">
            <div>
              <p className="eyebrow">Life decision simulator</p>
              <h2>What happens to readiness if you make the move?</h2>
            </div>
            <button
              className="text-button"
              onClick={() => setShowAllScenarios((current) => !current)}
              aria-expanded={showAllScenarios}
            >
              {showAllScenarios ? "Show featured scenarios" : "View all scenarios"} <ArrowRight size={16} />
            </button>
          </div>
          <div className={`scenario-strip ${showAllScenarios ? "show-all" : ""}`}>
            {selectable.map((item) => (
              <ScenarioCard
                key={item.id}
                scenario={item}
                active={item.id === selected.id}
                onClick={() => chooseScenario(item.id)}
              />
            ))}
          </div>

          <article className="insight-panel">
            <div className="insight-main">
              <div className="ai-badge"><Sparkles size={18} />Readiness impact</div>
              <h2>{selected.title}</h2>
              <p>{decisionSimulation.summary}</p>
              <div className="tradeoffs">
                {decisionSimulation.recommendedActions.slice(0, 2).map((action) => (
                  <span key={action}><i />{action}</span>
                ))}
              </div>
            </div>
            <div className="impact-grid">
              <Impact label="Readiness after" value={`${decisionSimulation.readinessScoreAfter}%`} positive={decisionSimulation.readinessScoreAfter >= 60} />
              <Impact label="Readiness impact" value={`${decisionSimulation.readinessImpact >= 0 ? "+" : ""}${decisionSimulation.readinessImpact} pts`} positive={decisionSimulation.readinessImpact >= 0} />
              <Impact label="Monthly impact" value={signedMoney(decisionSimulation.monthlyCashFlowImpact)} positive={decisionSimulation.monthlyCashFlowImpact >= 0} />
              <Impact label="Five-year impact" value={signedMoney(decisionSimulation.fiveYearNetWorthImpact)} positive={decisionSimulation.fiveYearNetWorthImpact >= 0} />
              <Impact label="Risk change" value={`${decisionSimulation.riskChange >= 0 ? "+" : ""}${decisionSimulation.riskChange} pts`} positive={decisionSimulation.riskChange <= 0} />
              <Impact label="Timeline change" value={`${decisionSimulation.timelineChangeMonths >= 0 ? "+" : ""}${decisionSimulation.timelineChangeMonths} mo`} positive={decisionSimulation.timelineChangeMonths <= 0} />
            </div>
          </article>

          <article className="heatmap-panel" aria-labelledby="heatmap-title">
            <div className="section-title compact">
              <div>
                <p className="eyebrow">Scenario impact heatmap</p>
                <h2 id="heatmap-title">{scenarioHeatmap.title}</h2>
              </div>
              <button className="secondary-button" onClick={saveSelectedDecision}>
                Save decision
              </button>
            </div>
            <div className="impact-heatmap">
              {scenarioHeatmap.cells.map((cell) => (
                <div className={cell.sentiment.toLowerCase()} key={cell.dimension}>
                  <span>{readinessLevel(cell.dimension)}</span>
                  <strong>{cell.label}</strong>
                </div>
              ))}
            </div>
          </article>

          <article className="risk-panel" aria-labelledby="risk-title">
            <div className="risk-summary">
              <div className={`risk-score-badge ${result.riskScore.level.toLowerCase()}`}>
                <strong>{result.riskScore.value}</strong>
                <span>/100</span>
              </div>
              <div>
                <p className="eyebrow">Transparent risk model</p>
                <h2 id="risk-title">{result.riskScore.level.toLowerCase()} risk</h2>
                <p>{result.riskScore.summary}</p>
              </div>
            </div>
            <div className="risk-factor-list">
              {result.riskScore.factors.map((factor) => (
                <div key={factor.id} className="risk-factor">
                  <div>
                    <strong>{factor.title}</strong>
                  </div>
                  <b className={factor.points < 0 ? "protective" : ""}>
                    {factor.points > 0 ? "+" : ""}{factor.points}
                  </b>
                </div>
              ))}
            </div>
          </article>
        </section>

        <section className="monthly-review-section" id="monthly-review" aria-labelledby="monthly-review-title">
          <div className="section-title">
            <div>
              <p className="eyebrow">Monthly financial review</p>
              <h2 id="monthly-review-title">{currentReview.label}</h2>
            </div>
            <button
              className="secondary-button"
              onClick={() => {
                const next = !reviewOpen;
                setReviewOpen(next);
                if (next) recordAnalyticsEvent("monthly_review_opened", currentReview.id);
              }}
            >
              {reviewOpen ? "Hide review" : "Open review"}
            </button>
          </div>
          {reviewOpen && (
            <div className="review-layout">
              <article className="review-summary">
                <p>{currentReview.aiSummary}</p>
                <div className="review-columns">
                  <div><span>Win</span><strong>{currentReview.wins[0]}</strong></div>
                  <div><span>Risk</span><strong>{currentReview.risks[0]}</strong></div>
                  <div><span>Next</span><strong>{currentReview.recommendedActions[0]}</strong></div>
                </div>
                <small>{data.monthlyReviews.length} monthly reviews stored locally</small>
              </article>
              <article className="journal-panel">
                <p className="eyebrow">Financial decision journal</p>
                {decisionJournal.slice(0, 3).map((entry) => (
                  <div className="journal-row" key={entry.id}>
                    <div><strong>{entry.title}</strong><small>{entry.decisionDate}</small></div>
                    <span className={entry.status.toLowerCase()}>{readinessLevel(entry.status)}</span>
                    <b>{money(entry.actualFiveYearImpact ?? entry.expectedFiveYearImpact, true)}</b>
                  </div>
                ))}
              </article>
            </div>
          )}
        </section>

        <section className="future-improvements" aria-labelledby="future-improvements-title">
          <div className="section-title">
            <div><p className="eyebrow">What improved my future?</p><h2 id="future-improvements-title">Actions creating the most value</h2></div>
          </div>
          <div className="contribution-grid">
            {data.futureOutcomeContributions.slice(0, 4).map((contribution) => (
              <article key={contribution.id}>
                <span>{contribution.sharePercentage}% of modeled improvement</span>
                <strong>{contribution.title}</strong>
                <b>{money(contribution.fiveYearContribution)} at 5 years</b>
              </article>
            ))}
          </div>
        </section>

        <section className="section-block comparison-section" id="compare">
          <div className="section-title">
            <div><p className="eyebrow">Side by side</p><h2>Compare two paths</h2></div>
            <span className="education-pill">Educational simulation</span>
          </div>
          <article className="comparison-card">
            <ComparisonChart left={leftResult} right={rightResult} />
            <div className="comparison-columns">
            <ComparisonColumn
              label="Option A"
              scenario={left}
              scenarios={selectable.filter((item) => item.id !== right.id)}
              result={leftResult}
              onChange={setLeftId}
              preferred={preferred.scenario.id === left.id}
            />
            <div className="versus">VS</div>
            <ComparisonColumn
              label="Option B"
              scenario={right}
              scenarios={selectable.filter((item) => item.id !== left.id)}
              result={rightResult}
              onChange={setRightId}
              preferred={preferred.scenario.id === right.id}
            />
            </div>
          </article>
          <div className="comparison-summary">
            <div className="summary-icon"><Sparkles size={19} /></div>
            <p><strong>FutureMe take:</strong> {comparison.summary}</p>
            <button
              className="text-button"
              onClick={() => submitQuestion(
                `Explain the tradeoffs between ${left.title} and ${right.title}.`,
              )}
            >
              Ask about comparison <ArrowRight size={16} />
            </button>
          </div>
        </section>

        <section className="executive-demo" id="demo" aria-labelledby="demo-title">
          <div className="demo-persona">
            <p className="eyebrow light">Executive banking demo</p>
            <h2 id="demo-title">{data.bankingVisionDemo.title}</h2>
            <p>{data.bankingVisionDemo.subtitle}</p>
            <strong className="persona-summary">
              {data.bankingVisionDemo.audiences.join(" · ")}
            </strong>
          </div>
          <div className="demo-flow">
            <p className="eyebrow">Seven-step product story</p>
            {data.bankingVisionDemo.steps.map((step) => (
              <button
                key={step.order}
                onClick={() => {
                  if (step.order === 4) submitQuestion("If I can only do one thing this month, what should it be?");
                  const target = step.order === 7
                    ? "#monthly-review"
                    : step.order >= 5
                      ? "#banking-intelligence"
                      : "#readiness";
                  document.querySelector(target)?.scrollIntoView({ behavior: "smooth" });
                }}
              >
                <span>{step.order}</span>
                <div><strong>{step.title}</strong><small>{step.focusTarget}</small></div>
                <ArrowRight size={16} />
              </button>
            ))}
          </div>
        </section>
          </div>
        )}

        <footer>
          <ShieldCheck size={15} />
          <span>{data.disclaimer}</span>
          <i />
          <span>Mock data workspace</span>
        </footer>
      </main>

      <button
        className="assistant-fab"
        onClick={() => setAssistantOpen(true)}
        aria-label="Open FutureMe AI coach"
      >
        <Sparkles size={20} />
        <span>Ask your coach</span>
      </button>

      {assistantOpen && (
        <div className="assistant-backdrop" role="presentation" onMouseDown={() => setAssistantOpen(false)}>
          <aside
            className="assistant-drawer"
            role="dialog"
            aria-modal="true"
            aria-labelledby="assistant-title"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <header>
              <div>
                <p className="eyebrow">FutureMe Mission Coach</p>
                <h2 id="assistant-title">Move the mission forward</h2>
              </div>
              <button className="icon-button" onClick={() => setAssistantOpen(false)} aria-label="Close assistant">
                <X size={19} />
              </button>
            </header>
            <p className="assistant-context">
              Grounded in readiness, blockers, timelines, and risk. Current mission: <strong>{selectedMission.title}</strong>.
            </p>
            <div className="suggestion-chips" aria-label="Suggested questions">
              {selectedBriefing.suggestedQuestions.map((suggestion) => (
                <button key={suggestion.id} onClick={() => submitQuestion(suggestion.prompt)}>
                  {suggestion.title}
                </button>
              ))}
            </div>
            <div className="assistant-messages" aria-live="polite">
              {messages.map((message) => (
                <div key={message.id} className={`chat-message ${message.isUser ? "user" : "assistant"}`}>
                  {message.text}
                </div>
              ))}
            </div>
            <form
              className="assistant-form"
              onSubmit={(event) => {
                event.preventDefault();
                submitQuestion(question);
              }}
            >
              <label className="sr-only" htmlFor="assistant-question">Mission Coach question</label>
              <textarea
                id="assistant-question"
                value={question}
                onChange={(event) => setQuestion(event.target.value)}
                placeholder="Ask what will move this mission forward"
                rows={2}
              />
              <button type="submit" disabled={!question.trim()} aria-label="Ask FutureMe AI coach">
                <Send size={18} />
              </button>
            </form>
            <small>{data.disclaimer}</small>
          </aside>
        </div>
      )}
    </div>
  );
}

function MissionCoachBriefingCard({
  briefing,
  explanationKey,
  onSelectExplanation,
  onAsk,
}: {
  briefing: MissionCoachBriefing;
  explanationKey: CoachExplanationKey;
  onSelectExplanation: (key: CoachExplanationKey) => void;
  onAsk: (question: string) => void;
}) {
  return (
    <section className="claude-mission-briefing" aria-labelledby="claude-briefing-title">
      <header>
        <div className="claude-briefing-mark"><Sparkles size={22} /></div>
        <div>
          <p className="eyebrow light">Claude Mission Coach</p>
          <h2 id="claude-briefing-title">Your mission briefing</h2>
        </div>
        <span>{briefing.isFallback ? "Demo fallback" : briefing.modelLabel}</span>
      </header>

      <p className="claude-briefing-summary">{briefing.coachingSummary}</p>

      <div className="claude-briefing-signals">
        <article>
          <small>Focus now</small>
          <strong>{briefing.recommendedFocusArea}</strong>
        </article>
        <article>
          <small>Top risk</small>
          <strong>{briefing.topRisk}</strong>
        </article>
        <article>
          <small>Best opportunity</small>
          <strong>{briefing.topOpportunity}</strong>
        </article>
      </div>

      <div className="claude-explanation-picker" aria-label="Mission explanation views">
        {coachExplanationLabels.map((item) => (
          <button
            className={item.key === explanationKey ? "active" : ""}
            key={item.key}
            onClick={() => onSelectExplanation(item.key)}
            aria-pressed={item.key === explanationKey}
          >
            {item.label}
          </button>
        ))}
      </div>
      <div className="claude-explanation-answer" aria-live="polite">
        <Sparkles size={17} />
        <p>{briefing[explanationKey]}</p>
      </div>

      <div className="claude-briefing-footer">
        <div>
          <small>What changed</small>
          <strong>{briefing.whatChanged[0]}</strong>
        </div>
        <div className="claude-question-chips" aria-label="Mission-specific coach questions">
          {briefing.suggestedQuestions.map((question) => (
            <button key={question.id} onClick={() => onAsk(question.prompt)}>
              {question.title}<ArrowRight size={14} />
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}

function LoadingState() {
  return (
    <main className="state-screen" aria-busy="true" aria-live="polite">
      <div className="state-mark">FM</div>
      <div className="loading-bar" />
      <h1>Building your financial twin</h1>
      <p>Loading the profile, scenarios, and five-year projection model.</p>
    </main>
  );
}

function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <main className="state-screen" role="alert">
      <AlertCircle size={34} />
      <h1>We could not load FutureMe</h1>
      <p>{message}</p>
      <button className="primary-button state-action" onClick={onRetry}>
        <RotateCcw size={16} />Try again
      </button>
    </main>
  );
}

function EmptyState({ onRetry }: { onRetry: () => void }) {
  return (
    <main className="state-screen">
      <Sparkles size={34} />
      <h1>No scenarios yet</h1>
      <p>Add a financial decision to begin comparing possible futures.</p>
      <button className="primary-button state-action" onClick={onRetry}>
        <RotateCcw size={16} />Reload demo workspace
      </button>
    </main>
  );
}

function MetricCard({ icon, label, value, hint, tone }: { icon: React.ReactNode; label: string; value: string; hint: string; tone: string }) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}>{icon}</div>
      <div><p>{label}</p><strong>{value}</strong><span>{hint}</span></div>
    </article>
  );
}

function ScenarioCard({ scenario, active, onClick }: { scenario: Scenario; active: boolean; onClick: () => void }) {
  return (
    <button
      className={`scenario-card ${active ? "active" : ""}`}
      onClick={onClick}
      aria-pressed={active}
      aria-label={`${active ? "Selected scenario" : "Simulate"}: ${scenario.title}. ${scenario.subtitle}`}
    >
      <span className="scenario-icon">{scenarioIcon(scenario.type)}</span>
      <strong>{scenario.title}</strong>
      <small>{scenario.subtitle}</small>
      <span className="scenario-link">Simulate <ArrowRight size={14} /></span>
    </button>
  );
}

function Impact({ label, value, positive }: { label: string; value: string; positive: boolean }) {
  return <div><span>{label}</span><strong className={positive ? "good" : "watch"}>{value}</strong></div>;
}

function ComparisonColumn({
  label,
  scenario,
  scenarios,
  result,
  onChange,
  preferred,
}: {
  label: string;
  scenario: Scenario;
  scenarios: Scenario[];
  result: ScenarioResult;
  onChange: (value: string) => void;
  preferred: boolean;
}) {
  return (
    <div className={`comparison-column ${preferred ? "preferred" : ""}`}>
      <div className="comparison-label">
        <span>{label}</span>
        {preferred && <strong><Sparkles size={13} />Recommended</strong>}
      </div>
      <label className="select-wrap">
        <span className="sr-only">{label} scenario</span>
        <select
          aria-label={`${label} scenario`}
          value={scenario.id}
          onChange={(event) => onChange(event.target.value)}
        >
          {scenarios.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
        </select>
        <ChevronDown size={17} />
      </label>
      <div className="comparison-hero">
        <div className="scenario-icon large">{scenarioIcon(scenario.type)}</div>
        <div><strong>{money(result.projectedNetWorth5Years, true)}</strong><span>projected net worth</span></div>
      </div>
      <dl>
        <div><dt>Monthly surplus</dt><dd>{money(result.projectedMonthlySurplus)}</dd></div>
        <div><dt>Emergency runway</dt><dd>{result.emergencyFundMonths.toFixed(1)} mo</dd></div>
        <div><dt>Risk score</dt><dd>{result.riskScore.value}/100</dd></div>
        <div><dt>5-year change</dt><dd className={result.netWorthDelta5Years >= 0 ? "good" : "watch"}>{signedMoney(result.netWorthDelta5Years)}</dd></div>
      </dl>
    </div>
  );
}

export default App;
