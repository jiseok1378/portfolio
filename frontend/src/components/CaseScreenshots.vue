<script setup lang="ts">
import type { Screenshot } from '@/content';

defineProps<{ screenshots: Screenshot[] }>();

// src 는 frontend/public 기준 경로다 (예: cases/<slug>/ambiguous.png)
const url = (src: string) => `/${src.replace(/^\/+/, '')}`;
</script>

<template>
  <section class="case-screenshots">
    <h2>실제 화면</h2>
    <figure v-for="s in screenshots" :key="s.src" class="screenshot">
      <img :src="url(s.src)" :alt="s.caption" loading="lazy" />
      <figcaption>{{ s.caption }}</figcaption>
    </figure>
  </section>
</template>
