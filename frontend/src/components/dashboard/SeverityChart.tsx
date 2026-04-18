import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from "recharts";
import { SEVERITY_COLORS } from "../../utils/severityColors";

interface Props {
  data: Record<string, number>;
}

export default function SeverityChart({ data }: Props) {
  const chartData = Object.entries(data)
    .filter(([, count]) => count > 0)
    .map(([severity, count]) => ({ name: severity, value: count }));

  if (chartData.length === 0) {
    return <div style={{ color: "#64748b", textAlign: "center", padding: 40 }}>No CVE data</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={250}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          innerRadius={50}
          outerRadius={90}
          dataKey="value"
          label={({ name, value }) => `${name}: ${value}`}
        >
          {chartData.map((entry) => (
            <Cell
              key={entry.name}
              fill={SEVERITY_COLORS[entry.name] || "#94a3b8"}
            />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{ background: "#1e293b", border: "1px solid #334155", borderRadius: 4 }}
          itemStyle={{ color: "#e2e8f0" }}
        />
        <Legend
          wrapperStyle={{ color: "#94a3b8", fontSize: 12 }}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}
