<script setup lang="ts">
import {computed, useAttrs} from "vue";

const props = withDefaults(defineProps<{
  tagList: Array<any>; showNum?: number;
  nameKey?: string;
  size?: 'small' | 'medium' | 'large'
}>(), {
  showNum: 2,
  nameKey: 'name',
  size: 'small'
})
const emit = defineEmits<{
  (e: 'click'): void;
}>();

const attrs = useAttrs();
const filterTagList = computed(() => {
  return (props.tagList || []).filter((item: any) => item) || [];
});
const showTagList = computed(() => {
  return filterTagList.value.slice(0, props.showNum);
});
</script>

<template>
  <div v-if="tagList.length > 0" class="flex max-w-[440px] flex-row" @click="emit('click')">
    <n-tag v-for="tag of showTagList" :key="tag.id" :size="props.size" v-bind="attrs">
      {{ tag[props.nameKey] }}
    </n-tag>
  </div>
  <span v-else> - </span>
</template>

<style scoped>

</style>