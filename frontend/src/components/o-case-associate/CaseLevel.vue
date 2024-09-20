<script setup lang="ts">
import {CaseLevel} from "/@/components/o-case-associate/types.ts";
import {computed} from "vue";

const props = defineProps<{
  caseLevel?: CaseLevel;
}>();
const caseLevelMap = {
  P0: {
    label: 'P0',
    bgColor: 'rgb(253,205,197)',
    borderColor: 'rgb(245,63,63)',
  },
  P1: {
    label: 'P1',
    bgColor: 'rgb(253,221,195)',
    borderColor: 'rgb(247, 114, 52)',
  },
  P2: {
    label: 'P2',
    bgColor: 'rgb(190,218,255)',
    borderColor: 'rgb(64,128,255)',
  },
  P3: {
    label: 'P3',
    bgColor: 'rgb(201,205,212)',
    borderColor: 'rgb(78,89,105)',
  },
};

const caseLevel = computed(() => (props.caseLevel ? caseLevelMap[props.caseLevel] : undefined));
</script>

<template>
  <div v-if="caseLevel" class="flex items-center">
    <div
        class="mr-[4px] h-[8px] w-[8px] rounded-full"
        :style="{
        backgroundColor: caseLevel.bgColor,
        border: `1px solid ${caseLevel.borderColor}`,
      }"
    ></div>
    {{ caseLevel.label }}
  </div>
  <span v-else class="text-[var(--color-text-2)]"> - </span>
</template>

<style scoped>

</style>