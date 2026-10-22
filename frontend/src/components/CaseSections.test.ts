import { expect, test } from 'vitest';
import CaseSections from './CaseSections.vue';
import { mountQ } from '@/test-utils';
import type { CaseSectionsData } from '@/content';

const full: CaseSectionsData = {
  problem: '문제 본문',
  thinking: ['고민1'],
  solution: ['해결1'],
  result: ['결과1'],
  lessons: ['배움1'],
  interviewQuestions: [{ q: '질문?', a: '답.' }],
};

test('renders sections in fixed order', () => {
  const w = mountQ(CaseSections, { props: { sections: full } });
  expect(w.findAll('h2').map((h) => h.text())).toEqual([
    '문제', '고민', '해결', '결과', '남은 위험 · 배운 점', '면접 예상 질문',
  ]);
});

test('skips empty sections', () => {
  const w = mountQ(CaseSections, { props: { sections: { ...full, lessons: [], interviewQuestions: [] } } });
  expect(w.findAll('h2').map((h) => h.text())).toEqual(['문제', '고민', '해결', '결과']);
});
