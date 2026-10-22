<script setup lang="ts">
import type { Snippet } from '@/content';

defineProps<{ snippets: Snippet[] }>();
</script>

<template>
  <section class="case-snippets">
    <h2>핵심 코드</h2>
    <figure v-for="s in snippets" :key="s.title" class="snippet">
      <figcaption>
        <span class="snippet__title">{{ s.title }}</span>
        <span class="snippet__lang">{{ s.lang }}</span>
      </figcaption>
      <pre><code>{{ s.code }}</code></pre>
    </figure>
  </section>
</template>
