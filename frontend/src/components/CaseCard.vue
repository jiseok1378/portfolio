<script setup lang="ts">
import type { CaseContent } from '@/content';

defineProps<{ item: CaseContent }>();
</script>

<template>
  <router-link :to="`/cases/${item.slug}`" class="case-card">
    <ul class="case-card__meta">
      <li v-for="tag in item.tags" :key="tag" class="pill pill--blue">{{ tag }}</li>
    </ul>
    <h3 class="case-card__title">{{ item.title }}</h3>
    <p class="case-card__summary">{{ item.summary }}</p>
    <p v-if="item.metric" class="case-card__metric">{{ item.metric }}</p>
    <p class="case-card__period">{{ item.period }} · {{ item.context }}</p>
  </router-link>
</template>
