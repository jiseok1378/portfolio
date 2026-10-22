import { MarkerType, type Edge, type Node } from '@vue-flow/core';
import type { Flow } from './flows';

export type LayoutMode = 'wide' | 'narrow';

export interface FlowNodeData {
  label: string;
  detail?: string | undefined;
  key?: boolean | undefined;
  parent?: string | undefined;
}

const STEP_W = 300;
const BRANCH_W = 280;
const ROW = 96;

const BLUE = '#3182f6';
const GREY = '#b0b8c1';
const ORANGE = '#f2711c';

/**
 * 단계는 위에서 아래로 한 줄로 잇고, 분기는
 * 넓은 화면에서는 오른쪽, 좁은 화면(모바일)에서는 그 단계 바로 아래에 둔다.
 */
export function layoutFlow(flow: Flow, mode: LayoutMode): { nodes: Node<FlowNodeData>[]; edges: Edge[] } {
  const nodes: Node<FlowNodeData>[] = [];
  const edges: Edge[] = [];
  let y = 0;

  flow.steps.forEach((step, i) => {
    const id = `s${i}`;
    nodes.push({
      id,
      type: 'step',
      position: { x: 0, y },
      width: STEP_W,
      data: { label: step.label, detail: step.detail, key: step.key },
    });

    if (i > 0) {
      const prev = `s${i - 1}`;
      // 좁은 화면에서 앞 단계 아래에 분기 상자가 있으면, 연결선을 상자 왼쪽 끝(20px)으로 곧게 내려 분기 옆을 지나게 한다
      const around = mode === 'narrow' && Boolean(flow.steps[i - 1]?.branch);
      edges.push({
        id: `${prev}-${id}`,
        source: prev,
        target: id,
        sourceHandle: around ? 'bottom-left' : 'bottom',
        targetHandle: around ? 'top-left' : 'top',
        type: 'smoothstep',
        markerEnd: { type: MarkerType.ArrowClosed, color: step.key ? BLUE : GREY },
        style: { stroke: step.key ? BLUE : GREY, strokeWidth: 2 },
        data: { kind: 'main' },
      });
    }

    if (step.branch) {
      const bid = `b${i}`;
      const wide = mode === 'wide';
      nodes.push({
        id: bid,
        type: 'branch',
        position: wide ? { x: STEP_W + 60, y } : { x: 40, y: y + ROW },
        width: wide ? BRANCH_W : STEP_W - 40,
        data: { label: step.branch.label, detail: step.branch.detail, parent: id },
      });
      edges.push({
        id: `${id}-${bid}`,
        source: id,
        target: bid,
        sourceHandle: wide ? 'right' : 'bottom',
        targetHandle: wide ? 'left' : 'top',
        type: 'smoothstep',
        markerEnd: { type: MarkerType.ArrowClosed, color: ORANGE },
        style: { stroke: ORANGE, strokeWidth: 2, strokeDasharray: '6 4' },
        data: { kind: 'branch' },
      });
      if (!wide) {
        y += ROW + 24;
      }
    }

    y += ROW;
  });

  return { nodes, edges };
}
