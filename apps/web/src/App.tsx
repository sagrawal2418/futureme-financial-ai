import {
  AlertTriangle,
  ArrowRight,
  Bell,
  CheckCircle2,
  ChevronRight,
  Home,
  Lightbulb,
  Menu,
  MessageCircle,
  Moon,
  ShieldCheck,
  Sparkles,
  Sun,
  Target,
  TrendingUp,
  UserRound,
  X,
} from "lucide-react";
import { useEffect, useState } from "react";

import {
  askFutureMe,
  bootstrapProduct,
  completeMissionAction,
  recordAnalyticsEvent,
  type CustomerPersona,
  type Mission,
  type ProductBootstrap,
} from "./shared";

type ProductTab = "home" | "missions" | "insights" | "coach" | "profile";

interface ChatMessage {
  id: number;
  text: string;
  isUser: boolean;
}

const tabIcons: Record<ProductTab, typeof Home> = {
  home: Home,
  missions: Target,
  insights: Lightbulb,
  coach: MessageCircle,
  profile: UserRound,
};

const money = (value: number, compact = false) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
    notation: compact ? "compact" : "standard",
  }).format(value);

const displayDate = (value: string) =>
  new Intl.DateTimeFormat("en-US", {
    month: "short",
    year: "numeric",
  }).format(new Date(`${value}T00:00:00`));

const label = (value: string) =>
  value
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/^\w/, (letter) => letter.toUpperCase());

