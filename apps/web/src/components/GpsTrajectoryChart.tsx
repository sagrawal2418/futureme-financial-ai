import type { ProjectionPoint } from "../shared";

interface GpsTrajectoryChartProps {
  current: ProjectionPoint[];
  improved: ProjectionPoint[];
}

const pathFor = (
  points: ProjectionPoint[],
  min: number,
  max: number,
) =>
  points
    .map((point, index) => {
      const x = 18 + (index / Math.max(1, points.length - 1)) * 464;
      const y =
        168 -
        ((point.scenarioNetWorth - min) / Math.max(1, max - min)) * 138;
      return `${index === 0 ? "M" : "L"} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(" ");

export function GpsTrajectoryChart({
  current,
  improved,
}: GpsTrajectoryChartProps) {
  const all = [...current, ...improved].map((point) => point.scenarioNetWorth);
  const min = Math.min(...all) * 0.96;
  const max = Math.max(...all) * 1.04;
  const currentPath = pathFor(current, min, max);
  const improvedPath = pathFor(improved, min, max);

  return (
    <div
      className="chart gps-chart"
      aria-label="Five-year Financial GPS current and improved trajectories"
    >
      <svg viewBox="0 0 500 190" role="img">
        {[30, 76, 122, 168].map((y) => (
          <line
            key={y}
            x1="18"
            y1={y}
            x2="482"
            y2={y}
            className="chart-grid"
          />
        ))}
        <path d={currentPath} className="chart-line baseline" />
        <path d={improvedPath} className="chart-line scenario" />
        {improved.map((point, index) => {
          const x = 18 + (index / Math.max(1, improved.length - 1)) * 464;
          return (
            <text
              key={point.year}
              x={x}
              y="187"
              textAnchor="middle"
              className="chart-label"
            >
              {point.year === 0 ? "Now" : `${point.year}Y`}
            </text>
          );
        })}
      </svg>
      <div className="chart-legend">
        <span><i className="legend-dot scenario-dot" />Improved route</span>
        <span><i className="legend-dot baseline-dot" />Current route</span>
      </div>
    </div>
  );
}
