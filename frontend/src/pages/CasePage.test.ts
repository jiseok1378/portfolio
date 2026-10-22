import { expect, test } from 'vitest';
import CasePage from './CasePage.vue';
import { mountQ } from '@/test-utils';

const stubs = { ChatDialogButton: true };

test('unknown slug shows not-found message with link home', () => {
  const w = mountQ(CasePage, { props: { slug: 'nope' }, global: { stubs } });
  expect(w.text()).toContain('사례를 찾을 수 없습니다');
  expect(w.find('a[href="/"]').exists()).toBe(true);
});

test('known slug shows title and sections', () => {
  const w = mountQ(CasePage, { props: { slug: 'ad-card-designer' }, global: { stubs } });
  expect(w.find('h1').text()).toContain('AD 카드');
  expect(w.text()).toContain('문제');
  expect(w.find('.flow').exists()).toBe(true);
});

test('unfinished side project is labelled', () => {
  const w = mountQ(CasePage, { props: { slug: 'auth-db-docker' }, global: { stubs } });
  expect(w.text()).toContain('미완성 사이드 프로젝트');
});