function App() {
  const [data, setData] = useState<ProductBootstrap | null>(null);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState<ProductTab>("home");
  const [selectedMissionId, setSelectedMissionId] = useState("mission-home");
  const [selectedPersonaId, setSelectedPersonaId] = useState("young-family");
  const [menuOpen, setMenuOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 0,
      text: "Ask about a mission, blocker, timeline, or next action. I will use the calculated plan.",
      isUser: false,
    },
  ]);
  const [darkMode, setDarkMode] = useState(() => {
    const stored = window.localStorage.getItem("futureme-theme");
    return stored
      ? stored === "dark"
      : window.matchMedia("(prefers-color-scheme: dark)").matches;
  });

  useEffect(() => {
    try {
      setData(bootstrapProduct());
    } catch {
      setError("FutureMe could not load the shared product workspace.");
    }
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = darkMode ? "dark" : "light";
    window.localStorage.setItem("futureme-theme", darkMode ? "dark" : "light");
  }, [darkMode]);

  if (error) {
    return (
      <main className="state-screen">
        <AlertTriangle size={32} />
        <h1>FutureMe could not load</h1>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>Try again</button>
      </main>
    );
  }

  if (!data) {
    return (
      <main className="state-screen" aria-live="polite">
        <span className="loading-mark">FM</span>
        <h1>Preparing your next move</h1>
      </main>
    );
  }

  const selectedMission =
    data.missions.find((mission) => mission.missionId === selectedMissionId) ??
    data.missions[0];
  const selectedExecution =
    data.missionExecution.plans.find((plan) => plan.missionId === selectedMission.missionId) ??
    data.missionExecution.plans[0];
  const selectedBriefing =
    data.missionCoachBriefings.find((item) => item.missionId === selectedMission.missionId) ??
    data.missionCoachBriefings[0];
  const selectedPersona =
    data.customerPersonas.find((persona) => persona.id === selectedPersonaId) ??
    data.customerPersonas[0];
  const currentNavigation =
    data.productStrategy.navigation.find((item) => item.id === activeTab) ??
    data.productStrategy.navigation[0];

  const navigate = (tab: ProductTab) => {
    setActiveTab(tab);
    setMenuOpen(false);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const selectMission = (mission: Mission) => {
    setSelectedMissionId(mission.missionId);
    recordAnalyticsEvent("readiness_viewed", mission.missionId);
  };

  const completeNextAction = () => {
    const nextAction = selectedExecution.actionPlan.nextAction;
    if (!nextAction) return;
    setData(completeMissionAction(nextAction.actionId));
  };

  const askCoach = (prompt: string) => {
    const normalized = prompt.trim();
    if (!normalized) return;
    const scopedPrompt = `${normalized} Focus on my ${selectedMission.title} mission.`;
    const response = askFutureMe(scopedPrompt);
    setMessages((current) => [
      ...current,
      { id: Date.now(), text: normalized, isUser: true },
      { id: Date.now() + 1, text: response.answer, isUser: false },
    ]);
    setQuestion("");
    setActiveTab("coach");
  };

  return (
    <div className="product-shell">
      <aside className={menuOpen ? "product-sidebar open" : "product-sidebar"}>
        <div className="brand-row">
          <span>FM</span>
          <div>
            <strong>FutureMe</strong>
            <small>Life decision intelligence</small>
          </div>
        </div>
        <button className="close-menu" onClick={() => setMenuOpen(false)} aria-label="Close menu">
          <X size={20} />
        </button>
        <nav aria-label="Primary navigation">
          {data.productStrategy.navigation.map((item) => {
            const tab = item.id as ProductTab;
            const Icon = tabIcons[tab];
            return (
              <button
                key={item.id}
                className={activeTab === tab ? "active" : ""}
                onClick={() => navigate(tab)}
                aria-current={activeTab === tab ? "page" : undefined}
              >
                <Icon size={20} />
                <span>
                  <strong>{item.label}</strong>
                  <small>{item.customerQuestion}</small>
                </span>
              </button>
            );
          })}
        </nav>
        <div className="sidebar-note">
          <ShieldCheck size={20} />
          <p><strong>Private by design</strong> Demo data stays on this device.</p>
        </div>
        <div className="account-row">
          <span>JL</span>
          <div>
            <strong>{data.identity.displayName}</strong>
            <small>{data.identity.householdName}</small>
          </div>
        </div>
      </aside>

      <main className="product-main">
        <header className="product-header">
          <button className="menu-trigger" onClick={() => setMenuOpen(true)} aria-label="Open menu">
            <Menu size={22} />
          </button>
          <div>
            <p className="eyebrow">{currentNavigation.label}</p>
            <h1>{currentNavigation.customerQuestion}</h1>
          </div>
          <div className="header-actions">
            <button
              className="icon-button"
              onClick={() => setNotificationsOpen((current) => !current)}
              aria-label="Mission notifications"
              aria-expanded={notificationsOpen}
            >
              <Bell size={19} />
              <span>{data.missionExecution.notifications.length}</span>
            </button>
            <button
              className="icon-button"
              onClick={() => setDarkMode((current) => !current)}
              aria-label={darkMode ? "Use light theme" : "Use dark theme"}
            >
              {darkMode ? <Sun size={19} /> : <Moon size={19} />}
            </button>
          </div>
        </header>

        {notificationsOpen && (
          <section className="notification-panel" aria-label="Mission notification center">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Mission updates</p>
                <h2>What changed</h2>
              </div>
              <button onClick={() => setNotificationsOpen(false)} aria-label="Close notifications">
                <X size={18} />
              </button>
            </div>
            {data.missionExecution.notifications.slice(0, 5).map((notification) => (
              <button
                key={notification.notificationId}
                onClick={() => {
                  setSelectedMissionId(notification.missionId);
                  setNotificationsOpen(false);
                  setActiveTab("missions");
                }}
              >
                <Bell size={17} />
                <span>
                  <strong>{notification.title}</strong>
                  <small>{notification.message}</small>
                </span>
              </button>
            ))}
          </section>
        )}

        {activeTab === "home" && (
          <HomeView
            data={data}
            onOpenMissions={() => navigate("missions")}
            onOpenCoach={() => navigate("coach")}
          />
        )}
        {activeTab === "missions" && (
          <MissionsView
            data={data}
            mission={selectedMission}
            onSelectMission={selectMission}
            onCompleteAction={completeNextAction}
            onAsk={askCoach}
          />
        )}
        {activeTab === "insights" && <InsightsView data={data} />}
        {activeTab === "coach" && (
          <CoachView
            data={data}
            mission={selectedMission}
            briefing={selectedBriefing}
            messages={messages}
            question={question}
            onQuestionChange={setQuestion}
            onAsk={askCoach}
          />
        )}
        {activeTab === "profile" && (
          <ProfileView
            data={data}
            persona={selectedPersona}
            onSelectPersona={setSelectedPersonaId}
          />
        )}

        <footer>
          <span>{data.disclaimer}</span>
          <strong>{data.productStrategy.positioningStatement}</strong>
        </footer>
      </main>
    </div>
  );
}

