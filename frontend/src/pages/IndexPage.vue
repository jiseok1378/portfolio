<script setup lang="ts">
import { career, cases, profile } from '@/content';
import SiteHero from '@/components/SiteHero.vue';
import CaseCard from '@/components/CaseCard.vue';
import CareerTimeline from '@/components/CareerTimeline.vue';

const me = profile();
</script>

<template>
  <main class="page index-page">
    <SiteHero :profile="me" />

    <section id="chat" class="index-page__chat" />

    <section id="cases">
      <h2 class="section-title">대표 사례</h2>
      <div class="case-grid">
        <CaseCard v-for="c in cases()" :key="c.slug" :item="c" />
      </div>
    </section>

    <section id="career">
      <h2 class="section-title">경력</h2>
      <CareerTimeline :career="career()" />
    </section>
  </main>
</template>
