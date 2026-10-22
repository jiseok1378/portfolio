import { expect, test } from 'vitest';
import FlowDiagram from './FlowDiagram.vue';
import { FLOWS } from './flows';
import { mountQ } from '@/test-utils';

test('every diagram kind used by content has a flow', () => {
  expect(Object.keys(FLOWS).sort()).toEqual(['adcard', 'auth', 'chatbot', 'realtime']);
});

test('ad card flow shows the template hierarchy', () => {
  const w = mountQ(FlowDiagram, { props: { kind: 'adcard' } });
  expect(w.findAll('.flow__node strong').map((n) => n.text())).toEqual([
    '템플릿', '페이지', '행', '열', '구성 요소',
  ]);
});

test('renders steps in order with branch notes', () => {
  const w = mountQ(FlowDiagram, { props: { kind: 'realtime' } });
  const nodes = w.findAll('.flow__node strong').map((n) => n.text());
  expect(nodes[0]).toBe('경기장 계측 장비');
  expect(nodes).toContain('Redis Pub/Sub');
  expect(w.find('.flow__branch').text()).toContain('DLQ');
});
