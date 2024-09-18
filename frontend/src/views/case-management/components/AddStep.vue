<script setup lang="ts">
import {useI18n} from "vue-i18n";
import {ComponentPublicInstance, computed, h, ref, watch} from "vue";
import {getGenerateId} from "/@/utils";
import {StepList} from "/@/models/case-management/feature-case.ts";
import {type DataTableColumns, type DropdownOption, NInput} from "naive-ui";
import {hasAnyPermission} from "/@/utils/permission.ts";
import TableMoreAction from '/@/components/o-table-more-action/index.vue'

type refItem = Element | ComponentPublicInstance | null;
const {t} = useI18n();

const props = withDefaults(
    defineProps<{
      stepList: any;
      isDisabled?: boolean;
      isScrollY?: boolean;
      scrollY?: number;
      isTestPlan?: boolean;
      isDisabledTestPlan?: boolean;
      isPreview?: boolean; // 仅预览不展示状态可操作下拉和文本框
    }>(),
    {
      isDisabled: false,
      isScrollY: true,
    }
);

const emit = defineEmits(['update:stepList']);
// 步骤描述
const stepData = ref<StepList[]>([
  {
    id: getGenerateId(),
    step: '',
    expected: '',
    showStep: false,
    showExpected: false,
  },
]);
const refStepMap: Record<string, any> = {};
const expectedRefMap: Record<string, any> = {};
const setStepRefMap = (el: refItem, record: StepList) => {
  if (el) {
    refStepMap[`${record.id}`] = el;
  }
}
const setExpectedRefMap = (el: refItem, record: StepList) => {
  if (el) {
    expectedRefMap[`${record.id}`] = el;
  }
}
const moreActions = ref<DropdownOption[]>([
  {
    label: t('caseManagement.featureCase.copyStep'),
    key: 'copyStep',
  },
  {
    label: t('caseManagement.featureCase.InsertStepsBefore'),
    key: 'InsertStepsBefore',
  },
  {
    label: t('caseManagement.featureCase.afterInsertingSteps'),
    key: 'afterInsertingSteps',
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: t('common.delete'),
    key: 'delete',
    danger: true,
  },
]);
const moreActionList = computed(() => {
  return stepData.value.length <= 1 ? moreActions.value.slice(0, moreActions.value.length - 2) : moreActions.value;
});
const columns = ref<DataTableColumns<StepList>>([
      {
        title: t('system.orgTemplate.numberIndex'),
        key: 'index',
        width: 100,
        render: (_row, index) => {
          return h('div', {class: 'w-4 h-4 leading-4 rounded-full text-center text-[12px] font-medium bg-violet-100'},
              {default: () => index + 1})
        }
      },
      {
        title: t('system.orgTemplate.useCaseStep'),
        key: 'step',
        render: (record) => {
          if (!props.isTestPlan) {
            return h(NInput, {
              ref: (el: refItem) => setStepRefMap(el, record),
              value: record.step,
              size: 'small',
              type: 'textarea',
              maxlength: 1000,
              autosize: true,
              class: 'w-max-[267px] param-input',
              placeholder: t('system.orgTemplate.stepTip'),
              disabled: props.isDisabled,
              "onUpdate:value": (value) => record.step = value
            }, {})
          }
        }
      },
      {
        title: t('system.orgTemplate.expectedResult'),
        key: 'expected',
        render: (record) => {
          if (!props.isTestPlan) {
            return h(NInput, {
              ref: (el: refItem) => setExpectedRefMap(el, record),
              value: record.expected,
              size: 'small',
              type: 'textarea',
              maxlength: 1000,
              autosize: true,
              class: 'w-max-[267px] param-input',
              placeholder: t('system.orgTemplate.expectationTip'),
              disabled: props.isDisabled,
              "onUpdate:value": (value) => record.expected = value
            }, {})
          }
        }
      },
      ...(!props.isTestPlan ? [] : [
        {
          title: t('system.orgTemplate.actualResult'),
          key: 'actualResult',
          render: (record) => {
            if (hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE']) && !props.isDisabledTestPlan) {
              if (props.isPreview) {
                return h('div', {}, {default: () => record.actualResult})
              }
              return h(NInput, {
                value: record.actualResult,
                size: 'small',
                type: 'textarea',
                maxlength: 1000,
                autosize: true,
                class: 'w-max-[267px] param-input',
                placeholder: t('system.orgTemplate.actualResultTip'),
              }, {});
            }
          }
        },
        {
          title: t('system.orgTemplate.stepExecutionResult'),
          key: 'executeResult',
        },
      ]),
      {
        title: t('system.orgTemplate.operation'),
        key: 'operation',
        fixed: 'right',
        width: 90,
        render: (record) => {
          return h(TableMoreAction,
              {
                list: moreActionList.value,
                onSelect: ($event) => handleMoreActionSelect($event, record)
              },
              {})
        }
      },
    ]
)
const handleMoreActionSelect = (item: DropdownOption, record: StepList) => {
  switch (item.key) {
    case 'copyStep':
      copyStep(record);
      break;
    case 'InsertStepsBefore':
      insertStepsBefore(record);
      break;
    case 'afterInsertingSteps':
      afterInsertingSteps(record);
      break;
    default:
      deleteStep(record);
      break;
  }
}
const copyStep = (record: StepList) => {
  const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
  const insertItem = {
    ...record,
    id: getGenerateId(),
  };
  stepData.value.splice(index + 1, 0, insertItem);
}
const insertStepsBefore = (record: StepList) => {
  const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
  const insertItem = {
    id: getGenerateId(),
    step: '',
    expected: '',
    showStep: false,
    showExpected: false,
  };
  stepData.value.splice(index, 0, insertItem);
}
const afterInsertingSteps = (record: StepList) => {
  const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
  const insertItem = {
    id: getGenerateId(),
    step: '',
    expected: '',
    showStep: false,
    showExpected: false,
  };
  stepData.value.splice(index + 1, 0, insertItem);
}
const deleteStep = (record: StepList) => {
  stepData.value = stepData.value.filter((item: any) => item.id !== record.id);
}
watch(
    () => props.stepList,
    () => {
      stepData.value = props.stepList;
    },
    {
      immediate: true,
    }
);
watch(
    () => stepData.value,
    (val) => {
      emit('update:stepList', val);
      // setProps({ data: stepData.value });
    },
    {deep: true}
);
</script>

<template>
  <n-data-table :columns="columns" :data="stepData"/>
</template>

<style scoped>

</style>