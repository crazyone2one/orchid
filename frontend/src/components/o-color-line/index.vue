<script setup lang="ts">
const props = withDefaults(
    defineProps<{
      colorData: Array<{ percentage: number; color: string }>;
      height: string;
      radius?: string;
    }>(),
    {
      radius: 'var(--border-radius-large)',
    }
);
const getStyle = (item: { percentage: number; color: string }, index: number) => {
  return {
    width: `${item.percentage}%`,
    backgroundColor: item.color,
    height: props.height,
    display: 'inline-block',
    borderRight: index === props.colorData.length - 1 ? 'none' : '1px solid white', // 1px间隔
  };
};
</script>

<template>
  <n-popover trigger="hover" placement="bottom">
    <template #trigger>
      <div class="color-bar" :style="{ borderRadius: props.radius }">
        <template v-for="(item, index) in colorData">
          <div v-if="item.percentage > 0" :key="index" :style="getStyle(item, index)"></div>
        </template>
      </div>
    </template>
    <slot name="popoverContent"></slot>
  </n-popover>
</template>

<style scoped>
.color-bar {
  @apply flex w-full overflow-hidden;
}

.color-bar div {
  box-sizing: border-box;
}
</style>