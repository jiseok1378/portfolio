import { beforeEach, describe, expect, test, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useChatStore, HISTORY_LIMIT } from './chat';
import type { ChatRequest, StreamChat } from '@/chat/chatClient';
import type { SseEvent } from '@/chat/sseParser';

function fakeStream(events: SseEvent[], seen: ChatRequest[] = []): StreamChat {
  return async (req, onEvent) => {
    seen.push(req);
    events.forEach(onEvent);
  };
}

describe('chat store', () => {
  beforeEach(() => setActivePinia(createPinia()));

  test('appends tokens to the last assistant message and attaches sources', async () => {
    const store = useChatStore();
    store.stream = fakeStream([
      { event: 'token', data: JSON.stringify('이 개발자는 ') },
      { event: 'token', data: JSON.stringify('DLQ를 두었습니다.') },
      { event: 'sources', data: '[{"slug":"realtime-records","title":"실시간"}]' },
      { event: 'done', data: '{}' },
    ]);

    await store.ask('DLQ?');

    expect(store.messages).toEqual([
      { role: 'user', content: 'DLQ?' },
      { role: 'assistant', content: '이 개발자는 DLQ를 두었습니다.', sources: [{ slug: 'realtime-records', title: '실시간' }] },
    ]);
    expect(store.pending).toBe(false);
  });

  test('token keeps its leading space and newlines (sent as JSON string)', async () => {
    const store = useChatStore();
    store.stream = fakeStream([
      { event: 'token', data: JSON.stringify('실패했는지') },
      { event: 'token', data: JSON.stringify(' 남겨서\n- 목록') },
      { event: 'done', data: '{}' },
    ]);

    await store.ask('q');

    expect(store.messages[1]!.content).toBe('실패했는지 남겨서\n- 목록');
  });

  test('keeps partial answer and sets error message on error event', async () => {
    const store = useChatStore();
    store.stream = fakeStream([
      { event: 'token', data: JSON.stringify('부분') },
      { event: 'error', data: '{"code":"DAILY_LIMIT","message":"오늘 질문 한도가 끝났습니다. 내일 다시 질문해 주세요."}' },
    ]);

    await store.ask('q');

    expect(store.messages[1]).toEqual({
      role: 'assistant', content: '부분', error: '오늘 질문 한도가 끝났습니다. 내일 다시 질문해 주세요.',
    });
  });

  test('blocks a second ask while pending', async () => {
    const store = useChatStore();
    let release!: () => void;
    const calls: ChatRequest[] = [];
    store.stream = (req) => {
      calls.push(req);
      return new Promise<void>((r) => (release = r));
    };

    const first = store.ask('하나');
    await store.ask('둘');
    release();
    await first;

    expect(calls).toHaveLength(1);
  });

  test('sends at most the last HISTORY_LIMIT messages as history, excluding errored answers', async () => {
    const store = useChatStore();
    const seen: ChatRequest[] = [];
    store.stream = fakeStream([{ event: 'token', data: JSON.stringify('a') }, { event: 'done', data: '{}' }], seen);
    for (let i = 0; i < 5; i++) {
      await store.ask(`q${i}`);
    }
    const last = seen[seen.length - 1]!;
    expect(last.history).toHaveLength(HISTORY_LIMIT);
    expect(last.history[last.history.length - 1]).toEqual({ role: 'assistant', content: 'a' });
    expect(last.message).toBe('q4');
  });

  test('passes caseSlug when asked from a case page', async () => {
    const store = useChatStore();
    const seen: ChatRequest[] = [];
    store.stream = fakeStream([{ event: 'done', data: '{}' }], seen);

    await store.ask('이 사례?', 'parallel-testing');

    expect(seen[0]!.caseSlug).toBe('parallel-testing');
  });

  test('network failure sets UNAVAILABLE message', async () => {
    const store = useChatStore();
    store.stream = vi.fn().mockRejectedValue(new TypeError('Failed to fetch'));

    await store.ask('q');

    expect(store.messages[1]!.error).toBe('챗봇을 잠시 사용할 수 없습니다. 사례 글은 그대로 보실 수 있습니다.');
    expect(store.pending).toBe(false);
  });

  test('blank question is ignored', async () => {
    const store = useChatStore();
    store.stream = vi.fn();
    await store.ask('   ');
    expect(store.messages).toHaveLength(0);
    expect(store.stream).not.toHaveBeenCalled();
  });
});
