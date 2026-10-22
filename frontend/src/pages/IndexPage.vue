<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { career, featuredCases, otherCases, profile } from '@/content';
import { useChatStore } from '@/stores/chat';
import SiteHero from '@/components/SiteHero.vue';
import CaseCard from '@/components/CaseCard.vue';
import CareerTimeline from '@/components/CareerTimeline.vue';
import ChatPanel from '@/components/ChatPanel.vue';
import { hasSeenTour } from '@/tour/firstVisit';
import { startTour } from '@/tour/startTour';

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

// 처음 온 사람에게만 둘러보기를 띄운다(화면이 자리 잡은 뒤)
let tourTimer: ReturnType<typeof setTimeout> | undefined;
onMounted(() => {
  if (!hasSeenTour()) {
    tourTimer = setTimeout(startTour, 800);
  }
});
onBeforeUnmount(() => clearTimeout(tourTimer));
</script>

<template>
  <main class="page index-page">
    <div class="index-hero" :class="{ 'index-hero--chatting': chatting }">
      <SiteHero :profile="me" />
      <section id="chat" class="index-hero__chat" data-tour="chat">
        <h2 class="section-title">제 경력에 대해 물어보세요</h2>
        <p class="index-hero__chat-sub">이 사이트의 사례 글을 근거로 AI가 답합니다.</p>
        <ChatPanel />
      </section>
    </div>

    <section id="cases">
      <h2 class="section-title">대표 사례</h2>
      <div class="case-list" data-tour="cases">
        <CaseCard v-for="c in featuredCases()" :key="c.slug" :item="c" />
      </div>

      <div class="more-cases" data-tour="more-cases">
        <h3 class="more-cases__title">그 밖의 사례</h3>
        <ul>
          <li v-for="c in otherCases()" :key="c.slug">
            <router-link :to="`/cases/${c.slug}`">{{ c.title }}</router-link>
            <p>{{ c.summary }}</p>
          </li>
        </ul>
      </div>
    </section>

    <section id="career" data-tour="career">
      <h2 class="section-title">경력</h2>
      <CareerTimeline :career="career()" />
    </section>
  </main>
</template>
