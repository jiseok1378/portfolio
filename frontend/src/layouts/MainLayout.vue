<script setup lang="ts">
import { profile } from '@/content';
import { nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import SiteFooter from '@/components/SiteFooter.vue';
import { startTour } from '@/tour/startTour';

const route = useRoute();
const router = useRouter();

// 둘러보기는 첫 화면 기준이라, 다른 화면에서는 첫 화면으로 간 뒤 시작한다
async function replayTour() {
  if (route.path !== '/') {
    await router.push('/');
  }
  window.scrollTo({ top: 0 });
  await nextTick();
  setTimeout(startTour, 300);
}

const me = profile();
</script>

<template>
  <q-layout view="hHh lpR fFf">
    <q-header class="site-header">
      <nav class="site-nav">
        <router-link to="/" class="site-nav__home">{{ me.name || '포트폴리오' }}</router-link>
        <router-link to="/#cases">사례</router-link>
        <router-link to="/#career">경력</router-link>
        <router-link to="/#chat">챗봇</router-link>
        <button type="button" class="site-nav__tour" @click="replayTour">둘러보기</button>
        <a v-if="me.github" :href="me.github" target="_blank" rel="noopener">GitHub</a>
      </nav>
    </q-header>
    <q-page-container>
      <router-view />
      <SiteFooter :profile="me" />
    </q-page-container>
  </q-layout>
</template>
