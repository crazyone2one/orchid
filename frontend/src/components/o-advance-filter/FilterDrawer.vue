<script setup lang="ts">
import {
  ConditionsItem,
  FilterForm,
  FilterFormItem,
  FilterResult,
  ViewItem
} from "/@/components/o-advance-filter/type.ts";
import {FilterType, OperatorEnum, ViewTypeEnum} from "/@/enums/advanced-filter-enum.ts";
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import {cloneDeep} from "lodash-es";
import {computed, nextTick, ref, watch} from "vue";
import {
  getAllDataDefaultConditions,
  internalViewsHiddenConditionsMap,
  operatorOptionsMap
} from "/@/components/o-advance-filter/index.ts";
import ODrawer from '/@/components/o-drawer/index.vue'
import ViewNameInput from "/@/components/o-advance-filter/components/ViewNameInput.vue";
import type {FormInst} from "naive-ui";
import {NInputNumber, NAlert} from "naive-ui";
import {useRequest} from "alova/client";
import {getViewDetail} from "/@/api/modules/setting/user.ts";

const props = defineProps<{
  configList: FilterFormItem[]; // 系统字段
  customList?: FilterFormItem[]; // 自定义字段
  viewType: ViewTypeEnum;
  currentView: string; // 当前视图
  allViewNames: string[];
  canNotAddView: boolean;
  internalViews: ViewItem[]; // 系统视图
  memberOptions: { label: string; value: string }[];
}>();
const emit = defineEmits<{
  (e: 'handleFilter', value: FilterResult): void;
  (e: 'refreshViewList'): void;
  (e: 'changeViewToFirstCustom'): void;
}>();
const visible = defineModel<boolean>('visible', {required: true});

const {t} = useI18n();
const appStore = useAppStore();
const formRef = ref<FormInst | null>(null)
const viewNameInputRef = ref<InstanceType<typeof ViewNameInput>>();
const isShowNameInput = ref(false);
const defaultFormModel: FilterForm = {
  name: '',
  searchMode: 'AND',
  list: [{dataIndex: '', operator: undefined, value: '', type: FilterType.INPUT}],
};
const formModel = ref<FilterForm>(cloneDeep(defaultFormModel));
const savedFormModel = ref(cloneDeep(formModel.value));
const currentConfigList = computed<FilterFormItem[]>(() =>
    props.configList.filter(
        (item) => !(internalViewsHiddenConditionsMap[props.currentView] ?? []).includes(item.dataIndex as string)
    )
);
const searchModeOptions = [
  {value: 'AND', label: t('advanceFilter.and')},
  {value: 'OR', label: t('advanceFilter.or')},
];
const isSaveAsView = ref(false);
const saveAsViewForm = ref({name: ''});
const isInternalViews = (id?: string) => {
  return props.internalViews.some((item) => item.id === id);
}
const getListItemByDataIndex = (dataIndex: string) => {
  return [...currentConfigList.value, ...(props.customList || [])].find((item) => item.dataIndex === dataIndex);
}
const showNameInput = () => {
  isShowNameInput.value = true;
  nextTick(() => {
    viewNameInputRef.value?.inputFocus();
  });
}
const {send: fetchViewDetail} = useRequest((id) => getViewDetail(props.viewType, id), {immediate: false, force: true});
const getUserViewDetail = (id: string) => {
  fetchViewDetail(id).then(res => {
    // 全部数据默认显示搜索条件
    if (res?.id === 'all_data') {
      res.conditions = [...getAllDataDefaultConditions(props.viewType)];
    }
    const list: FilterFormItem[] = [];
    (res.conditions ?? [])?.forEach((item: ConditionsItem) => {
      const listItem = getListItemByDataIndex(item.name ?? '') as FilterFormItem;
      if (listItem) {
        list.push({
          ...listItem,
          operator: item.operator,
          value: item.value,
        });
      }
    });
    formModel.value = {...res, list};
    savedFormModel.value = cloneDeep(formModel.value);
  })
}
const currentOptions = computed(() => {
  return (currentDataIndex: string) => {
    const otherDataIndices = formModel.value.list
        .filter((listItem) => listItem.dataIndex !== currentDataIndex)
        .map((item: FilterFormItem) => item.dataIndex);
    return [...currentConfigList.value, ...(props.customList || [])]
        .filter(({dataIndex}) => !otherDataIndices.includes(dataIndex))
        .map((item) => ({...item, label: t(item.title as string)}));
  };
});
const dataIndexOptions = (param: string) => {
  return currentOptions.value(param).map(item => {
    return {label: t(item.title as string), value: item.dataIndex}
  })
}
const operatorOptions = (type: FilterType) => {
  return operatorOptionsMap[type].map(item => {
    return {...item, label: t(item.label as string)}
  })
}
const isValueDisabled = (item: FilterFormItem) => {
  return !item.dataIndex || ['EMPTY', 'NOT_EMPTY'].includes(item.operator as string);
}
const handleCancelSaveAsView = () => {
  isSaveAsView.value = false;
  saveAsViewForm.value.name = '';
}
const getParams = () => {
  const conditions = formModel.value.list.map(({customFieldType, value, operator, customField, dataIndex}) => {
    return {
      value,
      operator,
      customField: customField ?? false,
      name: dataIndex,
      customFieldType: customFieldType ?? '',
    };
  });
  return {searchMode: formModel.value.searchMode, conditions};
}
const handleFilter = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      visible.value = false;
      emit('handleFilter', getParams());
    }
  })
}
const handleReset = () => {
  formModel.value = cloneDeep(savedFormModel.value);
  isShowNameInput.value = false;
}
const resetToNewViewForm = () => {
  // 命名递增数字
  let name = '';
  for (let i = 1; i <= 10; i++) {
    const defaultName = `${t('advanceFilter.unnamedView')}${String(i).padStart(3, '0')}`;
    if (!props.allViewNames.includes(defaultName)) {
      name = defaultName;
      break;
    }
  }
  formModel.value = {
    ...cloneDeep(defaultFormModel),
    name,
  };
  savedFormModel.value = cloneDeep(formModel.value);
}
const handleAddItem = () => {
  const item = {
    dataIndex: '',
    type: FilterType.INPUT,
    value: '',
  };
  formModel.value.list.push(item);
}
const handleDeleteItem = (index: number) => {
  formModel.value.list.splice(index, 1);
}
watch(
    () => visible.value,
    async (val) => {
      // 新建视图关闭后重新获取数据
      if (!val) {
        handleCancelSaveAsView();
        if (formModel.value?.id !== props.currentView) {
          getUserViewDetail(props.currentView);
        }
      }
    }
);
watch(
    () => props.currentView,
    async (val, oldValue) => {
      getUserViewDetail(val);
      if (!oldValue.length) return;
      handleFilter();
    }
);
defineExpose({
  resetToNewViewForm,
  handleReset,
  getUserViewDetail,
});
</script>

