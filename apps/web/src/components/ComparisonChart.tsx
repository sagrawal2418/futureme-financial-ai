import type { ScenarioResult } from "../shared";

interface ComparisonChartProps {
  left: ScenarioResult;
  right: ScenarioResult;
}

const linePath = (
  values: number[],
  min: number,
  max: number,
) =>
  values
    .map((value, index) => {
      const x = 24 + (index / Math.max(1, values.length - 1)) * 452;
      const y = 150 - ((value - min) / Math.max(1, max - min)) * 116;
      return `${index === 0 ? "M" : "L"} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(" ");

export function ComparisonChart({ left, right }: ComparisonChartProps) {
  const leftValues = left.projections.map((point) => point.scenarioNetWorth);
  const rightValues = right.projections.map((point) => point.scenarioNetWorth);
  const allValues = [...leftValues, ...rightValues];
  const min = Math.min(...allValues) * 0.96;
  const max = Math.max(...allValues) * 1.04;

  return (
    <figure
      className="comparison-chart"
      aria-label={`Net worth comparison. ${left.scenario.title} versus ${right.scenario.title} over five years.`}
    >
      <figcaption>
        <div>
          <span className="legend-dot comparison-left-dot" />
          {left.scenario.title}
        </div>
        <div>
          <span className="legend-dot comparison-right-dot" />
          {right.scenario.title}
        </div>
      </figcaption>
      <svg viewBox="0 0 500 176" role="img" aria-hidden="true">
        {[34, 72, 110, 148].map((y) => (
          <line key={y} x1="24" x2="476" y1={y} y2={y} className="chart-grid" />
        ))}
        <path d={linePath(leftValues, min, max)} className="comparison-line left" />
        <path d={linePath(rightValues, min, max)} className="comparison-line right" />
        {left.projections.map((point, index) => {
          const x = 24 + (index / Math.max(1, left.projections.length - 1)) * 452;
          return (
            <text key={point.year} x={x} y="171" textAnchor="middle" className="chart-label">
              {point.year === 0 ? "Now" : `${point.year}Y`}
            </text>
          );
        })}
      </svg>
    </figure>
  );
}
