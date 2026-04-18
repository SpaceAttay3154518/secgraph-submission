import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ComponentType,
} from "react";
import ForceGraph2D from "react-force-graph-2d";
import type { GraphData, GraphLink, GraphNode } from "../../types/graph";
import type { LinkObject, NodeObject } from "react-force-graph-2d";
import { getNodeColor, getNodeSize } from "../../utils/severityColors";

interface Props {
  data: GraphData;
  onNodeClick?: (node: GraphNode) => void;
  highlightPath?: string[];
  width?: number;
  height?: number;
}

type ForceNode = NodeObject<GraphNode>;
type ForceLink = LinkObject<GraphNode, GraphLink>;
type ForceGraph2DProps = {
  graphData: GraphData;
  width: number;
  height: number;
  backgroundColor: string;
  nodeCanvasObject: (
    node: ForceNode,
    ctx: CanvasRenderingContext2D,
    globalScale: number
  ) => void;
  linkColor: (link: ForceLink) => string;
  linkWidth: (link: ForceLink) => number;
  linkDirectionalArrowLength: number;
  linkDirectionalArrowRelPos: number;
  onNodeClick: (node: ForceNode) => void;
  cooldownTicks: number;
  nodeId: string;
};

const ResponsiveForceGraph = ForceGraph2D as unknown as ComponentType<ForceGraph2DProps>;

export default function GraphVisualization({ data, onNodeClick, highlightPath, width, height }: Props) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [containerWidth, setContainerWidth] = useState(0);
  const highlightSet = useMemo(() => new Set(highlightPath || []), [highlightPath]);
  const graphWidth = width ?? containerWidth;
  const graphHeight = height || 600;

  useEffect(() => {
    if (width) return;

    const element = containerRef.current;
    if (!element) return;

    const updateWidth = () => {
      setContainerWidth(Math.max(320, Math.floor(element.clientWidth)));
    };

    updateWidth();
    const observer = new ResizeObserver(updateWidth);
    observer.observe(element);

    return () => observer.disconnect();
  }, [width]);

  const nodeCanvasObject = useCallback(
    (node: ForceNode, ctx: CanvasRenderingContext2D, globalScale: number) => {
      const label = node.label || String(node.id || "");
      const size = getNodeSize(node.type, node.score);
      const color = getNodeColor(node.type);
      const isHighlighted = highlightSet.has(String(node.id));
      const x = node.x ?? 0;
      const y = node.y ?? 0;

      ctx.beginPath();
      ctx.arc(x, y, size, 0, 2 * Math.PI);
      ctx.fillStyle = isHighlighted ? "#ff0000" : color;
      ctx.fill();

      if (isHighlighted) {
        ctx.strokeStyle = "#fbbf24";
        ctx.lineWidth = 2;
        ctx.stroke();
      }

      const fontSize = Math.max(10 / globalScale, 2);
      ctx.font = `${fontSize}px Sans-Serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = "#e2e8f0";
      ctx.fillText(label, x, y + size + fontSize);
    },
    [highlightSet]
  );

  const linkColor = useCallback(
    (link: ForceLink) => {
      const sourceId = getLinkNodeId(link.source);
      const targetId = getLinkNodeId(link.target);
      if (highlightSet.has(sourceId) && highlightSet.has(targetId)) {
        return "#ef4444";
      }
      return "#475569";
    },
    [highlightSet]
  );

  const linkWidth = useCallback(
    (link: ForceLink) => {
      const sourceId = getLinkNodeId(link.source);
      const targetId = getLinkNodeId(link.target);
      if (highlightSet.has(sourceId) && highlightSet.has(targetId)) {
        return 3;
      }
      return 1;
    },
    [highlightSet]
  );

  return (
    <div ref={containerRef} className="graph-canvas" style={{ height: graphHeight }}>
      {graphWidth > 0 && (
        <ResponsiveForceGraph
          graphData={data}
          width={graphWidth}
          height={graphHeight}
          backgroundColor="#0f1115"
          nodeCanvasObject={nodeCanvasObject}
          linkColor={linkColor}
          linkWidth={linkWidth}
          linkDirectionalArrowLength={4}
          linkDirectionalArrowRelPos={1}
          onNodeClick={(node) => onNodeClick?.(node as GraphNode)}
          cooldownTicks={100}
          nodeId="id"
        />
      )}
    </div>
  );
}

function getLinkNodeId(node: string | number | ForceNode | undefined) {
  if (typeof node === "object" && node !== null) {
    return String(node.id || "");
  }
  return String(node || "");
}
