export interface SseEvent {
  event: string;
  data: string;
}

/**
 * fetch 스트림의 바이트 조각을 SSE 이벤트로 바꾼다.
 * 조각이 이벤트 중간이나 한글 바이트 중간에서 잘려도 버퍼에 모았다가 완성된 이벤트만 넘긴다.
 */
export function createSseParser(onEvent: (e: SseEvent) => void) {
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  function emit(block: string) {
    let event = 'message';
    const data: string[] = [];
    for (const line of block.split(/\r?\n/)) {
      if (line === '' || line.startsWith(':')) {
        continue;
      }
      const colon = line.indexOf(':');
      const field = colon === -1 ? line : line.slice(0, colon);
      let value = colon === -1 ? '' : line.slice(colon + 1);
      if (value.startsWith(' ')) {
        value = value.slice(1);
      }
      if (field === 'event') {
        event = value;
      } else if (field === 'data') {
        data.push(value);
      }
    }
    if (data.length > 0) {
      onEvent({ event, data: data.join('\n') });
    }
  }

  function drain() {
    const parts = buffer.split(/\r?\n\r?\n/);
    buffer = parts.pop() ?? '';
    parts.forEach(emit);
  }

  return {
    push(chunk: Uint8Array) {
      buffer += decoder.decode(chunk, { stream: true });
      drain();
    },
    end() {
      buffer += decoder.decode();
      drain();
      if (buffer.trim() !== '') {
        emit(buffer);
      }
      buffer = '';
    },
  };
}
