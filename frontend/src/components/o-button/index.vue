<script setup lang="ts">
const props = withDefaults(defineProps<{
  type?: 'default' | 'tertiary' | 'primary' | 'success' | 'info' | 'warning' | 'error';
  text?: boolean;
  disabled?: boolean;
  loading?: boolean;
  content?: string;
}>(), {
  type: 'primary', text: true
})
const emit = defineEmits(['click']);
const handleClick = () => {
  if (!props.disabled) {
    emit('click')
  }
}
</script>

<template>
  <n-button :type="type" :text="text" :disabled="disabled" :loading="loading" @click="handleClick">
    {{ content }}
  </n-button>
</template>

<style scoped>
.n-button {
  &:not(:last-child) {
    @apply mr-2;
  }

  padding: 0 2px;
}
</style>