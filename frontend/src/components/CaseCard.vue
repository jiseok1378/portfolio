<script setup lang="ts">
import type { CaseContent } from '@/content';

defineProps<{ item: CaseContent }>();
</script>

<template>
  <router-link :to="`/cases/${item.slug}`" class="case-card">
    <p class="case-card__period">{{ item.period }}</p>
    <p class="case-card__context">{{ item.context }}</p>
    <h3 class="case-card__title">{{ item.title }}</h3>
    <p class="case-card__summary">{{ item.summary }}</p>
    <p v-if="item.metric" class="case-card__metric">{{ item.metric }}</p>
    <ul class="tags">
      <li v-for="tag in item.tags" :key="tag">{{ tag }}</li>
    </ul>
  </router-link>
</template>
