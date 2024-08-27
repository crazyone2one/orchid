<script setup lang="ts">
import {FormItemModel, FormMode} from "/@/components/o-batch-form/types.ts";
import {useI18n} from "vue-i18n";
import {ref, unref, watchEffect} from "vue";
import type {FormInst, FormItemRule, FormRules} from "naive-ui";
import {VueDraggable} from "vue-draggable-plus";
import {validateEmail} from "/@/utils/validate.ts";

const props = withDefaults(
    defineProps<{
      models: FormItemModel[];
      formMode: FormMode;
      addText: string;
      maxHeight?: string;
      defaultVals?: any[]; // 当外层是编辑状态时，可传入已填充的数据
      isShowDrag?: boolean; // 是否可以拖拽
      formWidth?: string; // 自定义表单区域宽度
      showEnable?: boolean; // 是否显示启用禁用switch状态
      hideAdd?: boolean; // 是否隐藏添加按钮
      addToolTip?: string;
    }>(),
    {
      maxHeight: '30vh',
      isShowDrag: false,
      hideAdd: false,
    }
);
const emit = defineEmits(['change']);
const defaultForm = {
  list: [] as Record<string, any>[],
};
const {t} = useI18n();
const formRef = ref<FormInst | null>(null)
const form = ref<Record<string, any>>({list: [...defaultForm.list]});
const formItem: Record<string, any> = {};

const getFormResult = () => {
  return unref<Record<string, any>[]>(form.value.list);
}
const formValidate = (cb: (res?: Record<string, any>[]) => void, isSubmit = true) => {
  formRef.value?.validate((errors) => {
    if (errors) {
      return
    }
    if (typeof cb === 'function') {
      if (isSubmit) {
        cb(getFormResult());
        return;
      }
      cb();
    }
  })
}
const addField = () => {
  const item = [{...formItem}];
  item[0].type = [];
  formValidate(() => {
    form.value.list.push(item[0]); // 序号自增，不会因为删除而重复
  }, false);
}
const removeField = (i: number) => {
  form.value.list.splice(i, 1);
}
const resetForm = () => {
  formRef.value?.restoreValidation();
}
const fieldNotRepeat = (value: string, index: number,
                        field: string,
                        msg?: string) => {
  if (value === '' || value === undefined) return;
  // 遍历其他同 field 名的输入框的值，检查是否与当前输入框的值重复
  for (let i = 0; i < form.value.list.length; i++) {
    if (i !== index && form.value.list[i][field].trim() === value) {
      return new Error(t(msg || ''));
    }
  }
}
const rules: FormRules = {
  name: [{
    trigger: ['input', 'blur'],
    required: true,
    message: t('system.user.createUserNameNotNull'),
    validator(_rule: FormItemRule, value: string) {
      if (value === '') {
        return new Error(t('system.user.createUserNameNotNull'))
      } else if (value.length > 255) {
        return new Error(t('system.user.createUserNameOverLength'))
      }
    }
  }],
  email: [
    {required: true, message: t('system.user.createUserEmailNotNull')},
    {
      validator(_rule: FormItemRule, value: string) {
        if (value === '') {
          return new Error(t('system.user.createUserEmailNotNull'))
        } else if (!validateEmail(value)) {
          return new Error(t('system.user.createUserEmailErr'))
        }
      }
    },
    // {notRepeat: true, message: 'system.user.createUserEmailNoRepeat'},
  ]
}
watchEffect(() => {
  props.models.forEach((e) => {
    // 默认填充表单项
    let value: string | number | boolean | string[] | number[] | undefined;
    if (e.type === 'inputNumber') {
      value = undefined;
    } else if (e.type === 'tagInput') {
      value = [];
    } else {
      value = e.defaultValue;
    }
    formItem[e.field] = value;
    if (props.showEnable) {
      // 如果有开启关闭状态，将默认禁用
      formItem.enable = false;
    }
    // 默认填充表单项的子项
    e.children?.forEach((child) => {
      formItem[child.field] = child.type === 'inputNumber' ? null : child.defaultValue;
    });
  });
  form.value.list = [{...formItem}];
  if (props.defaultVals?.length) {
    // 取出defaultVals的表单 field
    form.value.list = props.defaultVals.map((e) => e);
  }
});

defineExpose({
  formValidate,
  getFormResult,
  resetForm,
  // setFields,
});
</script>

<template>
  <n-form ref="formRef" :model="form" require-mark-placement="right-hanging">
    <div
        class="mb-[16px] overflow-y-auto rounded-[4px] border p-[12px]"
        :style="{ width: props.formWidth || '100%' }"
    >
      <n-scrollbar class="overflow-y-auto" :style="{ 'max-height': props.maxHeight }">
        <VueDraggable v-model="form.list"
                      ghost-class="ghost"
                      drag-class="dragChosenClass"
                      :disabled="!props.isShowDrag"
                      :force-fallback="true"
                      :animation="150"
                      handle=".dragIcon">
          <div v-for="(element, index) in form.list" :key="`${element.field}${index}`"
               class="draggableElement gap-[8px] pt-[6px] pr-[8px]">
            <div v-if="props.isShowDrag" class="dragIcon ml-[8px] mr-[8px] pt-[8px]">
              <div class="i-carbon-draggable block text-[16px]"/>
            </div>
            <n-form-item v-for="model of props.models" :key="`${model.field}${index}`"
                         :path="`list[${index}].${model.field}`"
                         :role="model.rules?.map((e)=>{
                         if(e.notRepeat===true){
                           return {
                             validator: (val:string) => fieldNotRepeat(val, index, model.field, e.message),
                           }
                         }
                       })"
            >
              <template #label>
                <div>{{ index === 0 && model.label ? t(model.label) : '' }}</div>
              </template>
              <n-input v-if="model.type === 'input'"
                       v-model:value="element[model.field]"

                       class="flex-1" :placeholder="t(model.placeholder || '')"
                       :max-length="model.maxLength || 255"
                       clearable
                       @change="emit('change')"/>
            </n-form-item>
            <div v-if="showEnable">
              <n-switch v-model:value="element.enable" class="mt-[8px]" size="small"/>
            </div>
            <div v-if="!props.hideAdd" v-show="form.list.length > 1" class="minus"
                 :class="[
                'flex',
                'h-full',
                'w-[32px]',
                'cursor-pointer',
                'items-center',
                'justify-center',
                'mt-[8px]',
              ]"
                 :style="{ 'margin-top':  !props.isShowDrag ? '36px' : '' }"
                 @click="removeField(index)">
              <div class="i-carbon-subtract-alt"/>
            </div>
          </div>
        </VueDraggable>
      </n-scrollbar>

      <div v-if="props.formMode==='create' && !props.hideAdd" class="w-full">
        <n-tooltip>
          <template #trigger>
            <n-button text class="px-0" @click="addField">
              <div class="i-carbon-add-alt text-[14px]"/>
            </n-button>
          </template>
          {{ props.addToolTip }}
          <span class="ml-2 inline-block cursor-pointer text-[rgb(var(--primary-4))]">
            {{ t('system.authorized.applyTrial') }}
          </span>
        </n-tooltip>
      </div>
    </div>
  </n-form>
</template>

<style scoped>
.draggableElement {
  @apply flex w-full items-start justify-between rounded;
}

.dragChosenClass {
  background: white;
  opacity: 1 !important;
  @apply rounded;

  .minus {
    margin: 0 !important;
  }
}
</style>