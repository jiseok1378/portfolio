// 사례 상세의 흐름 그림. 회사 내부 이름 대신 일반적인 말로 쓴다.
export interface FlowStep {
  label: string;
  detail?: string;
  key?: boolean;
  branch?: { label: string; detail: string };
}

export interface Flow {
  caption: string;
  steps: FlowStep[];
}

export const FLOWS: Record<'realtime' | 'chatbot' | 'auth' | 'adcard' | 'fields', Flow> = {
  fields: {
    caption: '설정 한 번이 여러 서비스 화면에 퍼지기까지',
    steps: [
      {
        label: '공통 설정 서비스',
        detail: '기본 필드 · 커스텀 필드를 한 곳에서 관리',
        key: true,
        branch: { label: '적용 범위', detail: '모든 대회에 적용하거나 특정 대회만 덮어쓰기' },
      },
      { label: '서비스 간 API 호출', detail: '각 서비스가 메뉴별 설정을 읽음' },
      { label: '업무 화면', detail: '카드 발급 · 승인 · 온라인 등록이 컬럼과 입력란을 구성', key: true },
      { label: '검증', detail: '필수 여부 · 길이 · 여권과 사진 검증도 설정대로' },
    ],
  },
  adcard: {
    caption: '카드 한 장을 이루는 데이터 구조',
    steps: [
      {
        label: '템플릿',
        detail: '대회별 카드 디자인 · 크기',
        key: true,
        branch: { label: '부모 · 서브 · 분류별 템플릿', detail: '공통 디자인은 한 번만, 참가자 분류별 차이만 따로' },
      },
      { label: '페이지', detail: '앞면 · 뒷면, 배경 이미지, 여백' },
      { label: '행', detail: '순서 · 높이 · 스타일' },
      { label: '열', detail: '너비 · 스타일' },
      {
        label: '구성 요소',
        detail: '사진, 이름, 소속 엠블럼, 종목 등',
        key: true,
        branch: { label: '요소 목록은 별도 테이블', detail: '새 요소가 생겨도 구조를 바꾸지 않음' },
      },
    ],
  },
  realtime: {
    caption: '기록 한 건이 화면에 닿기까지',
    steps: [
      { label: '경기장 계측 장비', detail: '기록을 보냄' },
      { label: 'Kafka 원본 기록 토픽' },
      {
        label: '컨슈머',
        detail: '장비별 파서 · JSON 스키마 검사',
        branch: { label: '처리 실패 → DLQ', detail: '실패 이유를 나눠 토픽과 테이블에 남기고, 운영자가 다시 처리' },
      },
      { label: 'DB 저장', detail: '트랜잭션 커밋' },
      { label: 'Redis Pub/Sub', detail: '커밋된 뒤에만 알림', key: true },
      { label: '서버마다 SSE 전송', detail: '자기에게 연결된 브라우저에만', key: true },
      { label: '공개 결과 화면', detail: '새로고침 없이 갱신' },
    ],
  },
  chatbot: {
    caption: '질문 하나가 답이 되기까지',
    steps: [
      { label: '사용자 질문', detail: '"한국 양궁 메달 몇 개야?"' },
      { label: '모델', detail: '대상 이름만 뽑아 조회 도구 호출' },
      {
        label: '서버가 코드 확정',
        detail: '#번호 → 정확 일치 → 부분 일치',
        key: true,
        branch: { label: '후보가 여러 개', detail: '임의로 고르지 않고 번호 목록을 보여줘 사용자가 고름' },
      },
      { label: 'DB 조회', detail: '확정된 코드로만' },
      { label: '답변', detail: '내부 ID 없이 이름으로' },
    ],
  },
  auth: {
    caption: '요청 하나가 인가되기까지',
    steps: [
      { label: '로그인', detail: 'Access Token은 브라우저, Refresh Token은 Redis' },
      { label: '서명 검증', detail: '만료 · 위조 · 형식 오류를 구분' },
      { label: 'Redis 토큰 쌍 확인', detail: '로그아웃하면 즉시 막힘', key: true },
      { label: 'DB 사용자 상태 확인', detail: '탈퇴 여부 · 권한', key: true },
      { label: 'URL별 권한 검사' },
    ],
  },
};
