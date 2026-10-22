export const TOUR_KEY = 'portfolio.tour.v1';

/** 이 브라우저에서 둘러보기를 이미 봤는지. 저장소를 못 쓰면(시크릿 창 등) 본 것으로 쳐서 매번 뜨지 않게 한다. */
export function hasSeenTour(): boolean {
  try {
    return localStorage.getItem(TOUR_KEY) === '1';
  } catch {
    return true;
  }
}

export function markTourSeen(): void {
  try {
    localStorage.setItem(TOUR_KEY, '1');
  } catch {
    // 저장소를 못 쓰면 기록 없이 넘어간다
  }
}
