import {
  AlertCircle,
  ArrowRight,
  Baby,
  BarChart3,
  Bell,
  BriefcaseBusiness,
  ChevronDown,
  CircleDollarSign,
  CreditCard,
  Home,
  Landmark,
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
  WalletCards,
  X,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";

import { ComparisonChart } from "./components/ComparisonChart";
import { ProjectionChart } from "./components/ProjectionChart";
import {
  askFutureMe,
  bootstrapProduct,
  compareScenarios,
  simulateScenario,
  type ProductBootstrap,
  type Scenario,
  type ScenarioResult,
  type ScenarioType,
} from "./shared";

interface ChatMessage {
  id: number;
  text: string;
  isUser: boolean;
}

const money = (value: number, compact = false) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
    notation: compact ? "compact" : "standard",
  }).format(value);

const signedMoney = (value: number) =>
  `${value >= 0 ? "+" : "-"}${money(Math.abs(value))}`;

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
  }
};

function App() {
  const [loadState, setLoadState] = useState<
    | { status: "loading" }
    | { status: "error"; message: string }
    | { status: "content"; data: ProductBootstrap }
  >({ status: "loading" });
  const [selectedId, setSelectedId] = useState("move-to-texas");
  const [leftId, setLeftId] = useState("move-to-texas");
  const [rightId, setRightId] = useState("stay-in-new-jersey");
  const [mobileMenu, setMobileMenu] = useState(false);
  const [assistantOpen, setAssistantOpen] = useState(false);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 0,
      text: "Ask me about a major decision. I use the same assumptions and scenario engine as your dashboard.",
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

  const submitQuestion = (prompt: string) => {
    const normalized = prompt.trim();
    if (!normalized) return;
    const response = askFutureMe(normalized, selected.id);
    setMessages((current) => [
      ...current,
      { id: Date.now(), text: normalized, isUser: true },
      { id: Date.now() + 1, text: response.answer, isUser: false },
    ]);
    setQuestion("");
    setAssistantOpen(true);
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
          <a className="active" href="#dashboard"><BarChart3 size={19} />Overview</a>
          <a href="#scenarios"><Sparkles size={19} />Scenarios</a>
          <a href="#compare"><WalletCards size={19} />Compare</a>
          <button className="nav-button" onClick={() => setAssistantOpen(true)}><MessageCircle size={19} />AI assistant</button>
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
            <p className="eyebrow">Financial digital twin</p>
            <h1>Your future, modeled clearly.</h1>
          </div>
          <div className="header-actions">
            <button className="icon-button" aria-label="Notifications"><Bell size={19} /></button>
            <button
              className="icon-button"
              aria-label={darkMode ? "Use light theme" : "Use dark theme"}
              onClick={() => setDarkMode((current) => !current)}
            >
              {darkMode ? <Sun size={19} /> : <Moon size={19} />}
            </button>
            <button className="primary-button" aria-label="Ask FutureMe" onClick={() => setAssistantOpen(true)}>
              <Sparkles size={17} />Ask FutureMe
            </button>
          </div>
        </header>

        <div className="offline-banner" role="status">
          <ShieldCheck size={16} />
          <span>Shared Kotlin engine active. Mock data stays on this device.</span>
          <strong>Demo mode</strong>
        </div>

        <section className="hero-grid" id="dashboard">
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

        <section className="section-block" id="scenarios">
          <div className="section-title">
            <div><p className="eyebrow">Decision lab</p><h2>Explore a different future</h2></div>
            <button className="text-button">View all scenarios <ArrowRight size={16} /></button>
          </div>
          <div className="scenario-strip">
            {selectable.map((item) => (
              <ScenarioCard
                key={item.id}
                scenario={item}
                active={item.id === selected.id}
                onClick={() => setSelectedId(item.id)}
              />
            ))}
          </div>

          <article className="insight-panel">
            <div className="insight-main">
              <div className="ai-badge"><Sparkles size={18} />FutureMe insight</div>
              <h2>{selected.title}</h2>
              <p>{result.recommendation}</p>
              <div className="tradeoffs">
                {result.tradeoffs.slice(0, 3).map((tradeoff) => (
                  <span key={tradeoff}><i />{tradeoff}</span>
                ))}
              </div>
            </div>
            <div className="impact-grid">
              <Impact label="Monthly impact" value={signedMoney(result.monthlyCashFlowImpact)} positive={result.monthlyCashFlowImpact >= 0} />
              <Impact label="Five-year change" value={signedMoney(result.netWorthDelta5Years)} positive={result.netWorthDelta5Years >= 0} />
              <Impact label="Risk score" value={`${result.riskScore.value}/100`} positive={result.riskScore.value < 45} />
              <Impact label="Cash runway" value={`${result.emergencyFundMonths.toFixed(1)} months`} positive={result.emergencyFundMonths >= 6} />
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
                    <span>{factor.explanation}</span>
                  </div>
                  <b className={factor.points < 0 ? "protective" : ""}>
                    {factor.points > 0 ? "+" : ""}{factor.points}
                  </b>
                </div>
              ))}
            </div>
          </article>
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
              scenarios={selectable}
              result={leftResult}
              onChange={setLeftId}
              preferred={preferred.scenario.id === left.id}
            />
            <div className="versus">VS</div>
            <ComparisonColumn
              label="Option B"
              scenario={right}
              scenarios={selectable}
              result={rightResult}
              onChange={setRightId}
              preferred={preferred.scenario.id === right.id}
            />
            </div>
          </article>
          <div className="comparison-summary">
            <div className="summary-icon"><Sparkles size={19} /></div>
            <p><strong>FutureMe take:</strong> {comparison.summary}</p>
            <button className="text-button">Open full comparison <ArrowRight size={16} /></button>
          </div>
        </section>

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
        aria-label="Open FutureMe financial assistant"
      >
        <Sparkles size={20} />
        <span>Ask FutureMe</span>
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
                <p className="eyebrow">AI financial guide</p>
                <h2 id="assistant-title">Ask FutureMe</h2>
              </div>
              <button className="icon-button" onClick={() => setAssistantOpen(false)} aria-label="Close assistant">
                <X size={19} />
              </button>
            </header>
            <p className="assistant-context">Mock AI grounded in the active profile and selected scenario: <strong>{selected.title}</strong>.</p>
            <div className="suggestion-chips" aria-label="Suggested questions">
              {data.suggestedQuestions.map((suggestion) => (
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
              <label className="sr-only" htmlFor="assistant-question">Financial assistant question</label>
              <textarea
                id="assistant-question"
                value={question}
                onChange={(event) => setQuestion(event.target.value)}
                placeholder="Ask a financial what-if question"
                rows={2}
              />
              <button type="submit" disabled={!question.trim()} aria-label="Send question to FutureMe">
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
