<script setup lang="ts">
import { computed } from 'vue';
import type { CaseSectionsData } from '@/content';

const props = defineProps<{ sections: CaseSectionsData }>();

// 순서 고정: 문제 → 고민 → 해결 → 결과 → 남은 위험·배운 점 → 면접 예상 질문
const lists = computed(() =>
  [
    { kind: 'thinking', title: '고민', items: props.sections.thinking },
    { kind: 'solution', title: '해결', items: props.sections.solution },
    { kind: 'result', title: '결과', items: props.sections.result },
    { kind: 'lessons', title: '남은 위험 · 배운 점', items: props.sections.lessons },
  ].filter((s) => s.items.length > 0),
);
</script>

<template>
  <div class="case-sections">
    <section v-if="sections.problem" class="case-section case-section--problem">
      <h2>문제</h2>
      <p>{{ sections.problem }}</p>
    </section>
    <section v-for="s in lists" :key="s.kind" class="case-section" :class="`case-section--${s.kind}`">
      <h2>{{ s.title }}</h2>
      <ul>
        <li v-for="item in s.items" :key="item">{{ item }}</li>
      </ul>
    </section>
    <section v-if="sections.interviewQuestions.length > 0" class="case-section case-section--questions">
      <h2>면접 예상 질문</h2>
      <dl>
        <template v-for="qa in sections.interviewQuestions" :key="qa.q">
          <dt>Q. {{ qa.q }}</dt>
          <dd>{{ qa.a }}</dd>
        </template>
      </dl>
    </section>
  </div>
</template>
