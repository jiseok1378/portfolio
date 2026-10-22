// @vitest-environment node
import { expect, test } from 'vitest';
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

/**
 * 공개 저장소에 올라가는 화면 소스에 회사 내부 이름이 섞이지 않았는지 검사한다.
 * 금칙어 원문은 커밋하지 않고, content/forbidden-terms.sha256 의 해시와 비교한다.
 */
const root = fileURLToPath(new URL('.', import.meta.url));
const hashes = new Set(
  readFileSync(fileURLToPath(new URL('../../content/forbidden-terms.sha256', import.meta.url)), 'utf8')
    .split(/\r?\n/)
    .map((s) => s.trim())
    .filter(Boolean),
);

function files(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? files(path) : /\.(ts|vue|css)$/.test(name) ? [path] : [];
  });
}

const sha256 = (s: string) => createHash('sha256').update(s).digest('hex');

test('frontend source has no forbidden internal terms', () => {
  expect(hashes.size).toBeGreaterThan(0);
  const hits: string[] = [];
  for (const file of files(root)) {
    for (const token of readFileSync(file, 'utf8').match(/[A-Za-z0-9._]+/g) ?? []) {
      if (hashes.has(sha256(token.toLowerCase()))) {
        hits.push(relative(root, file)); // 단어 자체는 출력하지 않는다
      }
    }
  }
  expect(hits).toEqual([]);
});
