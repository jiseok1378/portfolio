// Gemini 를 부르는 명령(임베딩·서버 실행) 전에 쓰일 키가 내 키인지 확인한다.
// 기대하는 키 끝 4자리는 .claude/gemini-key-suffix.local (git 제외) 에 둔다.
// 막을 때는 exit 2 + stderr 메시지로 Claude 에게 이유를 돌려준다.
import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { join } from 'node:path';

const GEMINI_COMMAND = /gradlew\b[^|;&]*\b(embed|bootRun)\b|docker[ -]compose\b[^|;&]*\bup\b/;
const READS_USER_KEY = "GetEnvironmentVariable('GEMINI_API_KEY','User')";

const input = JSON.parse(readFileSync(0, 'utf8'));
const command = input.tool_input?.command ?? '';
if (!GEMINI_COMMAND.test(command)) process.exit(0);

const block = (msg) => {
  process.stderr.write(`[Gemini 키 검사] ${msg}\n`);
  process.exit(2);
};
const tail = (key) => (key ? key.trim().slice(-4) : '(없음)');

let expected;
try {
  expected = readFileSync(join(process.env.CLAUDE_PROJECT_DIR ?? '.', '.claude', 'gemini-key-suffix.local'), 'utf8').trim();
} catch {
  block('.claude/gemini-key-suffix.local 이 없습니다. 키 끝 4자리를 적어 두세요.');
}

const userKey = execFileSync('powershell', ['-NoProfile', '-Command', `[Environment]::${READS_USER_KEY}`], { encoding: 'utf8' });
if (tail(userKey) !== expected) {
  block(`사용자 환경변수 키 끝 4자리가 ${tail(userKey)} 입니다. 기대값 ${expected} 과 달라 실행을 막았습니다.`);
}

// 이 세션이 시작될 때 물려받은 키는 예전 값일 수 있다. 레지스트리에서 직접 읽어 넘기지 않으면 그 키가 쓰인다.
const sessionKey = process.env.GEMINI_API_KEY;
if (!command.includes(READS_USER_KEY) && sessionKey && tail(sessionKey) !== expected) {
  block(`세션 키 끝 4자리가 ${tail(sessionKey)} 입니다. 사용자 환경변수(레지스트리)에서 읽은 키를 GEMINI_API_KEY 로 명시해 넘기세요.`);
}
