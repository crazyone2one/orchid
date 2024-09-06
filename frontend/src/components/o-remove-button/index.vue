<script setup lang="ts">
import {ref} from "vue";
import PopConfirm from '/@/components/o-popconfirm/index.vue'

export interface ConfirmValue {
  field: string;
  id?: string;
}

export type types = 'error' | 'info' | 'success' | 'warning';
const props = withDefaults(
    defineProps<{
      title: string;
      subTitleTip: string;
      loading?: boolean;
      removeText?: string;
      okText?: string;
      disabled?: boolean;
    }>(),
    {
      removeText: 'common.remove',
      disabled: false,
    }
);
const currentVisible = ref(false);

const handleCancel = () => {
  currentVisible.value = false;
};

const showPopover = () => {
  currentVisible.value = true;
};

const emit = defineEmits<{
  (e: 'ok'): void;
}>();
const handleOk = () => {
  emit('ok');
};
</script>

<template>
  <span>
    <pop-confirm :title="props.title" :sub-title-tip="props.subTitleTip"
                 :loading="props.loading"
                 :visible="currentVisible"
                 :ok-text="props.okText"
                 @confirm="handleOk"
                 @cancel="handleCancel">
      <slot>
        <n-button text :disabled="props.disabled" @click="showPopover">
        {{ $t(props.removeText) }}
        </n-button>
      </slot>
    </pop-confirm>
  </span>
</template>

<style scoped>

</style>