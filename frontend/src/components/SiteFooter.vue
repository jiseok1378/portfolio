<script setup lang="ts">
import type { Profile } from '@/content';

defineProps<{ profile: Profile }>();
</script>

<template>
  <footer class="site-footer">
    <ul class="site-footer__contacts">
      <li v-if="profile.email"><a :href="`mailto:${profile.email}`">{{ profile.email }}</a></li>
      <li v-if="profile.phone"><a :href="`tel:${profile.phone}`">{{ profile.phone }}</a></li>
      <li v-if="profile.github">
        <a :href="profile.github" target="_blank" rel="noopener">GitHub</a>
      </li>
    </ul>
    <p class="site-footer__note">이 사이트는 Vue3 · Quasar · Spring Boot · Spring AI로 직접 만들었습니다.</p>
  </footer>
</template>
