import { expect, test } from 'vitest';
import CareerTimeline from './CareerTimeline.vue';
import { mountQ } from '@/test-utils';
import { career } from '@/content';

test('each job shows period beside company, role and stack', () => {
  const w = mountQ(CareerTimeline, { props: { career: career() } });
  const first = w.find('.career__job');
  expect(first.find('.career__period').text()).toBe('2023.01 ~ 현재');
  expect(first.find('h3').text()).toContain('아이티센 클로잇');
  expect(first.findAll('.career__stack li').length).toBeGreaterThan(0);
});

test('stories are native disclosure rows, collapsed by default', () => {
  const w = mountQ(CareerTimeline, { props: { career: career() } });
  const rows = w.findAll('details.career__story');
  expect(rows.length).toBeGreaterThan(5);
  expect(rows[0]!.find('summary').text()).toContain('국제 스포츠 대회 운영 SaaS');
  expect(rows[0]!.attributes('open')).toBeUndefined();
});

test('story links are rendered inside the row', () => {
  const w = mountQ(CareerTimeline, { props: { career: career() } });
  expect(w.find('details.career__story a[href*="boannews.com"]').exists()).toBe(true);
});
