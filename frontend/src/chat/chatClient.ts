import { createSseParser, type SseEvent } from './sseParser';

export interface ChatTurn {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  message: string;
  history: ChatTurn[];
  caseSlug?: string;
}

export type StreamChat = (req: ChatRequest, onEvent: (e: SseEvent) => void, signal?: AbortSignal) => Promise<void>;

/** POST 로 질문을 보내고 SSE 응답을 흘려 받는다 (EventSource 는 POST 를 못 쓴다). */
export const streamChat: StreamChat = async (req, onEvent, signal) => {
  const res = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'text/event-stream' },
    body: JSON.stringify(req),
    ...(signal ? { signal } : {}),
  });
  if (!res.ok || !res.body) {
    throw new Error(`chat request failed: ${res.status}`);
  }
  const parser = createSseParser(onEvent);
  const reader = res.body.getReader();
  for (;;) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    parser.push(value);
  }
  parser.end();
};
