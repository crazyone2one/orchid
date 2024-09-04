<script setup lang="ts">
import type {DataTableColumns, DataTableRowKey} from "naive-ui";
import {TestPlanItem} from "/@/models/test-plan/test-plan.ts";
import {useI18n} from "vue-i18n";
import {onBeforeMount, ref, watch} from "vue";
import {usePagination} from "alova/client";
import {ModuleTreeNode, TableQueryParams} from "/@/models/common.ts";
import {getTestPlanList} from "/@/api/modules/test-plan/test-plan.ts";
import {useAppStore} from "/@/store";
import {testPlanTypeEnum} from "/@/enums/test-plan-enum.ts";

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
const columns: DataTableColumns<TestPlanItem> = [
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
    width: 150
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
    width: 200
  },
  {
    title: t('testPlan.testPlanIndex.planStartToEndTime'),
    key: 'planStartToEndTime',
    width: 370
  },
  {
    title: t('testPlan.testPlanIndex.actualStartToEndTime'),
    key: 'actualStartToEndTime',
    width: 370
  },
];
const checkedRowKeys = ref<DataTableRowKey[]>([]);
const showType = ref<keyof typeof testPlanTypeEnum>(testPlanTypeEnum.ALL);
const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys;
};
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
  const params = initTableParams();
  console.log('params', params)
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
  <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:TestPlanItem)=>tmp.id"
                @update:checked-row-keys="handleCheck"/>
</template>

<style scoped>

</style>