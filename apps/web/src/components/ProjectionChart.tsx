import type { ProjectionPoint } from "../shared";

interface ProjectionChartProps {
  points: ProjectionPoint[];
}

const pathFor = (
  points: ProjectionPoint[],
  value: (point: ProjectionPoint) => number,
  min: number,
  max: number,
) =>
  points
    .map((point, index) => {
      const x = 18 + (index / Math.max(1, points.length - 1)) * 464;
      const y = 168 - ((value(point) - min) / Math.max(1, max - min)) * 138;
      return `${index === 0 ? "M" : "L"} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(" ");

export function ProjectionChart({ points }: ProjectionChartProps) {
  const all = points.flatMap((point) => [
    point.baselineNetWorth,
    point.scenarioNetWorth,
  ]);
  const min = Math.min(...all) * 0.96;
  const max = Math.max(...all) * 1.04;
  const baselinePath = pathFor(points, (point) => point.baselineNetWorth, min, max);
  const scenarioPath = pathFor(points, (point) => point.scenarioNetWorth, min, max);

  return (
    <div className="chart" aria-label="Five-year net worth projection chart">
      <svg viewBox="0 0 500 190" role="img">
        <defs>
          <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#6ee7b7" stopOpacity="0.3" />
            <stop offset="100%" stopColor="#6ee7b7" stopOpacity="0" />
          </linearGradient>
        </defs>
        {[30, 76, 122, 168].map((y) => (
          <line key={y} x1="18" y1={y} x2="482" y2={y} className="chart-grid" />
        ))}
        <path
          d={`${scenarioPath} L 482 168 L 18 168 Z`}
          fill="url(#areaGradient)"
        />
        <path d={baselinePath} className="chart-line baseline" />
        <path d={scenarioPath} className="chart-line scenario" />
        {points.map((point, index) => {
          const x = 18 + (index / Math.max(1, points.length - 1)) * 464;
          return (
            <text key={point.year} x={x} y="187" textAnchor="middle" className="chart-label">
              {point.year === 0 ? "Now" : `${point.year}Y`}
            </text>
          );
        })}
      </svg>
      <div className="chart-legend">
        <span><i className="legend-dot scenario-dot" />Selected scenario</span>
        <span><i className="legend-dot baseline-dot" />Current plan</span>
      </div>
    </div>
  );
}
