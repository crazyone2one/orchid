<script setup lang="ts">
const props = withDefaults(defineProps<{
  iconClass?: string
  isText?: boolean
}>(), {
  iconClass: 'i-ic-twotone-question-mark',
  isText: true
})
</script>

<template>
  <n-button :text="isText">
    <slot></slot>
    <template #icon>
      <n-icon>
        <div :class="iconClass"/>
      </n-icon>
    </template>
  </n-button>
</template>

<style scoped>

</style>