import { marked } from 'marked';
import DOMPurify from 'dompurify';

/** 챗봇 답변(마크다운)을 HTML 로 바꾼다. AI 가 만든 글이므로 위험한 태그·속성은 지운다. */
export function renderMarkdown(text: string): string {
  const html = marked.parse(text, { async: false, gfm: true, breaks: true });
  return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } });
}