function HomeView({
  data,
  onOpenMissions,
  onOpenCoach,
}: {
  data: ProductBootstrap;
  onOpenMissions: () => void;
  onOpenCoach: () => void;
}) {
  const topRisk = data.missionControl.risks[0];
  const topOpportunity = data.missionControl.opportunities[0];
  const readinessChange = data.financialExplainability.netChange;

  return (
    <>
      <section className="focus-hero">
        <div>
          <p className="eyebrow light">Your highest-impact action</p>
          <h2>{data.nextBestAction.title}</h2>
          <p>{data.nextBestAction.callout}</p>
          <button onClick={onOpenMissions}>
            Add to this month&apos;s plan <ArrowRight size={18} />
          </button>
        </div>
        <dl>
          <div><dt>5-year impact</dt><dd>{money(data.nextBestAction.fiveYearImpact, true)}</dd></div>
          <div><dt>Confidence</dt><dd>{data.nextBestAction.confidenceScore}%</dd></div>
          <div><dt>Monthly effort</dt><dd>{money(data.nextBestAction.monthlyCommitment)}</dd></div>
        </dl>
      </section>

      <section className="today-grid" aria-label="Today's financial priorities">
        <SummaryCard
          icon={TrendingUp}
          eyebrow="Readiness change"
          title={`${readinessChange >= 0 ? "+" : ""}${readinessChange} points`}
          body={data.financialExplainability.summary}
          tone={readinessChange >= 0 ? "positive" : "warning"}
        />
        <SummaryCard
          icon={AlertTriangle}
          eyebrow="Biggest risk"
          title={topRisk.title}
          body={topRisk.description}
          tone="warning"
        />
        <SummaryCard
          icon={Lightbulb}
          eyebrow="Biggest opportunity"
          title={topOpportunity.title}
          body={topOpportunity.description}
          tone="positive"
        />
      </section>

      <section className="home-lower-grid">
        <article className="mission-snapshot">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Active missions</p>
              <h2>What you are preparing for</h2>
            </div>
            <button className="text-button" onClick={onOpenMissions}>View all</button>
          </div>
          {data.missionControl.activeMissions.slice(0, 4).map((mission) => (
            <div key={mission.missionId}>
              <span>
                <strong>{mission.title}</strong>
                <small>{mission.blockers[0]}</small>
              </span>
              <b>{mission.readinessScore}</b>
            </div>
          ))}
        </article>

        <article className="coach-invite">
          <Sparkles size={26} />
          <p className="eyebrow light">FutureMe Coach</p>
          <h2>Understand the recommendation.</h2>
          <p>Ask why it matters, what changes next, or how to move faster.</p>
          <button onClick={onOpenCoach}>Ask my coach <ChevronRight size={18} /></button>
        </article>
      </section>
    </>
  );
}

