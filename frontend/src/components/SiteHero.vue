<script setup lang="ts">
import { computed } from 'vue';
import { careerLength, type Profile } from '@/content';

const props = withDefaults(defineProps<{ profile: Profile; now?: Date }>(), {
  now: () => new Date(),
});

const length = computed(() => careerLength(props.profile.careerStart, props.now));
</script>

<template>
  <section class="hero">
    <p v-if="profile.name" class="hero__name" data-test="name">{{ profile.name }}</p>
    <p class="hero__title">{{ profile.title }} · 경력 {{ length }}</p>
    <h1 v-if="profile.headline" class="hero__headline" data-test="headline">{{ profile.headline }}</h1>
  </section>
</template>
