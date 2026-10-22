import { describe, expect, test } from 'vitest';
import { createSseParser, type SseEvent } from './sseParser';

const enc = new TextEncoder();

function collect(chunks: Uint8Array[]): SseEvent[] {
  const events: SseEvent[] = [];
  const parser = createSseParser((e) => events.push(e));
  chunks.forEach((c) => parser.push(c));
  parser.end();
  return events;
}

describe('createSseParser', () => {
  test('joins an event split across chunks', () => {
    expect(collect([enc.encode('event:token\ndata:안'), enc.encode('녕\n\n')])).toEqual([
      { event: 'token', data: '안녕' },
    ]);
  });

  test('handles a Korean char split in the middle of its bytes', () => {
    const bytes = enc.encode('event:token\ndata:안\n\n');
    const cut = bytes.indexOf(0xec) + 1; // '안' = EC 95 88
    expect(collect([bytes.slice(0, cut), bytes.slice(cut)])).toEqual([{ event: 'token', data: '안' }]);
  });

  test('handles multiple events in one chunk and CRLF line endings', () => {
    expect(collect([enc.encode('event: token\r\ndata: a\r\n\r\nevent:done\r\ndata:{}\r\n\r\n')])).toEqual([
      { event: 'token', data: 'a' },
      { event: 'done', data: '{}' },
    ]);
  });

  test('joins multiple data lines with newline', () => {
    expect(collect([enc.encode('event:token\ndata:첫 줄\ndata:둘째 줄\n\n')])).toEqual([
      { event: 'token', data: '첫 줄\n둘째 줄' },
    ]);
  });

  test('ignores comment lines and defaults event name to message', () => {
    expect(collect([enc.encode(':heartbeat\n\ndata:x\n\n')])).toEqual([{ event: 'message', data: 'x' }]);
  });

  test('flushes a final event without trailing blank line on end()', () => {
    expect(collect([enc.encode('event:done\ndata:{}')])).toEqual([{ event: 'done', data: '{}' }]);
  });
});
