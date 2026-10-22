import { expect, test } from 'vitest';
import { layoutFlow } from './layout';
import { FLOWS } from './flows';

test('main steps form one chain connected in order', () => {
  const { nodes, edges } = layoutFlow(FLOWS.adcard, 'wide');
  const main = nodes.filter((n) => n.type === 'step');
  expect(main.map((n) => n.data!.label)).toEqual(['템플릿', '페이지', '행', '열', '구성 요소']);
  const chain = edges.filter((e) => e.data?.kind === 'main');
  expect(chain.map((e) => `${e.source}>${e.target}`)).toEqual(['s0>s1', 's1>s2', 's2>s3', 's3>s4']);
});

test('branches hang off their step with a dashed edge', () => {
  const { nodes, edges } = layoutFlow(FLOWS.realtime, 'wide');
  const branch = nodes.find((n) => n.type === 'branch')!;
  expect(branch.data!.label).toContain('DLQ');
  const edge = edges.find((e) => e.target === branch.id)!;
  expect(edge.data?.kind).toBe('branch');
  expect(edge.style).toMatchObject({ strokeDasharray: '6 4' });
});

test('wide layout puts branches to the right, narrow layout puts them below', () => {
  const wide = layoutFlow(FLOWS.adcard, 'wide');
  const narrow = layoutFlow(FLOWS.adcard, 'narrow');
  const wb = wide.nodes.find((n) => n.type === 'branch')!;
  const nb = narrow.nodes.find((n) => n.type === 'branch')!;
  const ws = wide.nodes.find((n) => n.id === wb.data!.parent)!;
  const ns = narrow.nodes.find((n) => n.id === nb.data!.parent)!;
  expect(wb.position.x).toBeGreaterThan(ws.position.x);
  expect(wb.position.y).toBe(ws.position.y);
  expect(nb.position.y).toBeGreaterThan(ns.position.y);
});

test('narrow layout never places a node beyond 320px wide', () => {
  for (const flow of Object.values(FLOWS)) {
    const { nodes } = layoutFlow(flow, 'narrow');
    expect(Math.max(...nodes.map((n) => n.position.x + Number(n.width ?? 0)))).toBeLessThanOrEqual(320);
  }
});

test('narrow layout routes the main edge down the left edge, beside the branch', () => {
  const { edges } = layoutFlow(FLOWS.adcard, 'narrow');
  const around = edges.find((e) => e.id === 's0-s1')!;
  expect(around.sourceHandle).toBe('bottom-left');
  expect(around.targetHandle).toBe('top-left');
  const straight = edges.find((e) => e.id === 's1-s2')!;
  expect(straight.sourceHandle).toBe('bottom');
});
