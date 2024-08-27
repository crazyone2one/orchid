<script setup lang="ts">

import type {DropdownOption} from "naive-ui";

const props = defineProps<{
  list: DropdownOption [];
  trigger?: 'hover' | 'click' | 'focus' | 'manual';
}>();
const emit = defineEmits(['select', 'close', 'open']);

const handleSelect = (value: string) => {
  const item = props.list.find((e: DropdownOption) => e.key === value);
  emit('select', item);
}
</script>

<template>
  <div>
    <n-dropdown :trigger="props.trigger || 'hover'" :options="props.list" @select="handleSelect">
      <n-button text>
        <template #icon>
          <div class="i-carbon-caret-down"/>
        </template>
      </n-button>
    </n-dropdown>
  </div>
</template>

<style scoped>

</style>