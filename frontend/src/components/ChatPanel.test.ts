import { beforeEach, expect, test } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { flushPromises } from '@vue/test-utils';
import ChatPanel from './ChatPanel.vue';
import { mountQ } from '@/test-utils';
import { useChatStore } from '@/stores/chat';
import type { ChatRequest } from '@/chat/chatClient';

let pinia: ReturnType<typeof createPinia>;
let seen: ChatRequest[];
let answer = '답변입니다.';

beforeEach(() => {
  pinia = createPinia();
  setActivePinia(pinia);
  seen = [];
  answer = '답변입니다.';
  useChatStore().stream = async (req, onEvent) => {
    seen.push(req);
    onEvent({ event: 'token', data: JSON.stringify(answer) });
    onEvent({ event: 'sources', data: '[{"slug":"parallel-testing","title":"병렬 처리"}]' });
    onEvent({ event: 'done', data: '{}' });
  };
});

const mountPanel = (props = {}) => mountQ(ChatPanel, { props, global: { plugins: [pinia] } });

test('shows the data notice', () => {
  expect(mountPanel().text()).toContain('질문 내용은 AI 서비스 제공사(Google)에 전달됩니다.');
});

test('clicking a suggested question asks it and renders answer with source link', async () => {
  const w = mountPanel();
  await w.find('[data-test="suggestion"]').trigger('click');
  await flushPromises();

  expect(seen[0]!.message).toBe('Kafka DLQ는 왜 그렇게 설계했나요?');
  expect(w.text()).toContain('답변입니다.');
  expect(w.find('a[href="/cases/parallel-testing"]').text()).toContain('병렬 처리');
});

test('send button is disabled over 500 characters', async () => {
  const w = mountPanel();
  await w.find('textarea').setValue('가'.repeat(501));
  expect(w.find('[data-test="send"]').attributes('disabled')).toBeDefined();
  await w.find('textarea').setValue('가'.repeat(500));
  expect(w.find('[data-test="send"]').attributes('disabled')).toBeUndefined();
});

test('passes caseSlug from props', async () => {
  const w = mountPanel({ caseSlug: 'auth-db-docker' });
  await w.find('textarea').setValue('이 사례는?');
  await w.find('[data-test="send"]').trigger('click');
  await flushPromises();
  expect(seen[0]!.caseSlug).toBe('auth-db-docker');
});

test('assistant answer is rendered as markdown', async () => {
  answer = '**굵게** 설명\n\n* 첫째\n* 둘째';
  const w = mountPanel();
  await w.find('[data-test="suggestion"]').trigger('click');
  await flushPromises();
  expect(w.find('.chat__msg--assistant strong').text()).toBe('굵게');
  expect(w.findAll('.chat__text--md li')).toHaveLength(2);
});

test('markdown output is sanitized', async () => {
  answer = '안녕<img src=x onerror="alert(1)"><script>alert(2)</script>';
  const w = mountPanel();
  await w.find('[data-test="suggestion"]').trigger('click');
  await flushPromises();
  const html = w.find('.chat__msg--assistant').html();
  expect(html).not.toContain('onerror');
  expect(html).not.toContain('<script');
});

test('user message stays plain text', async () => {
  const w = mountPanel();
  await w.find('textarea').setValue('**별표** 그대로');
  await w.find('[data-test="send"]').trigger('click');
  await flushPromises();
  expect(w.find('.chat__msg--user').text()).toContain('**별표** 그대로');
});
