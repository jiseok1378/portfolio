export interface TourStep {
  element: string;
  title: string;
  description: string;
}

/** 처음 들어온 면접관에게 보여줄 둘러보기 순서 */
export const TOUR_STEPS: TourStep[] = [
  {
    element: '[data-tour="chat"]',
    title: '먼저 물어보세요',
    description: '추천 질문을 누르거나 직접 입력하면, AI가 이 사이트의 사례 글을 근거로 답하고 근거 사례 링크를 붙여 줍니다.',
  },
  {
    // 목록 전체는 화면보다 커서 말풍선이 밖으로 밀려나므로 첫 카드를 짚는다
    element: '[data-tour="cases"] > :first-child',
    title: '대표 사례',
    description: '모든 사례는 문제 → 고민 → 해결 → 결과 순서로 썼습니다. 카드를 누르면 구성도와 함께 자세히 볼 수 있고, 아래에 사례가 더 있습니다.',
  },
  {
    element: '[data-tour="more-cases"]',
    title: '그 밖의 사례',
    description: '대표 사례 말고도 보고서 자동화, 인증·DB 설계 사례가 있습니다.',
  },
  {
    element: '[data-tour="career"] .career__job',
    title: '경력',
    description: '회사별로 한 일을 펼쳐 볼 수 있습니다. 학력과 자격, 교육도 맨 아래에 있습니다.',
  },
];
