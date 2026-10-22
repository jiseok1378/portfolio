<script setup lang="ts">
import type { CareerContent } from '@/content';

defineProps<{ career: CareerContent }>();
</script>

<template>
  <div class="career">
    <article v-for="job in career.jobs" :key="job.company" class="career__job">
      <header>
        <p class="career__period">{{ job.period }}</p>
        <h3>{{ job.company }} <span class="career__role">{{ job.role }}</span></h3>
        <p class="career__stack">{{ job.stack.join(' · ') }}</p>
      </header>
      <q-expansion-item
        v-for="story in job.stories"
        :key="story.title"
        :label="story.title"
        dense
        class="career__story"
      >
        <p>{{ story.body }}</p>
        <p v-for="link in story.links" :key="link.url">
          <a :href="link.url" target="_blank" rel="noopener">{{ link.label }}</a>
        </p>
      </q-expansion-item>
    </article>

    <article class="career__job">
      <header><h3>사이드 프로젝트</h3></header>
      <q-expansion-item
        v-for="p in career.sideProjects"
        :key="p.name"
        :label="p.period ? `${p.name} (${p.period})` : p.name"
        dense
        class="career__story"
      >
        <p>{{ p.body }}</p>
      </q-expansion-item>
    </article>

    <article class="career__job">
      <header><h3>학력 · 자격 · 교육</h3></header>
      <ul class="career__list">
        <li v-for="e in career.education" :key="e.name">{{ e.period }} {{ e.name }}</li>
        <li v-for="c in career.certifications" :key="c.name">{{ c.date }} {{ c.name }}</li>
      </ul>
    </article>
  </div>
</template>
