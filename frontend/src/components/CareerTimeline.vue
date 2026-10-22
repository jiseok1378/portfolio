<script setup lang="ts">
import type { CareerContent } from '@/content';

defineProps<{ career: CareerContent }>();
</script>

<template>
  <div class="career">
    <article v-for="job in career.jobs" :key="job.company" class="career__job">
      <p class="career__period">{{ job.period }}</p>
      <div class="career__body">
        <h3>{{ job.company }} <span class="career__role">{{ job.role }}</span></h3>
        <ul class="career__stack">
          <li v-for="s in job.stack" :key="s">{{ s }}</li>
        </ul>
        <details v-for="story in job.stories" :key="story.title" class="career__story">
          <summary>{{ story.title }}</summary>
          <p>{{ story.body }}</p>
          <p v-for="link in story.links" :key="link.url" class="career__link">
            <a :href="link.url" target="_blank" rel="noopener">{{ link.label }}</a>
          </p>
        </details>
      </div>
    </article>

    <article class="career__job">
      <p class="career__period">사이드</p>
      <div class="career__body">
        <h3>사이드 프로젝트</h3>
        <details v-for="p in career.sideProjects" :key="p.name" class="career__story">
          <summary>{{ p.period ? `${p.name} (${p.period})` : p.name }}</summary>
          <p>{{ p.body }}</p>
          <p v-for="link in p.links" :key="link.url" class="career__link">
            <a :href="link.url" target="_blank" rel="noopener">{{ link.label }}</a>
          </p>
        </details>
      </div>
    </article>

    <article class="career__job">
      <p class="career__period">학력 · 자격</p>
      <div class="career__body">
        <h3>학력 · 자격 · 교육</h3>
        <ul class="career__list">
          <li v-for="e in career.education" :key="e.name"><span>{{ e.period }}</span>{{ e.name }}</li>
          <li v-for="c in career.certifications" :key="c.name"><span>{{ c.date }}</span>{{ c.name }}</li>
        </ul>
      </div>
    </article>
  </div>
</template>
