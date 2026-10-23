import { describe, expect, test } from 'vitest';
import { career, cases, featuredCases, findCase, otherCases, profile } from './index';

describe('content', () => {
  test('featured cases follow index.json order', () => {
    expect(featuredCases().map((c) => c.slug)).toEqual([
      'realtime-records',
      'pool-query-tuning',
      'ad-card-designer',
      'llm-tools-chatbot',
      'custom-fields',
      'ai-harness',
    ]);
  });

  test('other cases are listed separately', () => {
    expect(otherCases().map((c) => c.slug)).toEqual(['report-versioning', 'auth-db-docker']);
  });

  test('cases() returns featured then others', () => {
    expect(cases()).toHaveLength(8);
  });

  test('findCase unknown slug is undefined', () => {
    expect(findCase('nope')).toBeUndefined();
  });

  test('findCase known slug returns the case', () => {
    expect(findCase('ad-card-designer')?.title).toContain('AD 카드');
  });

  test('profile blank fields are empty strings', () => {
    expect(profile().name).toBe('');
    expect(profile().email).toBe('');
  });

  test('parallel processing story lives in career with the article link', () => {
    const stories = career().jobs.flatMap((j) => j.stories);
    const story = stories.find((x) => x.title.includes('COYOTE'));
    expect(story?.links[0]?.url).toContain('boannews.com');
  });

  test('career has jobs, side projects, education and certifications', () => {
    expect(career().jobs.length).toBeGreaterThan(0);
    expect(career().sideProjects.length).toBeGreaterThan(0);
    expect(career().education.length).toBeGreaterThan(0);
    expect(career().certifications.length).toBeGreaterThan(0);
  });
});
