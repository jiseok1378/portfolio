import { beforeEach, expect, test } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { flushPromises } from '@vue/test-utils';
import IndexPage from './IndexPage.vue';
import { mountQ } from '@/test-utils';
import { useChatStore } from '@/stores/chat';

let pinia: ReturnType<typeof createPinia>;

beforeEach(() => {
  pinia = createPinia();
  setActivePinia(pinia);
  useChatStore().stream = async (_req, onEvent) => {
    onEvent({ event: 'token', data: '답' });
    onEvent({ event: 'done', data: '{}' });
  };
});

test('hero is two columns until the first question', () => {
  const w = mountQ(IndexPage, { global: { plugins: [pinia] } });
  expect(w.find('.index-hero').classes()).not.toContain('index-hero--chatting');
});

test('after the first question the chat drops below and spans full width', async () => {
  const w = mountQ(IndexPage, { global: { plugins: [pinia] } });
  await w.find('[data-test="suggestion"]').trigger('click');
  await flushPromises();
  expect(w.find('.index-hero').classes()).toContain('index-hero--chatting');
});

test('chat state survives remount (same tab conversation)', async () => {
  await useChatStore().ask('q');
  const w = mountQ(IndexPage, { global: { plugins: [pinia] } });
  expect(w.find('.index-hero').classes()).toContain('index-hero--chatting');
});

test('no duplicate case-order sentence under the cases title', () => {
  const w = mountQ(IndexPage, { global: { plugins: [pinia] } });
  expect(w.find('.section-sub').exists()).toBe(false);
});
