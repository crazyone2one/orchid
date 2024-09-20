<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import type {FormInst} from "naive-ui";
import {useAppStore} from "/@/store";
import {useI18n} from "vue-i18n";
import {FormRuleItem} from "/@/views/case-management/case-management-feature/components/types.ts";
import {DetailCase, StepList} from "/@/models/case-management/feature-case.ts";
import RichText from '/@/components/o-rich-text/index.vue'
import StepDescription from "/@/views/case-management/components/StepDescription.vue";
import AddStep from "/@/views/case-management/components/AddStep.vue";
import {getGenerateId} from "/@/utils";
import {FileItem} from "/@/components/o-upload/types.ts";
import {useRequest} from "alova/client";
import {updateCaseRequest} from "/@/api/modules/case-management/feature-case.ts";

const props = withDefaults(
    defineProps<{
      form: DetailCase;
      allowEdit?: boolean; // 是否允许编辑
      formRules?: FormRuleItem[]; // 编辑表单
      isTestPlan?: boolean; // 测试计划页面的
      isDisabledTestPlan?: boolean; // 测试计划页面-已归档
      isEdit?: boolean; // 是否为编辑状态
    }>(),
    {
      allowEdit: true, // 是否允许编辑
      isEdit: false,
    }
);
const emit = defineEmits<{
  (e: 'updateSuccess'): void;
}>();


const appStore = useAppStore();
const {t} = useI18n();
const currentProjectId = computed(() => appStore.currentProjectId);
const formRef = ref<FormInst | null>(null);
const isEditPreposition = ref<boolean>(props.isEdit); // 非编辑状态
const detailForm = ref<Record<string, any>>({
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
  moduleId: '',
  versionId: '',
  tags: [],
  customFields: [],
  relateFileMetaIds: [],
});
const stepData = ref<StepList[]>([
  {
    id: getGenerateId(),
    step: '',
    expected: '',
    showStep: false,
    showExpected: false,
  },
]);
const prepositionEdit = () => {
  isEditPreposition.value = !isEditPreposition.value;
}
const handleCancel = () => {
  detailForm.value = {...props.form};
  const {steps} = detailForm.value;
  setStepData(steps);
  isEditPreposition.value = false;
}
const setStepData = (steps: string) => {
  if (steps) {
    stepData.value = JSON.parse(steps).map((item: any) => {
      return {
        id: item.id,
        step: item.desc,
        expected: item.result,
        actualResult: item.actualResult,
        executeResult: item.executeResult,
      };
    });
  } else {
    stepData.value = [];
  }
}
const attachmentsList = ref([]);
const fileList = ref<FileItem[]>([]);
// 后台传过来的local文件的item列表
const oldLocalFileList = computed(() => {
  return attachmentsList.value.filter((item: any) => item.local);
});
// 后台已保存本地文件
const currentOldLocalFileList = computed(() => {
  return fileList.value.filter((item) => item.local && item.status !== 'pending').map((item: any) => item.uid);
});
// 删除本地上传的文件id
const deleteFileMetaIds = computed(() => {
  return oldLocalFileList.value
      .filter((item: any) => !currentOldLocalFileList.value.includes(item.id))
      .map((item: any) => item.id);
});
const getParams = () => {
  const steps = stepData.value.map((item, index) => {
    return {
      id: item.id,
      num: index,
      desc: item.step,
      result: item.expected,
    };
  });

  const customFieldsArr = props.formRules?.map((item: any) => {
    return {
      fieldId: item.field,
      value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
    };
  });

  return {
    request: {
      ...detailForm.value,
      steps: JSON.stringify(steps),
      deleteFileMetaIds: deleteFileMetaIds.value,
      // unLinkFilesIds: unLinkFilesIds.value,
      // newAssociateFileListIds: newAssociateFileListIds.value,
      customFields: customFieldsArr,
      // caseDetailFileIds: allAttachmentsFileIds.value,
    },
    fileList: fileList.value.filter((item: any) => item.status === 'init'), // 总文件列表
  };
}
const {loading, send} = useRequest(() => updateCaseRequest(getParams()), {immediate: false})
const handleOK = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      send().then(() => {
        window.$message.success(t('caseManagement.featureCase.editSuccess'));
        isEditPreposition.value = false;
        emit('updateSuccess');
      })
    }
  })
}
const getDetails = () => {
  const {steps, attachments} = detailForm.value;
  setStepData(steps);
  const fileIds = (attachments || []).map((item: any) => item.id);
}
watch(
    () => props.form,
    (val) => {
      detailForm.value = {...val};
      getDetails();
    },
    {
      deep: true,
    }
);
onMounted(() => {
  detailForm.value = {...props.form};
  getDetails();
});
defineExpose({
  handleOK,
  getParams,
  stepData,
});
</script>

