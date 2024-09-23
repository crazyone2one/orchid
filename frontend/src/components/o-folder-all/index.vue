<script setup lang="ts">
import {addCommasToNumber} from "/@/utils";

const emit = defineEmits<{
  (e: 'setActiveFolder', val: string): void;
}>();
const props = defineProps<{
  activeFolder?: string; // 选中的节点
  folderName: string; // 名称
  allCount: number; // 总数
}>();
const isExpandAll = defineModel<boolean>('isExpandAll', {
  required: false,
  default: undefined,
});
const getFolderClass = () => {
  return props.activeFolder === 'all' ? 'folder-text folder-text--active' : 'folder-text';
}
</script>

<template>
  <div class="folder">
    <div :class="getFolderClass()" @click="emit('setActiveFolder', 'all')">
      <div class="folder-icon i-carbon-folders"/>
      <div class="folder-name">{{ props.folderName }}</div>
      <div class="folder-count">({{ addCommasToNumber(props.allCount) }})</div>
    </div>
    <div class="ml-auto flex items-center">
      <slot name="expandLeft"></slot>
      <n-tooltip>
        <template #trigger>
          <div :class="isExpandAll ? 'i-my-icons:folder-collapse' : 'i-my-icons:folder-expansion'"
               class="!mr-0 p-[4px] w-[10px] h-[10px]"
               @click="isExpandAll=!isExpandAll"/>
        </template>
        {{ isExpandAll ? $t('common.collapseAll') : $t('common.expandAll') }}
      </n-tooltip>
      <slot name="expandRight"></slot>
    </div>
  </div>
</template>

<style scoped>
.folder {
  @apply flex cursor-pointer items-center justify-between;

  padding: 4px;
  border-radius: 2px;

  &:hover {
    background-color: rgb(var(--primary-1));
  }

  .folder-text {
    @apply flex cursor-pointer items-center;

    height: 26px;

    .folder-icon {
      margin-right: 4px;
      color: var(--color-text-4);
    }

    .folder-name {
      color: var(--color-text-1);
    }

    .folder-count {
      margin-left: 4px;
      color: var(--color-text-4);
    }
  }

  .folder-text--active {
    .folder-icon,
    .folder-name,
    .folder-count {
      color: rgb(var(--primary-5));
    }
  }
}
</style>