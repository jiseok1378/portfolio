<script setup lang="ts">
import { ref } from 'vue';
import ChatPanel from './ChatPanel.vue';

defineProps<{ caseSlug: string; caseTitle: string }>();

const open = ref(false);
</script>

<template>
  <button type="button" class="ask-button" @click="open = true">이 사례에 대해 챗봇에게 묻기</button>
  <q-dialog v-model="open">
    <div class="chat-dialog">
      <header class="chat-dialog__header">
        <p>{{ caseTitle }}</p>
        <button type="button" class="chat-dialog__close" aria-label="닫기" @click="open = false">✕</button>
      </header>
      <ChatPanel :case-slug="caseSlug" />
    </div>
  </q-dialog>
</template>
