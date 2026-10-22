import { driver } from 'driver.js';
import 'driver.js/dist/driver.css';
import { TOUR_STEPS } from './steps';
import { markTourSeen } from './firstVisit';

/** 화면에 있는 단계만 골라 둘러보기를 띄운다. 끝내거나 닫으면 다시 자동으로 뜨지 않게 기록한다. */
export function startTour(): void {
  const steps = TOUR_STEPS.filter((s) => document.querySelector(s.element));
  if (steps.length === 0) {
    return;
  }
  const reduce = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false;
  const tour = driver({
    animate: !reduce,
    showProgress: true,
    progressText: '{{current}} / {{total}}',
    nextBtnText: '다음',
    prevBtnText: '이전',
    doneBtnText: '시작하기',
    allowClose: true,
    stagePadding: 8,
    stageRadius: 16,
    popoverClass: 'portfolio-tour',
    steps: steps.map((s) => ({
      element: s.element,
      popover: { title: s.title, description: s.description, side: 'bottom', align: 'start' },
    })),
    onDestroyed: () => markTourSeen(),
  });
  tour.drive();
}
