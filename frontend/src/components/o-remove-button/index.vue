<script setup lang="ts">
import {computed, ref} from "vue";
import {NPopconfirm} from "naive-ui";

export interface ConfirmValue {
  field: string;
  id?: string;
}

export type types = 'error' | 'info' | 'success' | 'warning';
const props = withDefaults(
    defineProps<{
      title: string; // 文本提示标题
      subTitleTip?: string; // 子内容提示
      isDelete?: boolean; // 当前使用是否是移除
      loading?: boolean;
      okText?: string; // 确定按钮文本
      cancelText?: string;
      nodeId?: string; // 节点 id
      removeText?: string;
      type?: types;
    }>(),
    {
      isDelete: true, // 默认移除pop
      okText: 'common.remove',
      removeText: 'common.remove',
      type: 'error'
    }
);
const currentVisible = ref(false);

const handleCancel = () => {
  currentVisible.value = false;
};

const showPopover = () => {
  currentVisible.value = true;
};
// 获取当前标题的样式
const titleClass = computed(() => {
  return props.isDelete
      ? 'ml-2 font-medium  text-[14px]'
      : 'mb-[8px] font-medium  text-[14px] leading-[22px]';
});
const emit = defineEmits<{
  (e: 'ok'): void;
}>();
const handleOk = () => {
  emit('ok');
};
</script>

<template>
  <n-popconfirm :show="currentVisible" trigger="click">
    <template #trigger>
      <n-button text :type="type" @click="showPopover">{{ $t(props.removeText) }}</n-button>
    </template>
    <template #action>
      <n-button secondary size="tiny" @click="handleCancel">{{ props.cancelText || $t('common.cancel') }}</n-button>
      <n-button type="primary" size="tiny" @click="handleOk">{{ $t(props.okText) || $t('common.confirm') }}</n-button>
    </template>
    <div class="flex items-center">
      <div class="flex flex-row flex-nowrap items-center">
        <div :class="[titleClass]">
          {{ props.title || '' }}
        </div>
      </div>
      <!-- 描述展示 -->
      <div v-if="props.subTitleTip" class="ml-8 mt-2 text-sm">
        {{ props.subTitleTip }}
      </div>
    </div>
  </n-popconfirm>
</template>

<style scoped>

</style>