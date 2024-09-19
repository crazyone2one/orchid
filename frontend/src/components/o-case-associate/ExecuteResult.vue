<script setup lang="ts">
import {LastExecuteResults} from "/@/enums/case-enum.ts";
import {lastExecuteResultMap} from "/@/components/o-case-associate/utils.ts";


const props = defineProps<{
  executeResult?: LastExecuteResults;
}>();
</script>

<template>
  <div v-if="props.executeResult" class="flex items-center">
    <div class="mr-1" :class="lastExecuteResultMap[props.executeResult]?.icon || ''"
         :style="{ color: lastExecuteResultMap[props.executeResult]?.color }"></div>
    <span class="text-[14px]">{{ $t(lastExecuteResultMap[props.executeResult]?.statusText || '-') }}</span>
  </div>
</template>

<style scoped>

</style>