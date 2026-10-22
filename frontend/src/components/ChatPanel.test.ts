import { beforeEach, expect, test } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { flushPromises } from '@vue/test-utils';
import ChatPanel from './ChatPanel.vue';
import { mountQ } from '@/test-utils';
import { useChatStore } from '@/stores/chat';
import type { ChatRequest } from '@/chat/chatClient';

let pinia: ReturnType<typeof createPinia>;
let seen: ChatRequest[];

beforeEach(() => {
  pinia = createPinia();
  setActivePinia(pinia);
  seen = [];
  useChatStore().stream = async (req, onEvent) => {
    seen.push(req);
    onEvent({ event: 'token', data: '답변입니다.' });
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
