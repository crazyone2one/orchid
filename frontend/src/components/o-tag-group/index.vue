<script setup lang="ts">
import {computed, useAttrs} from "vue";

const props = withDefaults(defineProps<{
  tagList: Array<any>;
  showNum?: number;
  nameKey?: string;
  size?: 'small' | 'medium' | 'large',
  isStringTag?: boolean;
  allowEdit?: boolean;
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
const tagsTooltip = computed(() => {
  return filterTagList.value.map((e: any) => (props.isStringTag ? e : e[props.nameKey])).join('，');
});
const numberTagWidth = computed(() => {
  const numberStr = `${props.tagList.length - props.showNum}`;
  return numberStr.length + 4;
});
</script>

<template>
  <div v-if="tagList.length > 0"
       :class="`tag-group-class ${props.allowEdit ? 'cursor-pointer' : ''}`"
       @click="emit('click')">
    <n-tag v-for="tag of showTagList" :key="tag.id" :size="props.size" v-bind="attrs">
      {{ props.isStringTag ? tag : tag[props.nameKey] }}
    </n-tag>
    <n-tooltip v-if="props.tagList.length > props.showNum">
      <template #trigger>
        <n-tag :size="props.size" :width="numberTagWidth" v-bind="attrs">
          + {{ props.tagList.length - props.showNum }}
        </n-tag>
      </template>
      {{ tagsTooltip }}
    </n-tooltip>
  </div>
  <span v-else :class="`tag-group-class ${props.allowEdit ? 'min-h-[24px] cursor-pointer' : ''}`"> - </span>
</template>

<style scoped>
.tag-group-class {
  max-width: 440px;
  @apply flex w-full flex-row;
}
</style>