<template>
  <div class="caseDetailWrapper ml-1 !break-words break-all">
    <n-form ref="formRef"
            :model="detailForm"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging">
      <n-form-item :label="t('system.orgTemplate.precondition')" path="precondition">
        <span class="absolute right-[6px] top-0">
          <n-button v-if="props.allowEdit && !props.isTestPlan" v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
                    text class="px-0" @click="prepositionEdit">
            <div class="i-carbon-edit mr-1 font-[16px]"/>
            {{ t('caseManagement.featureCase.contentEdit') }}
          </n-button>
        </span>
        <rich-text v-if="isEditPreposition" v-model:raw="detailForm.prerequisite" class="mt-2"/>
        <div
            v-else
            v-dompurify-html="detailForm?.prerequisite || '-'"
            class="markdown-body list-item-css !break-words break-all"
        />
      </n-form-item>
      <step-description v-model:case-edit-type="detailForm.caseEditType" :is-test-plan="props.isTestPlan"/>
      <div v-if="detailForm.caseEditType === 'STEP'" class="mb-[20px] w-full">
        <add-step
            v-model:step-list="stepData"
            :is-scroll-y="false"
            :is-test-plan="props.isTestPlan"
            :is-disabled-test-plan="props.isDisabledTestPlan"
            :is-disabled="!isEditPreposition"
        />
      </div>
      <rich-text v-if="detailForm.caseEditType === 'TEXT' && isEditPreposition"
                 v-model:raw="detailForm.textDescription" class="mt-2"/>
      <div v-if="detailForm.caseEditType === 'TEXT' && !isEditPreposition"
           v-dompurify-html="detailForm.textDescription || '-'" class="markdown-body !break-words break-all"/>
      <n-form-item v-if="detailForm.caseEditType === 'TEXT'"
                   field="remark"
                   class="mt-[20px]"
                   :label="t('caseManagement.featureCase.expectedResult')">
        <rich-text v-if="detailForm.caseEditType === 'TEXT' && isEditPreposition"
                   v-model:raw="detailForm.expectedResult" class="mt-2"/>
        <div v-else v-dompurify-html="detailForm.expectedResult || '-'" class="markdown-body !break-words break-all"/>
      </n-form-item>
      <n-form-item field="description" :label="t('caseManagement.featureCase.remark')">
        <rich-text v-if="isEditPreposition"
                   v-model:raw="detailForm.description" class="mt-2"/>
        <div v-else v-dompurify-html="detailForm.description || '-'" class="markdown-body !break-words break-all"/>
      </n-form-item>
      <div v-if="isEditPreposition" class="flex justify-end">
        <n-button secondary @click="handleCancel">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" class="ml-[12px]" :loading="loading" @click="handleOK">{{
            t('common.save')
          }}
        </n-button>
      </div>
      <div v-if="!props.isTestPlan" v-permission="['FUNCTIONAL_CASE:READ+UPDATE']">
        ...todo
      </div>
    </n-form>
    <div class="w-[90%]">
      <div v-if="!props.allowEdit || props.isTestPlan" class="mb-[16px] font-medium text-[var(--color-text-1)]">
        {{ t('caseManagement.featureCase.attachment') }}
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>