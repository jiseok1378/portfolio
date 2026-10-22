import { expect, test } from 'vitest';
import SiteHero from './SiteHero.vue';
import { mountQ } from '@/test-utils';
import type { Profile } from '@/content';

const blank: Profile = {
  name: '', title: '백엔드 개발자 · Java / Spring Boot', headline: '',
  email: '', phone: '', github: '', careerStart: '2020-06',
};

test('blank name and headline: shows title, hides name and headline elements', () => {
  const w = mountQ(SiteHero, { props: { profile: blank, now: new Date('2026-10-22') } });
  expect(w.text()).toContain('백엔드 개발자 · Java / Spring Boot');
  expect(w.find('[data-test="name"]').exists()).toBe(false);
  expect(w.find('[data-test="headline"]').exists()).toBe(false);
});

test('shows career length computed from careerStart', () => {
  const w = mountQ(SiteHero, { props: { profile: blank, now: new Date('2026-10-22') } });
  expect(w.text()).toContain('경력 6년 4개월');
});

test('filled name and headline are shown', () => {
  const w = mountQ(SiteHero, {
    props: { profile: { ...blank, name: '홍길동', headline: '같은 실수를 두 번 하지 않게' }, now: new Date('2026-10-22') },
  });
  expect(w.find('[data-test="name"]').text()).toBe('홍길동');
  expect(w.find('[data-test="headline"]').text()).toBe('같은 실수를 두 번 하지 않게');
});

test('headline becomes the main heading and title moves below it', () => {
  const w = mountQ(SiteHero, {
    props: { profile: { ...blank, headline: '반복되는 일을 설정과 자동화로 바꾸는 백엔드 개발자' }, now: new Date('2026-10-22') },
  });
  expect(w.find('h1').text()).toBe('반복되는 일을 설정과 자동화로 바꾸는 백엔드 개발자');
  expect(w.find('.hero__title').text()).toBe('백엔드 개발자 · Java / Spring Boot');
});

test('without headline the title is the main heading', () => {
  const w = mountQ(SiteHero, { props: { profile: blank, now: new Date('2026-10-22') } });
  expect(w.find('h1').text()).toBe('백엔드 개발자 · Java / Spring Boot');
});
