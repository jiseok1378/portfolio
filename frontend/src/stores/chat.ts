import { defineStore } from 'pinia';
import { ref } from 'vue';
import { streamChat, type ChatTurn, type StreamChat } from '@/chat/chatClient';
import type { SseEvent } from '@/chat/sseParser';

export const HISTORY_LIMIT = 6;
const UNAVAILABLE = '챗봇을 잠시 사용할 수 없습니다. 사례 글은 그대로 보실 수 있습니다.';

export interface ChatSource {
  slug: string;
  title: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  sources?: ChatSource[];
  error?: string;
}

/** 대화는 이 탭의 메모리에만 둔다(새로고침하면 사라짐). 서버로는 최근 대화만 보낸다. */
export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([]);
  const pending = ref(false);
  const stream = ref<StreamChat>(streamChat);
  let controller: AbortController | null = null;

  function history(): ChatTurn[] {
    return messages.value
      .filter((m) => !m.error && m.content !== '')
      .map((m) => ({ role: m.role, content: m.content }))
      .slice(-HISTORY_LIMIT);
  }

  function apply(answer: ChatMessage, e: SseEvent) {
    if (e.event === 'token') {
      answer.content += e.data;
    } else if (e.event === 'sources') {
      answer.sources = JSON.parse(e.data) as ChatSource[];
    } else if (e.event === 'error') {
      answer.error = (JSON.parse(e.data) as { message?: string }).message ?? UNAVAILABLE;
    }
  }

  async function ask(text: string, caseSlug?: string) {
    const question = text.trim();
    if (question === '' || pending.value) {
      return;
    }
    const req = { message: question, history: history(), ...(caseSlug ? { caseSlug } : {}) };
    messages.value.push({ role: 'user', content: question });
    messages.value.push({ role: 'assistant', content: '' });
    // 반응성 프록시를 통해 고쳐야 화면이 갱신된다
    const answer = messages.value[messages.value.length - 1]!;
    pending.value = true;
    controller = new AbortController();
    try {
      await stream.value(req, (e) => apply(answer, e), controller.signal);
    } catch (err) {
      if (!(err instanceof DOMException && err.name === 'AbortError')) {
        answer.error = UNAVAILABLE;
      }
    } finally {
      pending.value = false;
      controller = null;
    }
  }

  function cancel() {
    controller?.abort();
  }

  return { messages, pending, stream, ask, cancel };
});
