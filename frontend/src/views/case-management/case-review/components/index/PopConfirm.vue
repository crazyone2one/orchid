<script setup lang="ts">
import {ref, watch} from "vue";
import type {FormInst, FormItemRule} from "naive-ui";
import {NPopconfirm} from "naive-ui";
import {useI18n} from "vue-i18n";
import {useForm} from "alova/client";
import {addReviewModule, updateReviewModule} from "/@/api/modules/case-management/case-review.ts";
import {useAppStore} from "/@/store";

interface FieldConfig {
  field?: string;
  // rules?: FieldRule[];
  placeholder?: string;
  maxLength?: number;
  isTextArea?: boolean;
}

const props = defineProps<{
  mode: 'add' | 'rename' | 'fileRename' | 'fileUpdateDesc' | 'repositoryRename';
  visible?: boolean;
  title?: string;
  allNames: string[];
  popupContainer?: string;
  fieldConfig?: FieldConfig;
  parentId?: string; // 父节点 id
  nodeId?: string; // 节点 id
}>();
const {t} = useI18n()
const appStore = useAppStore()
const emit = defineEmits(['update:visible', 'close', 'addFinish', 'renameFinish', 'updateDescFinish']);
const innerVisible = ref(props.visible || false);
const formRef = ref<FormInst | null>(null);
const rules = {
  field: [{required: true, message: t('project.fileManagement.nameNotNull')},
    {
      validator(_rule: FormItemRule, value: string) {
        if (props.allNames.includes(value)) {
          return new Error(t('project.fileManagement.nameExist'));
        }
      }
    }]
}
const {loading, form, send} = useForm(
    formData => {
      if (props.mode === 'add') {
        const param = {
          projectId: appStore.currentProjectId,
          parentId: props.parentId || '',
          name: formData?.field as string,
        };
        return addReviewModule(param);
      } else if (props.mode === 'rename') {
        const param = {
          id: props.nodeId || '',
          name: formData?.field as string,
        };
        return updateReviewModule(param);
      }
    },
    {
      // 初始化表单数据
      initialForm: {
        field: props.fieldConfig?.field || '',
      },
      resetAfterSubmiting: true
    });
const handleConfirm = (done?: (closed: boolean) => void) => {
  formRef.value?.validate(errors => {
    if (!errors) {
      send().then(() => {
        if (props.mode === 'add') {
          window.$message.success(t('project.fileManagement.addSubModuleSuccess'));
        } else if (props.mode === 'rename') {
          window.$message.success(t('project.fileManagement.renameSuccess'));
        }
        emit('addFinish', form.value.field);
      })
      if (done) {
        done(true);
      } else {
        innerVisible.value = false;
      }
    }
  })
}
watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
);

watch(
    () => innerVisible.value,
    (val) => {
      if (!val) {
        emit('close');
      }
      emit('update:visible', val);
    }
);
</script>

<template>
  <n-popconfirm v-model:show="innerVisible" trigger="click"
                :negative-button-props="{ disabled: loading}"
                :positive-button-props="{ loading }"
                :show-icon="false"
                @positive-click="handleConfirm()"
  >
    <template #trigger>
      <div>
        <slot name="trigger"></slot>
      </div>
    </template>
    <div>
      <div class="mb-[8px] font-medium">
        {{
          props.title ||
          (props.mode === 'add' ? $t('project.fileManagement.addSubModule') : $t('project.fileManagement.rename'))
        }}
      </div>
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="field">
          <n-input v-if="props.fieldConfig?.isTextArea" v-model:value="form.field" type="textarea"
                   :maxlength="props.fieldConfig?.maxLength || 1000"
                   :placeholder="props.fieldConfig?.placeholder"
                   :autosize="{maxRows:4}"
                   @keyup.enter="handleConfirm(undefined)"/>
          <n-input v-else v-model:value="form.field" :maxlength="255" class="w-[245px]"
                   :placeholder="props.fieldConfig?.placeholder"
                   @keyup.enter="handleConfirm(undefined)"/>
        </n-form-item>
      </n-form>
    </div>
  </n-popconfirm>
</template>

<style scoped>

</style>