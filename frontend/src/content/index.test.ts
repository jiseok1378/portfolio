import { describe, expect, test } from 'vitest';
import { career, cases, findCase, profile } from './index';

describe('content', () => {
  test('cases follow index.json order', () => {
    expect(cases().map((c) => c.slug)).toEqual([
      'realtime-records',
      'llm-tools-chatbot',
      'ad-card-designer',
      'custom-fields',
      'report-versioning',
      'parallel-testing',
      'auth-db-docker',
      'ai-harness',
    ]);
  });

  test('findCase unknown slug is undefined', () => {
    expect(findCase('nope')).toBeUndefined();
  });

  test('findCase known slug returns the case', () => {
    expect(findCase('parallel-testing')?.title).toContain('병렬');
  });

  test('profile blank fields are empty strings', () => {
    expect(profile().name).toBe('');
    expect(profile().email).toBe('');
  });

  test('career has jobs, side projects, education and certifications', () => {
    expect(career().jobs.length).toBeGreaterThan(0);
    expect(career().sideProjects.length).toBeGreaterThan(0);
    expect(career().education.length).toBeGreaterThan(0);
    expect(career().certifications.length).toBeGreaterThan(0);
  });
});
