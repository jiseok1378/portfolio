<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core';
import type { FlowNodeData } from './layout';

defineProps<{ data: FlowNodeData; type: string }>();
</script>

<template>
  <div class="vf-node" :class="[`vf-node--${type}`, { 'vf-node--key': data.key }]">
    <Handle id="top" type="target" :position="Position.Top" />
    <Handle id="left" type="target" :position="Position.Left" />
    <strong>{{ data.label }}</strong>
    <span v-if="data.detail">{{ data.detail }}</span>
    <Handle id="bottom" type="source" :position="Position.Bottom" />
    <Handle id="right" type="source" :position="Position.Right" />
    <Handle id="top-left" type="target" :position="Position.Top" :style="{ left: '20px' }" />
    <Handle id="bottom-left" type="source" :position="Position.Bottom" :style="{ left: '20px' }" />
  </div>
</template>
