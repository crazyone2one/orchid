<script setup lang="ts">
import {computed, onBeforeMount, onBeforeUnmount, ref, watch} from "vue";
import {useAppStore} from "/@/store";
import {useRoute} from "vue-router";
import {useI18n} from "vue-i18n";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";
import {CreateOrUpdateCase, DetailCase, StepList} from "/@/models/case-management/feature-case.ts";
import {type FormInst, SelectOption} from 'naive-ui'
import {useRequest} from "alova/client";
import {getCaseDefaultFields, getCaseModuleTree} from "/@/api/modules/case-management/feature-case.ts";
import {ModuleTreeNode} from "/@/models/common.ts";
import RichText from '/@/components/o-rich-text/index.vue'
import StepDescription from "/@/views/case-management/components/StepDescription.vue";
import {getGenerateId} from "/@/utils";
import {FileItem} from "/@/components/o-upload/types.ts";
import {PreviewEditorImageUrl} from "/@/api/req-urls/case-management/feature-case.ts";
import AddStep from "/@/views/case-management/components/AddStep.vue";
import {CustomField, FieldOptions} from "/@/models/setting/template.ts";
import {FormItem, FormRuleItem} from "/@/views/case-management/case-management-feature/components/types.ts";

const props = defineProps<{
  formModeValue: Record<string, any>; // 表单值
  caseId: string; // 用例id
}>();
const emit = defineEmits(['update:formModeValue', 'changeFile']);
const {t} = useI18n();
const route = useRoute();
const appStore = useAppStore();
const featureCaseStore = useFeatureCaseStore();
const modelId = computed(() => featureCaseStore.moduleId[0] || 'root');
const currentProjectId = computed(() => appStore.currentProjectId);
// 总参数
const params = ref<Record<string, any>>({
  request: {},
  fileList: [], // 总文件列表
});
const fileList = ref<FileItem[]>([]);
const caseId = ref(props.caseId);
const formRules = ref<FormItem[]>([]);
const {send: fetchCaseDefaultFields} = useRequest(() => getCaseDefaultFields(currentProjectId.value), {
  immediate: false,
  force: true
})
const initDefaultFields = async () => {
  formRules.value = [];
  fetchCaseDefaultFields().then(res => {
    const {id, systemFields, customFields} = res;
    form.value.templateId = id;
    formRules.value = customFields.map((item: any) => {
      // const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
      let initValue = item.defaultValue;
      const optionsValue: FieldOptions[] = item.options;
      // if (memberType.includes(item.type)) {
      //   initValue = getDefaultMemberValue(item, optionsValue);
      // }

      return {
        type: item.type,
        name: item.fieldId,
        label: item.fieldName,
        value: initValue,
        required: item.required,
        options: optionsValue || [],
        props: {
          modelValue: initValue,
          options: optionsValue || [],
        },
      };
    });
    setSystemDefault(systemFields || []);
  })
};
const setSystemDefault = (systemFields: CustomField[]) => {
  systemFields.forEach((item: CustomField) => {
    form.value[item.fieldId] = item.defaultValue;
  });
  const {steps} = form.value;

  if (steps) {
    getStepData(steps);
  }
}
const getStepData = (steps: string) => {
  stepData.value = JSON.parse(steps).map((item: any) => {
    return {
      id: item.id,
      step: item.desc,
      expected: item.result,
    };
  });
}
const initForm: DetailCase = {
  id: '',
  projectId: currentProjectId.value,
  templateId: '',
  name: '',
  prerequisite: '',
  caseEditType: 'STEP',
  steps: '',
  textDescription: '',
  expectedResult: '',
  description: '',
  publicCase: false,
  moduleId: modelId.value,
  versionId: '',
  tags: [],
  customFields: [],
  relateFileMetaIds: [],
  functionalPriority: '',
  reviewStatus: 'UN_REVIEWED',
};

const form = ref<DetailCase | CreateOrUpdateCase>({...initForm});
const caseFormRef = ref<FormInst | null>(null);
const formRef = ref<FormInst | null>(null);
const rules = ref({
  name: [{required: true, message: t('system.orgTemplate.caseNamePlaceholder')}],
  moduleId: [{required: true, message: t('system.orgTemplate.moduleRuleTip')}],
})

