<script setup lang="ts">
import { computed } from 'vue';
import { FLOWS } from './flows';

const props = defineProps<{ kind: keyof typeof FLOWS }>();

const flow = computed(() => FLOWS[props.kind]);
</script>

<template>
  <figure class="flow">
    <figcaption class="flow__caption">{{ flow.caption }}</figcaption>
    <ol class="flow__steps">
      <li
        v-for="step in flow.steps"
        :key="step.label"
        class="flow__step"
        :class="{ 'flow__step--branched': step.branch }"
      >
        <div class="flow__node" :class="{ 'flow__node--key': step.key }">
          <strong>{{ step.label }}</strong>
          <span v-if="step.detail">{{ step.detail }}</span>
        </div>
        <div v-if="step.branch" class="flow__branch">
          <strong>{{ step.branch.label }}</strong>
          <div>{{ step.branch.detail }}</div>
        </div>
      </li>
    </ol>
  </figure>
</template>
