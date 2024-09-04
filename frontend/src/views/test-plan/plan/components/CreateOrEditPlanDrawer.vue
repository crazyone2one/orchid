<script setup lang="ts">
import ODrawer from '/@/components/o-drawer/index.vue';
import {ModuleTreeNode} from "/@/models/common.ts";
import {computed, ref} from "vue";
import {useI18n} from "vue-i18n";
import type {FormInst, SelectOption} from "naive-ui";
import {NDatePicker, NDynamicTags, NInputNumber, NTreeSelect} from "naive-ui";
import {AddTestPlanParams, SwitchListModel} from "/@/models/test-plan/test-plan.ts";
import {testPlanTypeEnum} from "/@/enums/test-plan-enum.ts";
import {cloneDeep} from "lodash-es";
import {useAppStore} from "/@/store";
import MoreSettingCollapse from '/@/components/o-more-setting-collapse/index.vue'
import {useForm} from "alova/client";
import {addTestPlan, updateTestPlan} from "/@/api/modules/test-plan/test-plan.ts";

const props = defineProps<{
  planId?: string;
  moduleTree?: ModuleTreeNode[];
  moduleId?: string;
}>();
const innerVisible = defineModel<boolean>('visible', {
  required: true,
});
const {t} = useI18n();
const formRef = ref<FormInst | null>(null);
const moduleId = computed(() => {
  return props.moduleId && props.moduleId !== 'all' ? props.moduleId : 'root';
});
const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'loadPlanList'): void;
}>();
const initForm: AddTestPlanParams = {
  isGroup: false,
  name: '',
  projectId: '',
  moduleId: moduleId.value,
  cycle: [Date.now(), Date.now()],
  tags: [],
  description: '',
  testPlanning: false,
  automaticStatusUpdate: false,
  repeatCase: false,
  passThreshold: 100,
  type: testPlanTypeEnum.TEST_PLAN,
  baseAssociateCaseRequest: {selectIds: [], selectAll: false, condition: {}},
};
const appStore = useAppStore()
// const form = ref<AddTestPlanParams>(cloneDeep(initForm));
const rules = {
  name: [{required: true, message: t('testPlan.planForm.nameRequired')}],
  moduleId: [{required: true, message: t('testPlan.planForm.testPlanGroupRequired')}],
  passThreshold: [{required: true, message: t('testPlan.planForm.passThresholdRequired')}],
};
const groupList = ref<SelectOption[]>([]);
const modelTitle = computed(() => {
  return props.planId ? t('testPlan.testPlanIndex.updateTestPlan') : t('testPlan.testPlanIndex.createTestPlan');
});

const okText = computed(() => {
  return props.planId ? t('common.update') : t('common.create');
});
const {loading: submiting, form, send: submit} = useForm(
    formData => {
      // 可以在此转换表单数据并提交
      const params: AddTestPlanParams = cloneDeep(formData) as AddTestPlanParams;
      params.plannedStartTime = params.cycle ? params.cycle[0] : undefined;
      params.plannedEndTime = params.cycle ? params.cycle[1] : undefined;
      params.projectId = appStore.currentProjectId;
      if (!params.isGroup && params.groupId) {
        delete params.groupId;
      }
      return !props.planId?.length ? addTestPlan(params) : updateTestPlan(params);
    },
    {
      // 初始化表单数据
      initialForm: cloneDeep(initForm)
    }
);
const handleDrawerConfirm = (isContinue: boolean) => {
  formRef.value?.validate(errors => {
    if (!errors) {
      submit().then((res) => {
        window.$message.success(!props.planId?.length ? t('common.createSuccess') : t('common.updateSuccess'));
        emit('loadPlanList');
        if (!isContinue) {
          if (!props.planId?.length) {
            // 跳转到测试计划详情
            toDetail(res.id);
          }
          handleCancel();
        } else {
          form.value = {...cloneDeep(initForm), moduleId: form.value.moduleId};
        }
      })

    }
  })
}
const toDetail = (id: string) => {
  console.log(id)
}
const handleCancel = () => {
  innerVisible.value = false;
  form.value = cloneDeep(initForm);
  formRef.value?.restoreValidation();
  emit('close')
}
const switchList: SwitchListModel[] = [
  {
    key: 'repeatCase',
    label: 'testPlan.planForm.associateRepeatCase',
    tooltipPosition: 'left-start',
    desc: ['testPlan.planForm.repeatCaseTip1', 'testPlan.planForm.repeatCaseTip2'],
  },
  {
    key: 'automaticStatusUpdate',
    label: 'testPlan.planForm.allowUpdateStatus',
    tooltipPosition: 'left-start',
    desc: ['testPlan.planForm.enableAutomaticStatusTip', 'testPlan.planForm.closeAutomaticStatusTip'],
  },
];
</script>