<template>
  <o-drawer v-model:visible="visible" class="filter-drawer" :mask="false" :width="600">
    <template #title>
      <view-name-input v-if="isShowNameInput" ref="viewNameInputRef" v-model:form="formModel"
                       :all-names="allViewNames.filter((name) => name !== savedFormModel.name)"
                       @handle-submit="isShowNameInput = false"/>
      <div v-else class="flex flex-1 items-center gap-[8px] overflow-hidden">
        <div class="one-line-text"> {{ formModel.name }}</div>
        <div v-if="!isInternalViews(formModel?.id)"
             class="i-ic-round-edit-note min-w-[16px] cursor-pointer hover:text-blue-600"
             @click="showNameInput"/>
      </div>
    </template>
    <n-alert class="mb-[12px]" closable>
      {{ t('advanceFilter.filterTip') }}
    </n-alert>
    <n-form ref="formRef" :model="formModel" label-placement="top">
      <n-select v-model:value="formModel.searchMode" :options="searchModeOptions" class="mb-[12px] w-[170px]">

      </n-select>
      <div v-for="(item, index) in formModel.list" :key="item.dataIndex || `filter_item_${index}`"
           class="flex items-start gap-[8px]">
        <n-form-item class="flex-1 overflow-hidden" :path="`list[${index}].dataIndex`"
                     :rule="{ required: true, message: t('advanceFilter.conditionRequired') }">
          <n-select v-model:value="item.dataIndex" :options="dataIndexOptions(item.dataIndex as string)" filterable/>
        </n-form-item>
        <n-form-item class="w-[105px]" :path="`list[${index}].operator`">
          <n-select v-model:value="item.operator" :options="operatorOptions(item.type)" :disabled="!item.dataIndex"/>
        </n-form-item>
        <n-form-item class="flex-[1.5] overflow-hidden" :path="`list[${index}].value`">
          <n-input v-if="item.type === FilterType.TEXTAREA" v-model:value="item.value" type="textarea"
                   clearable :disabled="isValueDisabled(item)"
                   :autosize="{minRows: 1, maxRows: 1}"
                   :placeholder="t('advanceFilter.inputPlaceholder')"
                   :maxlength="1000"/>
          <n-input-number v-else-if="
              item.type === FilterType.NUMBER ||
              (item.type === FilterType.TAGS_INPUT && [OperatorEnum.COUNT_LT, OperatorEnum.COUNT_GT].includes(item.operator as OperatorEnum))
            " v-model:value="item.value" clearable :disabled="isValueDisabled(item)" :max="255"
                          :placeholder="t('common.pleaseInput')" v-bind="item.numberProps"/>
          <n-dynamic-tags
              v-else-if="item.type === FilterType.TAGS_INPUT&& ![OperatorEnum.COUNT_LT, OperatorEnum.COUNT_GT].includes(item.operator as OperatorEnum)"
              v-model:value="item.value" :disabled="isValueDisabled(item)"/>
          <n-select v-else-if="item.type === FilterType.MEMBER" v-model:value="item.value"
                    :placeholder="t('common.pleaseSelect')" :disabled="isValueDisabled(item)"
                    v-bind="{options: props.memberOptions,multiple: true,}"/>
          <n-select v-else-if="item.type === FilterType.SELECT" v-model:value="item.value"
                    :placeholder="t('common.pleaseSelect')" :disabled="isValueDisabled(item)"
                    :options="item.selectProps?.options || []"
                    v-bind="item.selectProps"/>
          <n-tree-select v-else-if="item.type === FilterType.TREE_SELECT"
                         v-model:value="item.value" :options="item.treeSelectData ?? []"
                         :disabled="isValueDisabled(item)"
                         :multiple="item.treeSelectProps?.multiple"
                         v-bind="item.treeSelectProps"
                         :placeholder="t('common.pleaseSelect')"/>
          <n-date-picker v-else-if="item.type === FilterType.DATE_PICKER && item.operator !== OperatorEnum.BETWEEN"
                         v-model:value="item.value" :disabled="isValueDisabled(item)" type="datetimerange"/>
          <n-input v-else v-model:value="item.value" :disabled="isValueDisabled(item)"
                   :placeholder="t('advanceFilter.inputPlaceholder')"
                   :maxlength="255"/>
        </n-form-item>
        <n-button text class="mt-[33px]" @click="handleDeleteItem(index)">
          <template #icon>
            <div class="i-ic-baseline-block ]"/>
          </template>
        </n-button>
      </div>
    </n-form>
    <n-button text class="mt-[5px] w-[fit-content]" @click="handleAddItem">
      <div class="i-ic-baseline-add-circle"/>
      {{ t('advanceFilter.addCondition') }}
    </n-button>
    <template #footer>
      <div v-if="!isSaveAsView" class="mb-[22px] flex items-center gap-[8px]">
        <n-button v-if="!formModel?.id" type="primary">{{ t('advanceFilter.saveAndFilter') }}</n-button>
        <n-button v-if="formModel?.id" type="primary" @click="handleFilter">{{ t('common.filter') }}</n-button>
        <n-button class="mr-[16px]" @click="handleReset">{{ t('common.reset') }}</n-button>
        <n-button v-if="!isInternalViews(formModel?.id) && formModel?.id" text
                  class="h-[32px] !text-[var(--color-text-1)]">
          {{ t('common.save') }}
        </n-button>
        <n-button v-if="(formModel?.id && !isInternalViews(formModel?.id)) || formModel?.id === 'all_data'"
                  text class="h-[32px] !text-gray" disabled>
          {{ t('advanceFilter.saveAsView') }}
        </n-button>
      </div>
      <div v-else class="flex items-center gap-[8px]">
        <view-name-input ref="saveAsViewNameInputRef" v-model:form="saveAsViewForm"
                         class="w-[240px]"
                         :all-names="allViewNames"/>
        <n-button type="primary" class="mb-[22px]">{{ t('common.save') }}</n-button>
        <n-button>{{ t('common.cancel') }}</n-button>
      </div>
    </template>
  </o-drawer>
</template>

<style scoped>

</style>