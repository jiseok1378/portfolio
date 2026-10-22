import { beforeEach, expect, test, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import IndexPage from './IndexPage.vue';
import { mountQ } from '@/test-utils';
import { TOUR_STEPS } from '@/tour/steps';

const startTour = vi.fn();
vi.mock('@/tour/startTour', () => ({ startTour: (...a: unknown[]) => startTour(...a) }));

let pinia: ReturnType<typeof createPinia>;
beforeEach(() => {
  pinia = createPinia();
  setActivePinia(pinia);
  startTour.mockClear();
  localStorage.clear();
  vi.useFakeTimers();
});

test('every tour target exists on the index page', () => {
  const w = mountQ(IndexPage, { global: { plugins: [pinia] } });
  for (const s of TOUR_STEPS) {
    expect(w.find(s.element).exists()).toBe(true);
  }
});

test('first visit starts the tour after a short delay', () => {
  mountQ(IndexPage, { global: { plugins: [pinia] } });
  expect(startTour).not.toHaveBeenCalled();
  vi.advanceTimersByTime(1000);
  expect(startTour).toHaveBeenCalledTimes(1);
});

test('returning visitor does not get the tour', () => {
  localStorage.setItem('portfolio.tour.v1', '1');
  mountQ(IndexPage, { global: { plugins: [pinia] } });
  vi.advanceTimersByTime(1000);
  expect(startTour).not.toHaveBeenCalled();
});
