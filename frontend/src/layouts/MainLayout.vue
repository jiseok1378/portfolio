<script setup lang="ts">
import { profile } from '@/content';
import SiteFooter from '@/components/SiteFooter.vue';

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
        <a v-if="me.github" :href="me.github" target="_blank" rel="noopener">GitHub</a>
      </nav>
    </q-header>
    <q-page-container>
      <router-view />
      <SiteFooter :profile="me" />
    </q-page-container>
  </q-layout>
</template>