function MissionsView({
  data,
  mission,
  onSelectMission,
  onCompleteAction,
  onAsk,
}: {
  data: ProductBootstrap;
  mission: Mission;
  onSelectMission: (mission: Mission) => void;
  onCompleteAction: () => void;
  onAsk: (prompt: string) => void;
}) {
  const execution = data.missionExecution.plans.find(
    (item) => item.missionId === mission.missionId,
  ) ?? data.missionExecution.plans[0];
  const nextAction = execution.actionPlan.nextAction;

  return (
    <>
      <section className="mission-picker" aria-label="Active missions">
        {data.missionControl.activeMissions.map((item) => (
          <button
            key={item.missionId}
            className={item.missionId === mission.missionId ? "active" : ""}
            onClick={() => onSelectMission(item)}
            aria-pressed={item.missionId === mission.missionId}
          >
            <span>{item.title}</span>
            <strong>{item.readinessScore}</strong>
            <small>{item.progressPercentage}% complete</small>
          </button>
        ))}
      </section>

      <section className="mission-overview">
        <article className="readiness-card-large">
          <div className={`health-pill ${execution.health.status.toLowerCase()}`}>
            {label(execution.health.status)} health
          </div>
          <div className="readiness-title">
            <span><strong>{mission.readinessScore}</strong><small>ready</small></span>
            <div>
              <p className="eyebrow">{label(mission.status)}</p>
              <h2>{mission.title}</h2>
              <p>{execution.health.summary}</p>
            </div>
          </div>
          <div className="blocker-row">
            <AlertTriangle size={18} />
            <span><small>Biggest blocker</small><strong>{mission.blockers[0]}</strong></span>
          </div>
        </article>

        <article className="next-step-card">
          <p className="eyebrow light">Do this next</p>
          <h2>{nextAction?.title ?? "Mission complete"}</h2>
          <p>{nextAction?.description ?? "All currently modeled actions are complete."}</p>
          {nextAction && (
            <div className="impact-row">
              <span><small>Readiness</small><strong>+{nextAction.readinessGain}</strong></span>
              <span><small>Target</small><strong>{displayDate(nextAction.targetDate)}</strong></span>
              <span><small>Effort</small><strong>{label(nextAction.effort)}</strong></span>
            </div>
          )}
          <button disabled={!nextAction} onClick={onCompleteAction}>
            {nextAction ? "Mark complete" : "Completed"} <CheckCircle2 size={18} />
          </button>
        </article>
      </section>

      <section className="action-plan">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Action plan</p>
            <h2>One sequence, not another dashboard</h2>
          </div>
          <span>{execution.progress.completedActions}/{execution.progress.totalActions} done</span>
        </div>
        {execution.actionPlan.actions.map((action, index) => (
          <article key={action.actionId} className={action.completionStatus.toLowerCase()}>
            <span className="action-number">
              {action.completionStatus === "COMPLETED" ? <CheckCircle2 size={18} /> : index + 1}
            </span>
            <div>
              <strong>{action.title}</strong>
              <p>{action.blockerMessage ?? action.description}</p>
            </div>
            <b>{label(action.completionStatus)}</b>
          </article>
        ))}
      </section>

      <details className="mission-details">
        <summary>Roadmap, history, and decision scenarios</summary>
        <div className="details-grid">
          <article>
            <p className="eyebrow">Roadmap</p>
            {execution.roadmap.stages.map((stage) => (
              <div key={stage.horizon}>
                <strong>{stage.label}</strong>
                <span>+{stage.expectedReadinessGrowth} readiness</span>
                <small>{stage.upcomingActions[0]?.title ?? "Maintain progress"}</small>
              </div>
            ))}
          </article>
          <article>
            <p className="eyebrow">Recent change</p>
            <h3>{execution.history.events[0]?.title}</h3>
            <p>{execution.history.events[0]?.detail}</p>
            <strong>Projected completion: {displayDate(mission.targetDate)}</strong>
          </article>
          <article>
            <p className="eyebrow">Decision scenarios</p>
            {execution.scenarioImpacts.slice(0, 3).map((scenario) => (
              <div key={scenario.scenarioId}>
                <strong>{scenario.title}</strong>
                <span>{scenario.readinessImpact >= 0 ? "+" : ""}{scenario.readinessImpact} readiness</span>
                <small>{scenario.summary}</small>
              </div>
            ))}
          </article>
        </div>
      </details>

      <button
        className="coach-strip"
        onClick={() => onAsk("What should I do next?")}
      >
        <Sparkles size={22} />
        <span><strong>Need the reasoning?</strong><small>Ask Coach about this mission.</small></span>
        <ArrowRight size={20} />
      </button>
    </>
  );
}

function InsightsView({ data }: { data: ProductBootstrap }) {
  const review = data.monthlyReviews[0];
  return (
    <>
      <section className="insight-lead">
        <div>
          <p className="eyebrow light">This month</p>
          <h2>{review.aiSummary}</h2>
        </div>
        <span>{review.label}</span>
      </section>

      <section className="insight-columns">
        <InsightList
          title="Risks"
          icon={AlertTriangle}
          items={review.risks}
          tone="warning"
        />
        <InsightList
          title="Opportunities"
          icon={TrendingUp}
          items={review.opportunities}
          tone="positive"
        />
        <InsightList
          title="Recommended actions"
          icon={CheckCircle2}
          items={review.recommendedActions}
          tone="neutral"
        />
      </section>

      <section className="insights-lower-grid">
        <article>
          <div className="section-heading">
            <div>
              <p className="eyebrow">Money leaks</p>
              <h2>Costs worth fixing</h2>
            </div>
            <strong>
              {money(
                data.moneyLeaks.reduce(
                  (total, item) => total + item.estimatedAnnualLoss,
                  0,
                ),
                true,
              )}/yr
            </strong>
          </div>
          {data.moneyLeaks.slice(0, 4).map((leak) => (
            <div className="compact-row" key={leak.id}>
              <span><strong>{leak.title}</strong><small>{leak.fixRecommendation}</small></span>
              <b>{money(leak.estimatedAnnualLoss)}</b>
            </div>
          ))}
        </article>

        <article>
          <p className="eyebrow">Why the score changed</p>
          <div className="score-change">
            <span>{data.financialExplainability.previousScore}</span>
            <ArrowRight size={20} />
            <strong>{data.financialExplainability.currentScore}</strong>
          </div>
          {data.financialExplainability.factors.map((factor) => (
            <div className="compact-row" key={factor.id}>
              <span><strong>{factor.title}</strong><small>{factor.description}</small></span>
              <b className={factor.pointImpact >= 0 ? "positive" : "negative"}>
                {factor.pointImpact >= 0 ? "+" : ""}{factor.pointImpact}
              </b>
            </div>
          ))}
        </article>
      </section>
    </>
  );
}