// 前置操作附件id
const prerequisiteFileIds = ref<string[]>([]);
// 文本描述附件id
const textDescriptionFileIds = ref<string[]>([]);
// 预期结果附件id
const expectedResultFileIds = ref<string[]>([]);
// 描述附件id
const descriptionFileIds = ref<string[]>([]);
const caseTree = ref<ModuleTreeNode[]>([]);
const {send: fetchCaseTree} = useRequest(() => getCaseModuleTree({ projectId: currentProjectId.value }), {
  immediate: false,
  force: true
})
const handleUploadImage = (file: File) => {
  console.log('file', file)
}
const stepData = ref<StepList[]>([
  {
    id: getGenerateId(),
    step: '',
    expected: '',
    showStep: false,
    showExpected: false,
  },
]);
const allAttachmentsFileIds = computed(() => {
  return [
    ...prerequisiteFileIds.value,
    ...textDescriptionFileIds.value,
    ...expectedResultFileIds.value,
    ...descriptionFileIds.value,
  ];
});
const formItem = ref<FormRuleItem[]>([]);
const resetForm = async () => {
  caseFormRef.value?.restoreValidation();
  form.value = {...initForm, templateId: form.value.templateId};
  await initDefaultFields();
  form.value.customFields = formItem.value.map((item: any) => {
    return {
      fieldId: item.field,
      value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
    };
  });
  fileList.value = [];
  form.value.tags = [];
}
const handleUpdateValue = (value: string, option: SelectOption) => {
  form.value.customFields = [{
    fieldId: option.fieldId,
    value: Array.isArray(value) ? JSON.stringify(value) : value,
  }]
}
// 监视父组件传递过来的参数处理
watch(
    () => props.formModeValue,
    () => {
      // 这里要处理预览的参数格式给到params
      params.value = {...props.formModeValue};
    },
    {deep: true}
);
watch(
    () => stepData.value,
    () => {
      const res = stepData.value.map((item, index) => {
        return {
          id: item.id,
          num: index,
          desc: item.step,
          result: item.expected,
        };
      });
      form.value.steps = JSON.stringify(res);
    },
    {deep: true}
);
watch(() => form.value, (newValue) => {
  if (newValue) {
    params.value.request = {...form.value, caseDetailFileIds: allAttachmentsFileIds.value};
    emit('update:formModeValue', params.value);
  }
}, {deep: true})
watch(() => formRules.value, (val) => {
  val.forEach(item => {
    formItem.value.push({title: item.label, options: item.options, type: item.type})
  })
}, {deep: true})
onBeforeMount(() => {
  caseId.value = '';
  caseId.value = props.caseId;
  fetchCaseTree().then(res => caseTree.value = res)
  if (caseId.value) {

  } else {
    initDefaultFields()
  }
})
const validForm = () => {
  caseFormRef.value?.validate(errors => {
    if (errors) {
      console.log('caseFormRef', errors)
      return
    }
    formRef.value?.validate(errors => {
      if (errors) {
        console.log('formRef', errors)
        return
      }
    })
  })
}
onBeforeUnmount(() => {
  formRules.value = [];
});
defineExpose({
  resetForm,
  validForm
});
</script>

<template>
  <div class="wrapper-preview">
    <div class="preview-left pr-4">
      <n-form
          ref="caseFormRef"
          :model="form"
          :rules="rules"
          label-placement="left"
          label-width="auto"
          require-mark-placement="right-hanging"
      >
        <n-form-item :label="t('system.orgTemplate.caseName')" path="name">
          <n-input v-model:value="form.name" :placeholder="t('system.orgTemplate.caseNamePlaceholder')"
                   :maxlength="255"
                   clearable/>
        </n-form-item>
        <n-form-item :label="t('system.orgTemplate.precondition')" path="precondition">
          <rich-text v-model:raw="form.prerequisite"
                     v-model:filed-ids="prerequisiteFileIds"
                     :preview-url="`${PreviewEditorImageUrl}/${currentProjectId}`"
          />
          <!--          <n-input type="textarea" v-model:value="form.prerequisite" :maxlength="255" clearable/>-->
        </n-form-item>
        <step-description v-model:caseEditType="form.caseEditType"/>
        <div v-if="form.caseEditType === 'STEP'" class="mb-[20px] w-full">
          <add-step v-model:step-list="stepData" :is-disabled="false"/>
        </div>
        <rich-text v-else v-model:content="form.textDescription" class="mb-[20px]"/>
        <n-form-item v-if="form.caseEditType === 'TEXT'" :label="t('caseManagement.featureCase.expectedResult')"
                     path="remark">
          <rich-text v-model:content="form.expectedResult"/>
        </n-form-item>
        <n-form-item :label="t('caseManagement.featureCase.remark')" path="description">
          <rich-text v-model:content="form.description"/>
        </n-form-item>
      </n-form>
      <div class="w-[90%]">
        文件列表
      </div>
    </div>
    <div class="preview-right px-4">
      <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-placement="left"
          label-width="auto"
          require-mark-placement="right-hanging"
          class="rounded-[4px]"
      >
        <n-form-item :label="t('caseManagement.featureCase.ModuleOwned')" path="moduleId">
          <n-tree-select
              :options="caseTree"
              v-model:value="form.moduleId"
              label-field="name"
              key-field="id"
          />
        </n-form-item>
        <div v-for="item in formItem" :key="item.id">
          <n-form-item :label="item.title" path="tags">
            <n-select v-if="item.type==='SELECT'" :options="item.options" label-field="text"
                      :placeholder="t('common.pleaseSelect')"
                      @update:value="handleUpdateValue"/>
          </n-form-item>
        </div>
        <n-form-item :label="t('system.orgTemplate.tags')" path="tags">
          <n-dynamic-tags v-model:value="form.tags"/>
        </n-form-item>
      </n-form>
    </div>
  </div>
</template>

<style scoped>
.wrapper-preview {
  display: flex;

  .preview-left {
    width: calc(100% - 396px);

    .changeType {
      padding: 2px 4px;
      border-radius: 4px;

      &:hover {
        cursor: pointer;
      }
    }
  }

  .preview-right {
    width: 428px;
  }
}
</style>