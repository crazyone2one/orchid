<script setup lang="ts">
import {ref} from "vue";
import {DropdownOption} from "naive-ui";

const props = defineProps<{
  title: string;
  options: { label: string; value: string }[];
}>();
const emit = defineEmits<{
  (e: 'handleChange', val: string[]): void;
}>();
const selectList = defineModel<string[]>('selectList', {
  required: false,
});

const visible = ref(false);

function handleChange(val: (string | number | boolean)[]) {
  emit('handleChange', val as string[]);
}

const options = ref<DropdownOption[]>([])
</script>

<template>
  <n-dropdown :options="options">
    <n-button secondary
              :class="`px-[4px] py-[2px]`"
              @click="visible = !visible">
      <span class="mr-[8px] font-medium"> {{ props.title }} </span>
      <div class="i-carbon-filter text-[12px] font-medium"/>
    </n-button>
  </n-dropdown>
</template>

<style scoped>

</style>