function CoachView({
  data,
  mission,
  briefing,
  messages,
  question,
  onQuestionChange,
  onAsk,
}: {
  data: ProductBootstrap;
  mission: Mission;
  briefing: ProductBootstrap["missionCoachBriefings"][number];
  messages: ChatMessage[];
  question: string;
  onQuestionChange: (value: string) => void;
  onAsk: (prompt: string) => void;
}) {
  const evaluation = data.aiEvaluationDashboard;
  return (
    <>
      <section className="coach-layout">
        <article className="coach-briefing">
          <div className="provider-pill">
            <Sparkles size={16} /> {briefing.providerLabel} · {briefing.modelLabel}
          </div>
          <p className="eyebrow light">{mission.title} briefing</p>
          <h2>{briefing.coachingSummary}</h2>
          <div>
            <span><small>Focus now</small><strong>{briefing.recommendedFocusArea}</strong></span>
            <span><small>Top risk</small><strong>{briefing.topRisk}</strong></span>
            <span><small>Best opportunity</small><strong>{briefing.topOpportunity}</strong></span>
          </div>
        </article>

        <article className="chat-panel">
          <div className="prompt-row">
            {briefing.suggestedQuestions.slice(0, 4).map((item) => (
              <button key={item.id} onClick={() => onAsk(item.prompt)}>{item.title}</button>
            ))}
          </div>
          <div className="messages">
            {messages.slice(-6).map((message) => (
              <p className={message.isUser ? "user" : ""} key={message.id}>{message.text}</p>
            ))}
          </div>
          <form
            onSubmit={(event) => {
              event.preventDefault();
              onAsk(question);
            }}
          >
            <label htmlFor="coach-question">Ask about your mission</label>
            <div>
              <input
                id="coach-question"
                value={question}
                onChange={(event) => onQuestionChange(event.target.value)}
                placeholder="What is my biggest blocker?"
              />
              <button disabled={!question.trim()} aria-label="Send question">
                <ArrowRight size={19} />
              </button>
            </div>
          </form>
        </article>
      </section>

      <section className="evaluation-dashboard" aria-labelledby="evaluation-title">
        <div className="section-heading">
          <div>
            <p className="eyebrow">AI evaluation dashboard</p>
            <h2 id="evaluation-title">Can we trust the explanation quality?</h2>
          </div>
          <span>{evaluation.totalPrompts} realistic prompts</span>
        </div>
        <div className="evaluation-status">
          <ShieldCheck size={22} />
          <div><strong>{evaluation.statusLabel}</strong><p>{evaluation.methodologyNote}</p></div>
        </div>
        <div className="quality-metrics">
          {[
            ["Response quality", evaluation.overallQualityScore],
            ["Reasoning quality", evaluation.reasoningQualityScore],
            ["Consistency", evaluation.consistencyScore],
            ["Hallucination safety", evaluation.hallucinationRiskScore],
            ["Usefulness", evaluation.explanationUsefulnessScore],
          ].map(([name, score]) => (
            <div key={name}>
              <strong>{score === 0 ? "Pending" : `${score}`}</strong>
              <span>{name}</span>
            </div>
          ))}
        </div>
        <div className="category-grid">
          {evaluation.categories.map((category) => (
            <div key={category.category}>
              <span>{category.label}</span>
              <strong>{category.promptCount} prompts</strong>
              <small>{category.passRate === 0 ? "Ready for live run" : `${category.passRate}% pass`}</small>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

function ProfileView({
  data,
  persona,
  onSelectPersona,
}: {
  data: ProductBootstrap;
  persona: CustomerPersona;
  onSelectPersona: (id: string) => void;
}) {
  return (
    <>
      <section className="profile-grid">
        <article>
          <p className="eyebrow">Financial profile</p>
          <h2>{data.identity.householdName}</h2>
          <dl>
            <div><dt>Annual income</dt><dd>{money(data.profile.annualGrossIncome)}</dd></div>
            <div><dt>Monthly take-home</dt><dd>{money(data.profile.monthlyNetIncome)}</dd></div>
            <div><dt>Liquid savings</dt><dd>{money(data.profile.liquidSavings)}</dd></div>
            <div><dt>Investments</dt><dd>{money(data.profile.investmentBalance)}</dd></div>
          </dl>
        </article>
        <article>
          <p className="eyebrow">Family profile</p>
          <h2>{data.profile.dependents} dependent</h2>
          <dl>
            <div><dt>Home state</dt><dd>{data.profile.stateCode}</dd></div>
            <div><dt>Housing payment</dt><dd>{money(data.profile.housingPayment)}</dd></div>
            <div><dt>Childcare</dt><dd>{money(data.profile.monthlyChildcare)}</dd></div>
            <div><dt>Card debt</dt><dd>{money(data.profile.creditCardDebt)}</dd></div>
          </dl>
        </article>
        <article>
          <p className="eyebrow">Connected data</p>
          <h2>Demo accounts</h2>
          <dl>
            <div><dt>Cash</dt><dd>{data.cashAccounts.length}</dd></div>
            <div><dt>Debt</dt><dd>{data.debtAccounts.length}</dd></div>
            <div><dt>Investments</dt><dd>{data.investmentAccounts.length}</dd></div>
            <div><dt>Transactions</dt><dd>{data.transactions.length}</dd></div>
          </dl>
        </article>
      </section>

      <section className="persona-lab">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Realistic customer journeys</p>
            <h2>Test the product against different households</h2>
          </div>
        </div>
        <div className="persona-tabs">
          {data.customerPersonas.map((item) => (
            <button
              key={item.id}
              className={item.id === persona.id ? "active" : ""}
              onClick={() => onSelectPersona(item.id)}
            >
              {item.title}
            </button>
          ))}
        </div>
        <article className="persona-detail">
          <div>
            <p className="eyebrow">{persona.title}</p>
            <h2>{persona.summary}</h2>
            <div className="persona-scores">
              {persona.expectedReadinessScores.map((item) => (
                <span key={item.category}><strong>{item.score}</strong><small>{item.category}</small></span>
              ))}
            </div>
          </div>
          <div>
            <strong>Expected mission plan</strong>
            <ol>
              {persona.expectedMissionPlan.map((item) => <li key={item}>{item}</li>)}
            </ol>
          </div>
        </article>
      </section>

      <section className="demo-story">
        <div>
          <p className="eyebrow light">Executive demo mode</p>
          <h2>{data.executiveDemoStory.title}</h2>
          <p>{data.executiveDemoStory.opening}</p>
        </div>
        <ol>
          {data.executiveDemoStory.steps.map((step) => <li key={step}>{step}</li>)}
        </ol>
        <strong>{data.executiveDemoStory.closing}</strong>
      </section>
    </>
  );
}

function SummaryCard({
  icon: Icon,
  eyebrow,
  title,
  body,
  tone,
}: {
  icon: typeof TrendingUp;
  eyebrow: string;
  title: string;
  body: string;
  tone: "positive" | "warning";
}) {
  return (
    <article className={`summary-card ${tone}`}>
      <Icon size={21} />
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      <p>{body}</p>
    </article>
  );
}

function InsightList({
  title,
  icon: Icon,
  items,
  tone,
}: {
  title: string;
  icon: typeof AlertTriangle;
  items: string[];
  tone: "warning" | "positive" | "neutral";
}) {
  return (
    <article className={`insight-list ${tone}`}>
      <div><Icon size={20} /><h2>{title}</h2></div>
      {items.slice(0, 4).map((item) => (
        <p key={item}><ChevronRight size={16} />{item}</p>
      ))}
    </article>
  );
}

export default App;
