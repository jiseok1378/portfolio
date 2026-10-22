<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useChatStore } from '@/stores/chat';

const props = defineProps<{ caseSlug?: string }>();

const MAX_LENGTH = 500;
const SUGGESTIONS = [
  'Kafka DLQ는 왜 그렇게 설계했나요?',
  'LLM이 엉뚱한 ID로 조회한 문제는 어떻게 풀었나요?',
  '병렬 처리에서 결과가 섞인 문제는?',
  'AI가 같은 실수를 반복하지 않게 어떻게 했나요?',
];

const store = useChatStore();
const { messages, pending } = storeToRefs(store);
const input = ref('');
const log = ref<HTMLElement | null>(null);

const length = computed(() => input.value.trim().length);
const canSend = computed(() => length.value > 0 && length.value <= MAX_LENGTH && !pending.value);

async function send(text = input.value) {
  if (text.trim().length > MAX_LENGTH || pending.value) {
    return;
  }
  input.value = '';
  await store.ask(text, props.caseSlug);
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
    e.preventDefault();
    if (canSend.value) {
      void send();
    }
  }
}

// 답변이 늘어나면 대화창 맨 아래로
watch(
  () => messages.value.map((m) => m.content.length).join(),
  async () => {
    await nextTick();
    log.value?.scrollTo({ top: log.value.scrollHeight });
  },
);

onBeforeUnmount(() => store.cancel());
</script>

<template>
  <div class="chat">
    <div v-if="messages.length > 0" ref="log" class="chat__log" aria-live="polite">
      <div v-for="(m, i) in messages" :key="i" class="chat__msg" :class="`chat__msg--${m.role}`">
        <p v-if="m.content" class="chat__text">{{ m.content }}</p>
        <q-spinner-dots v-else-if="m.role === 'assistant' && !m.error && pending" size="24px" />
        <p v-if="m.error" class="chat__error">{{ m.error }}</p>
        <ul v-if="m.sources && m.sources.length > 0" class="chat__sources">
          <li v-for="s in m.sources" :key="s.slug">
            <router-link :to="`/cases/${s.slug}`">근거: {{ s.title }}</router-link>
          </li>
        </ul>
      </div>
    </div>

    <div v-else class="chat__suggestions">
      <button
        v-for="q in SUGGESTIONS"
        :key="q"
        type="button"
        class="chat__suggestion"
        data-test="suggestion"
        :disabled="pending"
        @click="send(q)"
      >
        {{ q }}
      </button>
    </div>

    <div class="chat__input">
      <textarea
        v-model="input"
        rows="2"
        placeholder="이 개발자의 경력에 대해 물어보세요"
        aria-label="질문 입력"
        @keydown="onKeydown"
      />
      <div class="chat__actions">
        <span class="chat__count" :class="{ 'chat__count--over': length > MAX_LENGTH }">{{ length }} / {{ MAX_LENGTH }}</span>
        <button type="button" class="chat__send" data-test="send" :disabled="!canSend" @click="send()">보내기</button>
      </div>
    </div>
    <p class="chat__notice">질문 내용은 AI 서비스 제공사(Google)에 전달됩니다.</p>
  </div>
</template>
