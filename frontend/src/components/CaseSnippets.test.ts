import { expect, test } from 'vitest';
import CaseSnippets from './CaseSnippets.vue';
import { mountQ } from '@/test-utils';

test('renders each snippet with title, language and code as plain text', () => {
  const w = mountQ(CaseSnippets, {
    props: {
      snippets: [
        { title: '이름으로 코드 확정', lang: 'java', code: 'if (a < b) {\n  return "<b>";\n}' },
        { title: '도구 스키마', lang: 'json', code: '{ "name": "x" }' },
      ],
    },
  });
  expect(w.find('h2').text()).toBe('핵심 코드');
  expect(w.findAll('.snippet__title').map((c) => c.text())).toEqual(['이름으로 코드 확정', '도구 스키마']);
  expect(w.findAll('.snippet__lang').map((c) => c.text())).toEqual(['java', 'json']);
  const code = w.find('pre code');
  expect(code.text()).toBe('if (a < b) {\n  return "<b>";\n}');
  expect(code.find('b').exists()).toBe(false);
});
