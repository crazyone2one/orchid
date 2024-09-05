<script setup lang="ts">
import {DataTableColumns, DataTableRowKey, NTooltip} from "naive-ui";
import {PassRateCountDetail, TestPlanDetail, TestPlanItem} from "/@/models/test-plan/test-plan.ts";
import {useI18n} from "vue-i18n";
import {h, onBeforeMount, ref, watch} from "vue";
import {usePagination} from "alova/client";
import {ModuleTreeNode, TableQueryParams} from "/@/models/common.ts";
import {getTestPlanList} from "/@/api/modules/test-plan/test-plan.ts";
import {useAppStore} from "/@/store";
import {testPlanTypeEnum} from "/@/enums/test-plan-enum.ts";
import AdvanceFilter from '/@/components/o-advance-filter/index.vue'
import {FilterFormItem} from "/@/components/o-advance-filter/type.ts";
import StatusTag from '/@/components/o-status-tag/index.vue'
import {getModules} from "/@/views/case-management/case-management-feature/components/utils.ts";
import dayjs from 'dayjs';
import {LastExecuteResults} from "/@/enums/case-enum.ts";


const props = defineProps<{
  activeFolder: string;
  activeFolderType: 'folder' | 'module';
  offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
  modulesCount: Record<string, number>; // 模块数量
  nodeName: string; // 选中模块名称
  moduleTree: ModuleTreeNode[];
}>();

const emit = defineEmits<{
  (e: 'init', params: any): void;
  (e: 'edit', record: TestPlanItem): void;
  (e: 'new', type: string): void;
}>();
const {t} = useI18n()
const appStore = useAppStore()
const columns: DataTableColumns<TestPlanDetail> = [
  {
    type: 'selection'
  },
  {
    title: t('testPlan.testPlanIndex.ID'),
    key: 'num',
    width: 180
  },
  {
    title: t('testPlan.testPlanIndex.testPlanName'),
    key: 'name',
    width: 180
  },
  {
    title: t('common.status'),
    key: 'status',
    width: 150,
    render: (row) => {
      if (getStatus(row?.id as string)) {
        return h(StatusTag, {status: getStatus(row?.id as string)})
      }
      return h('span', {}, {default: () => '--'})
    }
  },
  {
    title: t('common.creator'),
    key: 'createUserName',
    width: 150
  },
  {
    title: t('testPlan.testPlanIndex.passRate'),
    key: 'passRate',
    width: 200
  },
  {
    title: t('testPlan.testPlanIndex.useCount'),
    key: 'functionalCaseCount',
    width: 150
  },
  {
    title: t('common.tag'),
    key: 'tags',
    width: 300
  },
  {
    title: t('testPlan.testPlanIndex.belongModule'),
    key: 'moduleId',
    width: 200,
    render(record) {
      return h('span', {}, {default: () => getModules(record.moduleId, props.moduleTree)})
    },
  },
  {
    title: t('testPlan.testPlanIndex.planStartToEndTime'),
    key: 'planStartToEndTime',
    width: 370,
    render(record) {
      return h('span', {}, {
        default: () => {
          return [
            h('span', {}, {default: () => record.plannedStartTime ? dayjs(record.plannedStartTime).format('YYYY-MM-DD HH:mm:ss') : '-'}),
            h('span', {}, {default: () => t('common.to')}),
            h(NTooltip, {disabled: record.execStatus !== LastExecuteResults.ERROR}, {
              trigger: () => h('span', {}, {default: () => record?.plannedEndTime ? dayjs(record.plannedEndTime).format('YYYY-MM-DD HH:mm:ss') : '-'}),
              default: () => t('testPlan.planStartToEndTimeTip')
            }),
          ];
        }
      })
    }
  },
  {
    title: t('testPlan.testPlanIndex.actualStartToEndTime'),
    key: 'actualStartToEndTime',
    width: 370
  },
];
const checkedRowKeys = ref<DataTableRowKey[]>([]);
const showType = ref<keyof typeof testPlanTypeEnum>(testPlanTypeEnum.ALL);
const filterRowCount = ref(0);
const isArchived = ref<boolean>(false);
const keyword = ref<string>('');
const filterConfigList = ref<FilterFormItem[]>([]);

const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys;
};
const defaultCountDetailMap = ref<Record<string, PassRateCountDetail>>({});
const getStatus = (id: string) => {
  return defaultCountDetailMap.value[id]?.status;
}
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 6,
  keyword: ''
})
const {send: loadList, data} = usePagination(() => getTestPlanList(reqParam.value), {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
});
const initTableParams = (isSetDefaultKey = false) => {
  let moduleIds =
      props.activeFolder && props.activeFolder !== 'all' ? [props.activeFolder, ...props.offspringIds] : [];
  if (isSetDefaultKey) {
    moduleIds = [];
  }
  // const filterParams = {
  //   ...propsRes.value.filter,
  // };
  // if (isArchived.value) {
  //   filterParams.status = ['ARCHIVED'];
  // }
  reqParam.value = {
    ...reqParam.value,
    type: showType.value,
    moduleIds,
    projectId: appStore.currentProjectId,
    excludeIds: [],
    // selectAll: !!batchParams.value?.selectAll,
    selectIds: checkedRowKeys.value || [],
    // keyword: keyword.value,
    // filter: filterParams,
    // combine: {
    //   ...batchParams.value.condition,
    // },
  };
};
const loadPlanList = () => {
  initTableParams()
  loadList()
}
const fetchData = () => {
  loadPlanList()
}
onBeforeMount(() => {
  fetchData();
  initTableParams();
});
watch(
    () => props.activeFolder,
    (val) => {
      if (val) {
        fetchData();
      }
    }
);
defineExpose({
  fetchData,
  // emitTableParams,
});
</script>

<template>
  <advance-filter v-model:keyword="keyword" :row-count="filterRowCount"
                  :search-placeholder="t('common.searchByIDNameTag')"
                  :filter-config-list="filterConfigList"
                  @keyword-search="fetchData">
    <template #left>
      <div class="flex w-full items-center justify-between">
        <div>
          <n-radio-group v-model:value="showType" class="file-show-type mr-2">
            <n-radio-button :value="testPlanTypeEnum.ALL" class="show-type-icon p-[2px]">
              {{ t('testPlan.testPlanIndex.all') }}
            </n-radio-button>
            <n-radio-button :value="testPlanTypeEnum.TEST_PLAN" class="show-type-icon p-[2px]">
              {{ t('testPlan.testPlanIndex.plan') }}
            </n-radio-button>
            <n-radio-button :value="testPlanTypeEnum.GROUP" class="show-type-icon p-[2px]">
              {{ t('testPlan.testPlanIndex.testPlanGroup') }}
            </n-radio-button>
          </n-radio-group>
        </div>
        <div class="mr-[24px]">
          <n-switch v-model:value="isArchived" size="small"/>
          <span class="ml-1">{{ t('testPlan.testPlanGroup.seeArchived') }}</span>
        </div>
      </div>
    </template>
  </advance-filter>
  <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:TestPlanItem)=>tmp.id"
                @update:checked-row-keys="handleCheck"/>
</template>

<style scoped>

</style>