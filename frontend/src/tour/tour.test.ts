import { afterEach, describe, expect, test, vi } from 'vitest';
import { TOUR_STEPS } from './steps';
import { hasSeenTour, markTourSeen, TOUR_KEY } from './firstVisit';

describe('tour steps', () => {
  test('cover chat, cases, more cases and career in that order', () => {
    expect(TOUR_STEPS.map((s) => s.element)).toEqual([
      '[data-tour="chat"]',
      '[data-tour="cases"]',
      '[data-tour="more-cases"]',
      '[data-tour="career"]',
    ]);
  });

  test('every step has a Korean title and description', () => {
    for (const s of TOUR_STEPS) {
      expect(s.title).toMatch(/[가-힣]/);
      expect(s.description).toMatch(/[가-힣]/);
    }
  });
});

describe('first visit flag', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  test('not seen until marked', () => {
    expect(hasSeenTour()).toBe(false);
    markTourSeen();
    expect(hasSeenTour()).toBe(true);
    expect(localStorage.getItem(TOUR_KEY)).toBe('1');
  });

  test('blocked storage never throws and counts as seen (no surprise tour loop)', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    expect(() => markTourSeen()).not.toThrow();
    expect(hasSeenTour()).toBe(true);
  });
});
