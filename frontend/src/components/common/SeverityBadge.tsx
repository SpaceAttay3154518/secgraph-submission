import { getSeverityColor } from "../../utils/severityColors";

interface Props {
  severity: string;
}

export default function SeverityBadge({ severity }: Props) {
  const color = getSeverityColor(severity);
  return (
    <span style={{
      background: color + "22",
      color: color,
      padding: "2px 8px",
      borderRadius: 4,
      fontSize: 11,
      fontWeight: 600,
      textTransform: "uppercase",
    }}>
      {severity}
    </span>
  );
}
