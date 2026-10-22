<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { VueFlow } from '@vue-flow/core';
import '@vue-flow/core/dist/style.css';
import { FLOWS } from './flows';
import { layoutFlow, type LayoutMode } from './layout';
import FlowNode from './FlowNode.vue';

const props = defineProps<{ kind: keyof typeof FLOWS }>();

const flow = computed(() => FLOWS[props.kind]);
const root = ref<HTMLElement | null>(null);
const mode = ref<LayoutMode>('wide');
const graph = computed(() => layoutFlow(flow.value, mode.value));
// 그림 높이: 가장 아래 노드 위치 + 여유
const height = computed(() => Math.max(...graph.value.nodes.map((n) => n.position.y)) + 110);

let observer: ResizeObserver | undefined;
onMounted(() => {
  if (!root.value || typeof ResizeObserver === 'undefined') {
    return;
  }
  observer = new ResizeObserver(([entry]) => {
    mode.value = (entry?.contentRect.width ?? 800) < 640 ? 'narrow' : 'wide';
  });
  observer.observe(root.value);
});
onBeforeUnmount(() => observer?.disconnect());
</script>

<template>
  <figure ref="root" class="flow">
    <figcaption class="flow__caption">{{ flow.caption }}</figcaption>

    <!-- 그림은 보기 전용. 같은 내용을 화면 낭독기용 목록으로 함께 둔다 -->
    <div class="flow__canvas" :style="{ height: `${height}px` }" aria-hidden="true">
      <VueFlow
        :key="mode"
        :nodes="graph.nodes"
        :edges="graph.edges"
        fit-view-on-init
        :fit-view-options="{ padding: 0.08, maxZoom: 1 }"
        :min-zoom="0.4"
        :max-zoom="1"
        :nodes-draggable="false"
        :nodes-connectable="false"
        :elements-selectable="false"
        :pan-on-drag="false"
        :zoom-on-scroll="false"
        :zoom-on-pinch="false"
        :zoom-on-double-click="false"
        :prevent-scrolling="false"
      >
        <template #node-step="nodeProps">
          <FlowNode :data="nodeProps.data" type="step" />
        </template>
        <template #node-branch="nodeProps">
          <FlowNode :data="nodeProps.data" type="branch" />
        </template>
      </VueFlow>
    </div>

    <ol class="flow__steps sr-only">
      <li v-for="step in flow.steps" :key="step.label" class="flow__step">
        <div class="flow__node">
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
