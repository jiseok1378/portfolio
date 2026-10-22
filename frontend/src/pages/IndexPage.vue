<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { career, featuredCases, otherCases, profile } from '@/content';
import { useChatStore } from '@/stores/chat';
import SiteHero from '@/components/SiteHero.vue';
import CaseCard from '@/components/CaseCard.vue';
import CareerTimeline from '@/components/CareerTimeline.vue';
import ChatPanel from '@/components/ChatPanel.vue';

const me = profile();
const { messages } = storeToRefs(useChatStore());

// 첫 질문을 보내면 챗봇이 소개 아래로 내려와 전체 폭을 쓴다
const hasConversation = computed(() => messages.value.length > 0);
const chatting = ref(hasConversation.value);

watch(hasConversation, (now) => {
  if (!now || chatting.value) {
    return;
  }
  const reduce = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
  const doc = document as Document & { startViewTransition?: (cb: () => Promise<void>) => unknown };
  if (doc.startViewTransition && !reduce) {
    doc.startViewTransition(async () => {
      chatting.value = true;
      await nextTick();
    });
  } else {
    chatting.value = true;
  }
});
</script>

<template>
  <main class="page index-page">
    <div class="index-hero" :class="{ 'index-hero--chatting': chatting }">
      <SiteHero :profile="me" />
      <section id="chat" class="index-hero__chat">
        <h2 class="section-title">제 경력에 대해 물어보세요</h2>
        <p class="index-hero__chat-sub">이 사이트의 사례 글을 근거로 AI가 답합니다.</p>
        <ChatPanel />
      </section>
    </div>

    <section id="cases">
      <h2 class="section-title">대표 사례</h2>
      <div class="case-list">
        <CaseCard v-for="c in featuredCases()" :key="c.slug" :item="c" />
      </div>

      <div class="more-cases">
        <h3 class="more-cases__title">그 밖의 사례</h3>
        <ul>
          <li v-for="c in otherCases()" :key="c.slug">
            <router-link :to="`/cases/${c.slug}`">{{ c.title }}</router-link>
            <p>{{ c.summary }}</p>
          </li>
        </ul>
      </div>
    </section>

    <section id="career">
      <h2 class="section-title">경력</h2>
      <CareerTimeline :career="career()" />
    </section>
  </main>
</template>
