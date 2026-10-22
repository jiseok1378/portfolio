// 저장소 루트 content/ 의 JSON 을 빌드 때 함께 묶는다 (챗봇 서버와 같은 원본)
import caseOrder from '@content/cases/index.json';
import careerJson from '@content/career.json';
import profileJson from '@content/profile.json';

export interface Link {
  label: string;
  url: string;
}

export interface CaseSectionsData {
  problem: string;
  thinking: string[];
  solution: string[];
  result: string[];
  lessons: string[];
  interviewQuestions: { q: string; a: string }[];
}

export type DiagramKind = 'realtime' | 'chatbot' | 'auth' | 'adcard' | 'fields' | 'manual' | null;

export interface CaseContent {
  slug: string;
  title: string;
  period: string;
  context: string;
  summary: string;
  metric?: string;
  tags: string[];
  unfinished: boolean;
  diagram: DiagramKind;
  sections: CaseSectionsData;
  links: Link[];
}

export interface Story {
  title: string;
  body: string;
  links: Link[];
}

export interface CareerContent {
  jobs: { period: string; company: string; role: string; stack: string[]; stories: Story[] }[];
  sideProjects: { name: string; period?: string; body: string; links: Link[] }[];
  education: { period: string; name: string }[];
  certifications: { date: string; name: string }[];
}

export interface Profile {
  name: string;
  title: string;
  headline: string;
  email: string;
  phone: string;
  github: string;
  careerStart: string;
}

const caseFiles = import.meta.glob<CaseContent>('@content/cases/*.json', { eager: true, import: 'default' });

function load(slug: string): CaseContent {
  const found = Object.entries(caseFiles).find(([path]) => path.endsWith(`/${slug}.json`));
  if (!found) {
    throw new Error(`사례 파일이 없습니다: ${slug}`);
  }
  return found[1];
}

const order = caseOrder as { featured: string[]; others: string[] };
const featured = order.featured.map(load);
const others = order.others.map(load);
const allCases: CaseContent[] = [...featured, ...others];

/** 대표 사례(첫 화면 카드) + 그 밖의 사례 */
export function cases(): CaseContent[] {
  return allCases;
}

export function featuredCases(): CaseContent[] {
  return featured;
}

export function otherCases(): CaseContent[] {
  return others;
}

export function findCase(slug: string): CaseContent | undefined {
  return allCases.find((c) => c.slug === slug);
}

export function career(): CareerContent {
  return careerJson as CareerContent;
}

export function profile(): Profile {
  return profileJson as Profile;
}

/** careerStart(YYYY-MM)부터 now 까지의 경력. 예: "6년 4개월" */
export function careerLength(careerStart: string, now: Date): string {
  const [year = now.getFullYear(), month = now.getMonth() + 1] = careerStart.split('-').map(Number);
  const months = (now.getFullYear() - year) * 12 + (now.getMonth() + 1 - month);
  const years = Math.floor(months / 12);
  const rest = months % 12;
  return rest === 0 ? `${years}년` : `${years}년 ${rest}개월`;
}
