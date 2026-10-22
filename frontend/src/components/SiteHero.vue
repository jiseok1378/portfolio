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
    <template v-if="profile.headline">
      <h1 class="hero__headline" data-test="headline">{{ profile.headline }}</h1>
      <p class="hero__title">{{ profile.title }}</p>
    </template>
    <h1 v-else class="hero__title hero__title--main">{{ profile.title }}</h1>
    <p class="hero__length">경력 {{ length }}</p>
    <div class="hero__method">
      아래 사례는 모두 같은 순서로 썼습니다.
      <ol class="hero__steps">
        <li><span class="dot dot--problem" />문제</li>
        <li><span class="dot" />고민</li>
        <li><span class="dot dot--solve" />해결</li>
        <li><span class="dot dot--result" />결과</li>
      </ol>
    </div>
  </section>
</template>
