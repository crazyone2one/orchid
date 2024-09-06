<script setup lang="ts">
import {computed, ref, watch} from "vue";
import type {FormInst, FormItemRule, FormRules} from "naive-ui";
import {useI18n} from "vue-i18n";

interface FieldConfig {
  field?: string;
  rules?: FormRules;
  placeholder?: string;
  maxLength?: number;
  isTextArea?: boolean;
  nameExistTipText?: string; // 添加重复提示文本
}

export interface ConfirmValue {
  field: string;
  id?: string;
}

const props = withDefaults(defineProps<{
  title: string;
  subTitleTip?: string; // 子内容提示
  isDelete?: boolean; // 当前使用是否是移除
  fieldConfig?: FieldConfig; // 表单配置项
  allNames?: string[]; // 添加或者重命名名称重复
  okText?: string; // 确定按钮文本
  cancelText?: string;
  nodeId?: string; // 节点 id
  visible?: boolean; // 是否打开
  loading?: boolean;
}>(), {
  isDelete: true, // 默认移除pop
  okText: 'common.remove',
});
const {t} = useI18n()
const formRef = ref<FormInst | null>(null);
const currentVisible = ref(props.visible || false);
// 获取当前标题的样式
const titleClass = computed(() => {
  return props.isDelete
      ? 'ml-2 font-medium text-[14px]'
      : 'mb-[8px] font-medium text-[14px] leading-[22px]';
});
const form = ref({
  field: props.fieldConfig?.field || '',
});
const emits = defineEmits<{
  (e: 'confirm', formValue: ConfirmValue, cancel?: () => void): void;
  (e: 'cancel'): void;
  (e: 'update:visible', visible: boolean): void;
}>();
const handleCancel = () => {
  currentVisible.value = false;
  emits('cancel')
  form.value.field = '';
  formRef.value?.restoreValidation();
};
const emitConfirm = () => emits('confirm', {...form.value, id: props.nodeId}, handleCancel);
const handleConfirm = () => {
  if (!formRef.value) {
    emitConfirm();
    return;
  }
  formRef.value?.validate((errors) => {
    if (!errors) {
      emitConfirm();
    }
  });
}
watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
    }
);

// watch(
//     () => currentVisible.value,
//     (val) => {
//       if (!val) {
//         emits('cancel');
//       }
//       emits('update:visible', val);
//     }
// );
</script>

<template>
  <n-popover v-model:show="currentVisible" trigger="click">
    <template #trigger>
      <div>
        <slot name="trigger"></slot>
      </div>
    </template>
    <div class="flex flex-row flex-nowrap items-center">
      <slot name="icon">
      </slot>
      <div :class="[titleClass]">
        {{ props.title || '' }}
      </div>
    </div>
    <!-- 描述展示 -->
    <div v-if="props.subTitleTip" class="ml-8 mt-2 text-sm">
      {{ props.subTitleTip }}
    </div>
    <n-form v-else ref="formRef" :model="form"
            :rules="props.fieldConfig?.rules ||[{ required: true, message: $t('popConfirm.nameNotNull') },
            { validator(_rule: FormItemRule, value: string){
              console.log(props.allNames)
              if((props.allNames || []).includes(value)){
                if(props.fieldConfig && props.fieldConfig.nameExistTipText){
                  return new Error(t(props.fieldConfig.nameExistTipText));
                }else {
                  return new Error(t('popConfirm.nameExist'));
                }
              }
            }}]">
      <n-form-item path="field">
        <n-input v-if="props.fieldConfig?.isTextArea" v-model:value="form.field" type="textarea"
                 :maxlength="props.fieldConfig?.maxLength || 1000" class="w-[245px]"
                 :placeholder="props.fieldConfig?.placeholder"/>
        <n-input v-else v-model:value="form.field" :maxlength="255" class="w-[245px]"
                 :placeholder="props.fieldConfig?.placeholder"/>
      </n-form-item>
    </n-form>
    <div class="mb-1 mt-4 flex flex-row flex-nowrap justify-end gap-2">
      <n-button secondary size="tiny" :loading="props.loading" @click="handleCancel">
        {{ props.cancelText || t('common.cancel') }}
      </n-button>
      <n-button type="primary" size="tiny" :loading="props.loading" @click="handleConfirm">
        {{ t(props.okText) || t('common.confirm') }}
      </n-button>
    </div>
  </n-popover>
</template>

<style scoped>

</style>