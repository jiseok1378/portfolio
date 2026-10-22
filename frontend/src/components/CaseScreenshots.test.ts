import { expect, test } from 'vitest';
import CaseScreenshots from './CaseScreenshots.vue';
import { mountQ } from '@/test-utils';

test('renders each screenshot with caption as alt text', () => {
  const w = mountQ(CaseScreenshots, {
    props: { screenshots: [{ src: 'cases/demo/ambiguous.png', caption: '모호한 이름을 물으면 후보를 보여줍니다' }] },
  });
  expect(w.find('h2').text()).toBe('실제 화면');
  const img = w.find('img');
  expect(img.attributes('src')).toBe('/cases/demo/ambiguous.png');
  expect(img.attributes('alt')).toBe('모호한 이름을 물으면 후보를 보여줍니다');
  expect(img.attributes('loading')).toBe('lazy');
  expect(w.find('figcaption').text()).toBe('모호한 이름을 물으면 후보를 보여줍니다');
});