<template>
  <o-drawer v-model:visible="innerVisible" :width="800" :title="modelTitle" :ok-text="okText"
            :save-continue-text="t('case.saveContinueText')"
            :show-continue="!props.planId?.length"
            :ok-loading="submiting"
            @close="handleCancel"
            @cancel="handleCancel"
            @confirm="handleDrawerConfirm(false)">
    <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
    >
      <n-form-item :label="t('caseManagement.featureCase.planName')" path="name">
        <n-input v-model:value="form.name" :maxlength="255"
                 :placeholder="t('testPlan.planForm.namePlaceholder')"/>
      </n-form-item>
      <n-form-item :label="t('common.desc')" path="description">
        <n-input v-model:value="form.description" type="textarea" :maxlength="1000"
                 :placeholder="t('common.pleaseInput')"/>
      </n-form-item>
      <n-form-item
          :label="props.planId?.length ? t('caseManagement.featureCase.moveTo') : t('testPlan.planForm.createTo')"
          path="type">
        <n-radio-group v-model:value="form.isGroup">
          <n-radio :value="false">{{ t('testPlan.testPlanGroup.module') }}</n-radio>
          <n-radio :value="true">{{ t('testPlan.testPlanIndex.testPlanGroup') }}</n-radio>
        </n-radio-group>
      </n-form-item>
      <n-form-item v-if="form.isGroup" :label="t('testPlan.testPlanIndex.testPlanGroup')" path="groupId">
        <n-select v-model:value="form.moduleId" :options="groupList" filterable
                  :placeholder="t('common.pleaseSelect')"/>
      </n-form-item>
      <n-form-item v-else :label="t('common.belongModule')" path="groupId">
        <n-tree-select v-model:value="form.moduleId" :options="props.moduleTree" filterable label-field="name"
                       key-field="id"></n-tree-select>
      </n-form-item>
      <n-form-item :label="t('testPlan.planForm.planStartAndEndTime')" path="cycle">
        <n-date-picker v-model:value="form.cycle" type="daterange" clearable/>
      </n-form-item>
      <n-form-item :label="t('common.tag')" path="tags">
        <n-dynamic-tags v-model:value="form.tags" :max="10"/>
      </n-form-item>
      <more-setting-collapse>
        <template #content>
          <div v-for="item in switchList" :key="item.key" class="mb-[24px] flex items-center gap-[8px]">
            <n-switch v-model:value="form[item.key]" size="small"/>
            {{ t(item.label) }}
            <n-tooltip :placement="item.tooltipPosition">
              <template #trigger>
                <n-icon class="h-[16px] w-[16px]">
                  <div class="i-ic-baseline-question-mark"/>
                </n-icon>
              </template>
              <div v-for="descItem in item.desc" :key="descItem">{{ t(descItem) }}</div>
            </n-tooltip>
          </div>
          <n-form-item :label="t('testPlan.planForm.passThreshold')" path="passThreshold">
            <n-input-number v-model:value="form.passThreshold" size="small"
                            button-placement="both"
                            :min="1" :max="100" :precision="2"
                            :default-value="100">
            </n-input-number>
            <n-tooltip>
              <template #trigger>
                <n-icon class="h-[16px] w-[16px]">
                  <div class="i-ic-baseline-question-mark"/>
                </n-icon>
              </template>
              {{ t('testPlan.planForm.passThresholdTip') }}
            </n-tooltip>
          </n-form-item>
        </template>
      </more-setting-collapse>
    </n-form>
  </o-drawer>
</template>

<style scoped>

</style>