<script setup lang="ts">
import { computed, watchEffect } from 'vue';
import { findCase } from '@/content';
import CaseSections from '@/components/CaseSections.vue';
import ChatDialogButton from '@/components/ChatDialogButton.vue';
import FlowDiagram from '@/components/diagrams/FlowDiagram.vue';

const props = defineProps<{ slug: string }>();

const item = computed(() => findCase(props.slug));

watchEffect(() => {
  document.title = item.value ? `${item.value.title} | 포트폴리오` : '포트폴리오';
});
</script>

<template>
  <main class="page case-page">
    <template v-if="item">
      <router-link to="/#cases" class="back-link">전체 사례 보기</router-link>
      <p class="case-page__period">{{ item.period }}</p>
      <p class="case-page__context">{{ item.context }}</p>
      <h1>{{ item.title }}</h1>
      <p v-if="item.unfinished" class="case-page__badge">미완성 사이드 프로젝트</p>
      <p class="case-page__summary">{{ item.summary }}</p>
      <ul class="tags">
        <li v-for="tag in item.tags" :key="tag">{{ tag }}</li>
      </ul>

      <FlowDiagram v-if="item.diagram" :kind="item.diagram" />

      <CaseSections :sections="item.sections" />

      <ul v-if="item.links.length > 0" class="case-page__links">
        <li v-for="link in item.links" :key="link.url">
          <a :href="link.url" target="_blank" rel="noopener">{{ link.label }}</a>
        </li>
      </ul>

      <ChatDialogButton :case-slug="item.slug" :case-title="item.title" />
    </template>

    <template v-else>
      <h1>사례를 찾을 수 없습니다</h1>
      <router-link to="/">처음 화면으로</router-link>
    </template>
  </main>
</template>
