<script setup lang="ts">

import type {DropdownOption} from "naive-ui";

const props = defineProps<{
  list: DropdownOption[];
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
    <n-dropdown :trigger="props.trigger || 'click'" :options="props.list" @select="handleSelect">
      <n-button text>
        <template #icon>
          <n-icon :size="25" class="mt-1 ml-1" >
            <div class="i-carbon-caret-down mb-4 text-violet-400"/>
          </n-icon>
        </template>
      </n-button>
    </n-dropdown>
  </div>
</template>

<style scoped>

</style>